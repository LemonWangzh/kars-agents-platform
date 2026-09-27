package com.kars.common.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/** 每次生成加载属于当前文档的字体，不要跨文档缓存 PDFont。 */
@FunctionalInterface
public interface PdfFontSource {
    PDFont load(PDDocument document) throws IOException;

    /** 支持 classpath:fonts/example.ttf 或文件系统路径（推荐静态 TrueType 字体）。 */
    static PdfFontSource from(String location) {
        Objects.requireNonNull(location, "font location");
        if (location.trim().isEmpty()) {
            throw new IllegalArgumentException("Font location must not be blank");
        }
        return location.startsWith("classpath:")
                ? classpath(location.substring("classpath:".length())) : file(Paths.get(location));
    }

    static PdfFontSource classpath(String resource) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        return classpath(resource, loader == null ? PdfFontSource.class.getClassLoader() : loader);
    }

    static PdfFontSource classpath(String resource, ClassLoader loader) {
        Objects.requireNonNull(resource, "font resource");
        Objects.requireNonNull(loader, "class loader");
        final String name = resource.startsWith("/") ? resource.substring(1) : resource;
        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("Font resource must not be blank");
        }
        return document -> {
            try (InputStream input = loader.getResourceAsStream(name)) {
                if (input == null) {
                    throw new IOException("PDF font resource not found: classpath:" + name);
                }
                return PDType0Font.load(document, input, true);
            }
        };
    }

    static PdfFontSource file(Path path) {
        final Path resolved = Objects.requireNonNull(path, "font path").toAbsolutePath();
        return document -> {
            try (InputStream input = Files.newInputStream(resolved)) {
                return PDType0Font.load(document, input, true);
            }
        };
    }
}
