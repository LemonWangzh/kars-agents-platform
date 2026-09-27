package com.kars.common.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** 单次生成的排版上下文，不跨线程共享。纵向游标从页面顶部向下移动。 */
public final class PdfRenderContext implements Closeable {
    private final PDDocument document;
    private final PDFont font;
    private final PdfOptions options;
    private PDPage page;
    private PDPageContentStream stream;
    private float y;

    PdfRenderContext(PDDocument document, PDFont font, PdfOptions options) throws IOException {
        this.document = document;
        this.font = font;
        this.options = options;
        newPage();
    }

    public PDDocument document() { return document; }
    public PDPage page() { return page; }
    public PDFont font() { return font; }
    public PdfOptions options() { return options; }
    public float y() { return y; }
    public float remainingHeight() { return y - options.bottom(); }

    /** 自定义绘制时可访问原生 API；勿关闭此流，并恢复自己修改的图形状态。 */
    public PDPageContentStream contentStream() { return stream; }

    public void newPage() throws IOException {
        close();
        page = new PDPage(options.pageSize());
        document.addPage(page);
        stream = new PDPageContentStream(document, page);
        y = page.getMediaBox().getHeight() - options.top();
    }

    public void ensureSpace(float height) throws IOException {
        PdfOptions.nonNegative(height, "required height");
        if (height > options.contentHeight()) {
            throw new IllegalArgumentException("Component is taller than a page's content area: " + height);
        }
        if (height > remainingHeight()) newPage();
    }

    /** 消耗当前页空间，不隐式分页。 */
    public void advance(float height) {
        PdfOptions.nonNegative(height, "advance height");
        if (height > remainingHeight()) throw new IllegalArgumentException("Not enough space; call ensureSpace first");
        y -= height;
    }

    /** 段后间距在页底截断，避免产生空白末页。 */
    public void gap(float height) {
        PdfOptions.nonNegative(height, "gap height");
        y = Math.max(options.bottom(), y - height);
    }

    public void text(String text) throws IOException { text(text, options.fontSize()); }

    public void text(String text, float size) throws IOException {
        float lineHeight = PdfOptions.positive(size, "font size") * options.lineSpacing();
        for (String line : wrap(text, size, options.contentWidth())) {
            ensureSpace(lineHeight);
            drawText(line, size, options.left(), y - size);
            advance(lineHeight);
        }
        gap(size * 0.5f);
    }

    /** 按 Unicode 码点换行，保留显式换行，优先在空格处折行。 */
    public List<String> wrap(String text, float size, float width) throws IOException {
        Objects.requireNonNull(text, "text");
        PdfOptions.positive(size, "font size");
        PdfOptions.positive(width, "text width");
        List<String> lines = new ArrayList<>();
        String normalized = text.replace("\r\n", "\n").replace('\r', '\n').replace("\t", "    ");
        for (String paragraph : normalized.split("\n", -1)) {
            StringBuilder line = new StringBuilder();
            float used = 0;
            for (int offset = 0; offset < paragraph.length();) {
                int codePoint = paragraph.codePointAt(offset);
                String glyph = new String(Character.toChars(codePoint));
                float glyphWidth = textWidth(glyph, size);
                if (glyphWidth > width) {
                    throw new IllegalArgumentException("A glyph is wider than the available text area");
                }
                if (used + glyphWidth > width && line.length() > 0) {
                    int space = line.lastIndexOf(" ");
                    if (space > 0) {
                        lines.add(line.substring(0, space));
                        line.delete(0, space + 1);
                        used = textWidth(line.toString(), size);
                    }
                    if (used + glyphWidth > width) {
                        lines.add(line.toString());
                        line.setLength(0);
                        used = 0;
                    }
                }
                line.append(glyph);
                used += glyphWidth;
                offset += Character.charCount(codePoint);
            }
            lines.add(line.toString());
        }
        return lines;
    }

    public float textWidth(String text, float size) throws IOException {
        try {
            return font.getStringWidth(text) * size / 1000f;
        } catch (IllegalArgumentException ex) {
            throw new IOException("Configured PDF font cannot encode text; supply a font covering all required characters", ex);
        }
    }

    /** 在 PDF 原生坐标系绘制（原点位于左下角），不移动游标。 */
    public void drawText(String text, float size, float x, float baseline) throws IOException {
        PdfOptions.positive(size, "font size");
        textWidth(text, size); // 在 beginText 之前检查缺失字形。
        stream.beginText();
        try {
            stream.setFont(font, size);
            stream.newLineAtOffset(x, baseline);
            stream.showText(text);
        } finally {
            stream.endText();
        }
    }

    public void image(byte[] data, float maxWidth, float maxHeight) throws IOException {
        Objects.requireNonNull(data, "image data");
        PdfOptions.positive(maxWidth, "image width");
        PdfOptions.positive(maxHeight, "image height");
        PDImageXObject image = PDImageXObject.createFromByteArray(document, data, "image");
        float scale = Math.min(Math.min(maxWidth, options.contentWidth()) / image.getWidth(),
                Math.min(maxHeight, options.contentHeight()) / image.getHeight());
        float width = image.getWidth() * scale;
        float height = image.getHeight() * scale;
        ensureSpace(height);
        stream.drawImage(image, options.left(), y - height, width, height);
        advance(height);
        gap(options.fontSize() * 0.5f);
    }

    @Override
    public void close() throws IOException {
        if (stream != null) {
            PDPageContentStream previous = stream;
            stream = null;
            previous.close();
        }
    }
}
