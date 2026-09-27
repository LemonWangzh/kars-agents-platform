package com.kars.common.pdf;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/** 手工生成中文 PDF；本地开发可自动使用已有字体，找不到字体时跳过。 */
class PdfSampleTest {

    @Test
    void generateChineseReport() throws Exception {
        String fontLocation = System.getProperty("pdf.font", System.getenv("PDF_FONT_LOCATION"));
        if (fontLocation == null && getClass().getResource("/fonts/chinese-regular.ttf") != null) {
            fontLocation = "classpath:fonts/chinese-regular.ttf";
        }
        // 仅样例测试提供本机字体回退；生产组件和 K8s 仍应显式配置字体。
        Path localFont = Paths.get("/Library/Fonts/Arial Unicode.ttf");
        if (fontLocation == null && Files.isReadable(localFont) && Files.isRegularFile(localFont)) {
            fontLocation = localFont.toString();
        }
        assumeTrue(fontLocation != null && !fontLocation.trim().isEmpty(),
                "未找到中文字体，请在 IDEA 的 VM options 中设置 -Dpdf.font=\"/path/to/chinese.ttf\"，或配置 PDF_FONT_LOCATION");
        System.out.println("PDF 样例使用字体：" + fontLocation);

        Path output = Paths.get(System.getProperty("pdf.output", "target/generated-pdf/sample-report.pdf"));
        PdfOptions options = PdfOptions.builder()
                .font(fontLocation)
                .title("订单报告示例")
                .fontSize(12)
                .margins(48, 48, 48, 48)
                .build();
        PdfGenerator generator = new PdfGenerator(options, PdfGenerator.pageNumbers(9));

        List<List<String>> rows = new ArrayList<>();
        for (int index = 1; index <= 30; index++) {
            rows.add(Arrays.asList(String.format("商品 %02d", index), "办公用品采购明细", "100.00 元"));
        }

        generator.write(output,
                PdfComponents.heading("订单报告示例", 22),
                PdfComponents.paragraph("客户：小王\n订单编号：DEMO-2026-001\n本报告用于演示中文字体嵌入、表格跨页和总页码。"),
                PdfComponents.table(Arrays.asList("商品名称", "订单说明", "金额"), rows),
                PdfComponents.paragraph("合计：3000.00 元"),
                PdfComponents.paragraph("以上为测试数据，不代表实际交易。"));

        try (PDDocument document = Loader.loadPDF(Files.readAllBytes(output))) {
            assertTrue(document.getNumberOfPages() >= 2, "样例应展示跨页表格");
            assertEquals("订单报告示例", document.getDocumentInformation().getTitle());
            String text = new PDFTextStripper().getText(document);
            assertTrue(text.contains("客户：小王"));
            assertTrue(text.contains("商品 30"));
            assertTrue(text.contains("合计：3000.00 元"));
        }
        System.out.println("PDF 已生成：" + output.toAbsolutePath());
    }
}
