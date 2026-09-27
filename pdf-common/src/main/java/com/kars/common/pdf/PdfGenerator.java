package com.kars.common.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * PDF 生成入口，无 Spring 依赖。每次调用独立创建并关闭文档和字体。
 * options、组件、字体工厂和装饰器无共享可变状态时，生成器可安全并发复用。
 */
public final class PdfGenerator {
    private final PdfOptions options;
    private final List<PdfPageDecorator> decorators;

    public PdfGenerator(PdfOptions options, PdfPageDecorator... decorators) {
        this.options = Objects.requireNonNull(options, "options");
        List<PdfPageDecorator> copy = new ArrayList<>(Arrays.asList(decorators));
        for (PdfPageDecorator decorator : copy) Objects.requireNonNull(decorator, "page decorator");
        this.decorators = Collections.unmodifiableList(copy);
    }

    public byte[] generate(PdfComponent... components) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        write(output, components);
        return output.toByteArray();
    }

    /** 不关闭调用方的输出流；I/O 失败时流可能已写入部分 PDF，由调用方处理响应失败。 */
    public void write(OutputStream output, PdfComponent... components) throws IOException {
        Objects.requireNonNull(output, "output stream");
        Objects.requireNonNull(components, "components");
        for (PdfComponent component : components) Objects.requireNonNull(component, "component");
        try (PDDocument document = new PDDocument()) {
            PDFont font = Objects.requireNonNull(options.fontSource().load(document), "loaded font");
            document.getDocumentInformation().setTitle(options.title());
            try (PdfRenderContext context = new PdfRenderContext(document, font, options)) {
                for (PdfComponent component : components) component.render(context);
            }
            int total = document.getNumberOfPages();
            for (int index = 0; index < total; index++) {
                for (PdfPageDecorator decorator : decorators) {
                    decorator.decorate(document, document.getPage(index), font, index + 1, total);
                }
            }
            document.save(new FilterOutputStream(output) {
                @Override public void write(byte[] bytes, int offset, int length) throws IOException {
                    out.write(bytes, offset, length);
                }
                @Override public void close() throws IOException { flush(); }
            });
        }
    }

    /** 在同目录临时文件生成成功后替换目标，生成失败不会覆盖已有文件。 */
    public void write(Path destination, PdfComponent... components) throws IOException {
        Path target = Objects.requireNonNull(destination, "destination").toAbsolutePath();
        Path parent = target.getParent();
        if (parent == null) throw new IllegalArgumentException("Destination must be a file path");
        Files.createDirectories(parent);
        Path temporary = Files.createTempFile(parent, ".pdf-", ".tmp");
        try {
            try (OutputStream output = Files.newOutputStream(temporary)) { write(output, components); }
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    /** 页底居中的 Page X / Y；调用方需为字号预留足够下边距。 */
    public static PdfPageDecorator pageNumbers(float size) {
        PdfOptions.positive(size, "page number font size");
        return (document, page, font, number, total) -> {
            String text = "Page " + number + " / " + total;
            float width = font.getStringWidth(text) * size / 1000f;
            try (PDPageContentStream stream = new PDPageContentStream(document, page,
                    PDPageContentStream.AppendMode.APPEND, true, true)) {
                stream.beginText();
                stream.setFont(font, size);
                stream.newLineAtOffset((page.getMediaBox().getWidth() - width) / 2, size * 1.5f);
                stream.showText(text);
                stream.endText();
            }
        };
    }
}
