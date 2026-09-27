package com.kars.common.pdf;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

class PdfGeneratorTest {
    // PDFBox 自带的测试用西文字体；业务中文字体由接入方提供。
    private static final String FONT = "org/apache/pdfbox/resources/ttf/LiberationSans-Regular.ttf";
    @TempDir Path temporary;

    private PdfOptions.Builder options() {
        return PdfOptions.builder().font("classpath:" + FONT).pageSize(300, 300).margins(30, 30, 30, 40);
    }

    @Test
    void wrapsPaginatesEmbedsFontAndAddsTotalPageNumbers() throws Exception {
        PdfGenerator generator = new PdfGenerator(options().title("Report").build(), PdfGenerator.pageNumbers(9));
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < 60; i++) text.append("Record ").append(i).append(" has readable content.\n");
        byte[] bytes = generator.generate(PdfComponents.paragraph(text.toString()));
        try (PDDocument document = Loader.loadPDF(bytes)) {
            assertTrue(document.getNumberOfPages() > 1);
            assertEquals("Report", document.getDocumentInformation().getTitle());
            String extracted = new PDFTextStripper().getText(document);
            assertTrue(extracted.contains("Record 0"));
            assertTrue(extracted.contains("Record 59"));
            for (int i = 0; i < document.getNumberOfPages(); i++) {
                assertTrue(extracted.contains("Page " + (i + 1) + " / " + document.getNumberOfPages()));
                PDPage page = document.getPage(i);
                for (COSName name : page.getResources().getFontNames()) {
                    assertTrue(page.getResources().getFont(name).isEmbedded());
                }
            }
        }
    }

    @Test
    void repeatsTableHeadersAndKeepsAllRows() throws Exception {
        List<List<String>> rows = new ArrayList<>();
        for (int i = 0; i < 35; i++) rows.add(Arrays.asList("Item " + i, "Description"));
        PdfGenerator generator = new PdfGenerator(options().build());
        try (PDDocument document = Loader.loadPDF(generator.generate(
                PdfComponents.table(Arrays.asList("Code", "Details"), rows)))) {
            assertTrue(document.getNumberOfPages() > 1);
            PDFTextStripper stripper = new PDFTextStripper();
            String all = stripper.getText(document);
            assertTrue(all.contains("Item 0"));
            assertTrue(all.contains("Item 34"));
            for (int page = 1; page <= document.getNumberOfPages(); page++) {
                stripper.setStartPage(page);
                stripper.setEndPage(page);
                String content = stripper.getText(document);
                assertTrue(content.contains("Code"));
                assertTrue(content.contains("Details"));
            }
        }
    }

    @Test
    void rendersImageAndCustomComponent() throws Exception {
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(20, 10, BufferedImage.TYPE_INT_RGB), "png", png);
        byte[] result = new PdfGenerator(options().build()).generate(
                PdfComponents.image(png.toByteArray(), 100, 50),
                context -> context.text("Custom component"));
        try (PDDocument document = Loader.loadPDF(result)) {
            assertTrue(document.getPage(0).getResources().getXObjectNames().iterator().hasNext());
            assertTrue(new PDFTextStripper().getText(document).contains("Custom component"));
        }
    }

    @Test
    void doesNotCloseCallerStreamOnSuccessOrFailure() throws Exception {
        class TrackingStream extends ByteArrayOutputStream {
            boolean closed;
            @Override public void close() { closed = true; }
        }
        PdfGenerator generator = new PdfGenerator(options().build());
        TrackingStream success = new TrackingStream();
        generator.write(success, PdfComponents.paragraph("OK"));
        assertFalse(success.closed);
        TrackingStream failure = new TrackingStream();
        IOException error = new IOException("component failed");
        assertSame(error, assertThrows(IOException.class, () -> generator.write(failure, context -> { throw error; })));
        assertFalse(failure.closed);
        assertEquals(0, failure.size());
    }

    @Test
    void preservesExistingFileWhenRenderingFailsAndCleansTemporaryFiles() throws Exception {
        Path output = temporary.resolve("existing.pdf");
        byte[] previous = "previous file".getBytes(StandardCharsets.UTF_8);
        Files.write(output, previous);
        PdfGenerator generator = new PdfGenerator(options().build());
        assertThrows(IOException.class, () -> generator.write(output, context -> { throw new IOException("failed"); }));
        assertArrayEquals(previous, Files.readAllBytes(output));
        try (java.util.stream.Stream<Path> files = Files.list(temporary)) { assertEquals(1, files.count()); }
        generator.write(output, PdfComponents.paragraph("replacement"));
        try (PDDocument document = Loader.loadPDF(Files.readAllBytes(output))) {
            assertTrue(new PDFTextStripper().getText(document).contains("replacement"));
        }
    }

    @Test
    void loadsFontFromFilesystemAndReportsMissingResource() throws Exception {
        Path font = temporary.resolve("test.ttf");
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(FONT)) {
            assertNotNull(input);
            Files.copy(input, font);
        }
        PdfGenerator generator = new PdfGenerator(options().font(font.toString()).build());
        assertTrue(generator.generate(PdfComponents.paragraph("File font")).length > 0);
        PdfGenerator missing = new PdfGenerator(options().font("classpath:fonts/missing.ttf").build());
        IOException error = assertThrows(IOException.class, () -> missing.generate(PdfComponents.paragraph("test")));
        assertTrue(error.getMessage().contains("fonts/missing.ttf"));
    }

    @Test
    void rejectsUnsupportedCharactersAndInvalidLayout() {
        PdfGenerator generator = new PdfGenerator(options().build());
        assertThrows(IOException.class, () -> generator.generate(PdfComponents.paragraph("中文")));
        assertThrows(IllegalArgumentException.class, () -> options().fontSize(Float.NaN).build());
        assertThrows(IllegalArgumentException.class, () -> options().margins(200, 200, 0, 0).build());
        assertThrows(IllegalArgumentException.class, () -> generator.generate(PdfComponents.heading("Too big", 500)));
        assertThrows(IllegalArgumentException.class, () -> PdfComponents.table(
                Arrays.asList("A", "B"), Arrays.asList(Arrays.asList("one"))));
    }

    @Test
    void rejectsOversizedTableRowWithoutLoopingOrClipping() {
        StringBuilder large = new StringBuilder();
        for (int i = 0; i < 50; i++) large.append("line\n");
        PdfComponent table = PdfComponents.table(Arrays.asList("A"), Arrays.asList(Arrays.asList(large.toString())));
        assertThrows(IllegalArgumentException.class, () -> new PdfGenerator(options().build()).generate(table));
    }

    @Test
    void reusesGeneratorAcrossConcurrentDocuments() throws Exception {
        PdfGenerator generator = new PdfGenerator(options().build());
        ExecutorService executor = Executors.newFixedThreadPool(4);
        try {
            List<Callable<byte[]>> work = new ArrayList<>();
            for (int i = 0; i < 8; i++) {
                final String label = "Document " + i;
                work.add(() -> generator.generate(PdfComponents.paragraph(label)));
            }
            List<Future<byte[]>> results = executor.invokeAll(work);
            for (int i = 0; i < results.size(); i++) {
                try (PDDocument document = Loader.loadPDF(results.get(i).get())) {
                    assertTrue(new PDFTextStripper().getText(document).contains("Document " + i));
                }
            }
        } finally { executor.shutdownNow(); }
    }
}
