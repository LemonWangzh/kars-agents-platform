package com.kars.common.pdf;

import org.apache.pdfbox.pdmodel.PDPageContentStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** 可组合的常用内容组件；复杂图表等可直接实现 PdfComponent。 */
public final class PdfComponents {
    private PdfComponents() { }

    public static PdfComponent paragraph(String text) {
        Objects.requireNonNull(text, "text");
        return context -> context.text(text);
    }

    public static PdfComponent heading(String text, float size) {
        Objects.requireNonNull(text, "text");
        PdfOptions.positive(size, "heading size");
        return context -> context.text(text, size);
    }

    public static PdfComponent pageBreak() { return PdfRenderContext::newPage; }

    public static PdfComponent image(byte[] data, float maxWidth, float maxHeight) {
        byte[] copy = Objects.requireNonNull(data, "image data").clone();
        PdfOptions.positive(maxWidth, "image width");
        PdfOptions.positive(maxHeight, "image height");
        return context -> context.image(copy, maxWidth, maxHeight);
    }

    /** 等宽列的基础表格，单元格自动换行、跨页重复表头；单行不拆页，超高行会明确报错。 */
    public static PdfComponent table(List<String> headers, List<? extends List<String>> rows) {
        List<String> header = copyRow(headers);
        if (header.isEmpty()) throw new IllegalArgumentException("Table requires at least one column");
        List<List<String>> data = new ArrayList<>();
        for (List<String> row : Objects.requireNonNull(rows, "rows")) {
            List<String> copy = copyRow(row);
            if (copy.size() != header.size()) throw new IllegalArgumentException("Table row/column count mismatch");
            data.add(copy);
        }
        return context -> {
            float size = context.options().fontSize();
            float columnWidth = context.options().contentWidth() / header.size();
            float padding = 5;
            List<List<String>> headerLines = wrapRow(context, header, size, columnWidth - padding * 2);
            float headerHeight = rowHeight(headerLines, size * context.options().lineSpacing(), padding);
            if (data.isEmpty()) {
                context.ensureSpace(headerHeight);
                drawRow(context, headerLines, columnWidth, headerHeight, size, padding);
            } else {
                boolean first = true;
                for (List<String> row : data) {
                    List<List<String>> lines = wrapRow(context, row, size, columnWidth - padding * 2);
                    float height = rowHeight(lines, size * context.options().lineSpacing(), padding);
                    if (headerHeight + height > context.options().contentHeight()) {
                        throw new IllegalArgumentException("Table header plus row exceeds a page; split the row or reduce font size");
                    }
                    if (first || height > context.remainingHeight()) {
                        if (first) context.ensureSpace(headerHeight + height);
                        else context.newPage();
                        drawRow(context, headerLines, columnWidth, headerHeight, size, padding);
                        first = false;
                    }
                    drawRow(context, lines, columnWidth, height, size, padding);
                }
            }
            context.gap(size * 0.5f);
        };
    }

    private static List<String> copyRow(List<String> row) {
        List<String> result = new ArrayList<>(Objects.requireNonNull(row, "table row"));
        for (String cell : result) Objects.requireNonNull(cell, "table cell");
        return result;
    }

    private static List<List<String>> wrapRow(PdfRenderContext context, List<String> row,
                                             float size, float width) throws IOException {
        List<List<String>> result = new ArrayList<>();
        for (String cell : row) result.add(context.wrap(cell, size, width));
        return result;
    }

    private static float rowHeight(List<List<String>> row, float lineHeight, float padding) {
        int count = 1;
        for (List<String> cell : row) count = Math.max(count, cell.size());
        return count * lineHeight + padding * 2;
    }

    private static void drawRow(PdfRenderContext context, List<List<String>> row, float width,
                                float height, float size, float padding) throws IOException {
        PDPageContentStream stream = context.contentStream();
        stream.saveGraphicsState();
        try {
            stream.setLineWidth(0.5f);
            float x = context.options().left();
            for (List<String> cell : row) {
                stream.addRect(x, context.y() - height, width, height);
                stream.stroke();
                float baseline = context.y() - padding - size;
                for (String line : cell) {
                    context.drawText(line, size, x + padding, baseline);
                    baseline -= size * context.options().lineSpacing();
                }
                x += width;
            }
        } finally {
            stream.restoreGraphicsState();
        }
        context.advance(height);
    }
}
