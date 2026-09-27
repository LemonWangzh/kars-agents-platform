package com.kars.common.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.font.PDFont;

import java.io.IOException;

/** 内容完成后逐页调用，页码从 1 开始。适合页眉、页脚、水印；请在页边距内绘制。 */
@FunctionalInterface
public interface PdfPageDecorator {
    void decorate(PDDocument document, PDPage page, PDFont font, int pageNumber, int totalPages) throws IOException;
}
