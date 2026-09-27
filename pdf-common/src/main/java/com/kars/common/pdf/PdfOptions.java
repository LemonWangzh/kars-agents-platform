package com.kars.common.pdf;

import org.apache.pdfbox.pdmodel.common.PDRectangle;

import java.util.Objects;

/** 不可变页面配置，所有尺寸以 point 为单位，72 point = 1 inch。 */
public final class PdfOptions {
    private final float width, height, left, right, top, bottom, fontSize, lineSpacing;
    private final String title;
    private final PdfFontSource fontSource;

    private PdfOptions(Builder b) {
        width = positive(b.width, "page width");
        height = positive(b.height, "page height");
        left = nonNegative(b.left, "left margin");
        right = nonNegative(b.right, "right margin");
        top = nonNegative(b.top, "top margin");
        bottom = nonNegative(b.bottom, "bottom margin");
        fontSize = positive(b.fontSize, "font size");
        lineSpacing = positive(b.lineSpacing, "line spacing");
        if (lineSpacing < 1 || width <= left + right || height - top - bottom < fontSize * lineSpacing) {
            throw new IllegalArgumentException("Margins/font size must leave room for content; line spacing must be >= 1");
        }
        title = Objects.requireNonNull(b.title, "title");
        fontSource = Objects.requireNonNull(b.fontSource, "Configure a PDF font using font(...) before build()");
    }

    public static Builder builder() { return new Builder(); }
    public PDRectangle pageSize() { return new PDRectangle(width, height); }
    public float left() { return left; }
    public float right() { return right; }
    public float top() { return top; }
    public float bottom() { return bottom; }
    public float fontSize() { return fontSize; }
    public float lineSpacing() { return lineSpacing; }
    public float contentWidth() { return width - left - right; }
    public float contentHeight() { return height - top - bottom; }
    public String title() { return title; }
    public PdfFontSource fontSource() { return fontSource; }

    static float positive(float value, String name) {
        if (!Float.isFinite(value) || value <= 0) throw new IllegalArgumentException(name + " must be finite and > 0");
        return value;
    }

    static float nonNegative(float value, String name) {
        if (!Float.isFinite(value) || value < 0) throw new IllegalArgumentException(name + " must be finite and >= 0");
        return value;
    }

    public static final class Builder {
        private float width = PDRectangle.A4.getWidth(), height = PDRectangle.A4.getHeight();
        private float left = 48, right = 48, top = 48, bottom = 48, fontSize = 12, lineSpacing = 1.4f;
        private String title = "";
        private PdfFontSource fontSource;

        private Builder() { }
        public Builder pageSize(float width, float height) { this.width = width; this.height = height; return this; }
        public Builder margins(float left, float right, float top, float bottom) {
            this.left = left; this.right = right; this.top = top; this.bottom = bottom; return this;
        }
        public Builder fontSize(float size) { fontSize = size; return this; }
        public Builder lineSpacing(float multiple) { lineSpacing = multiple; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder font(String location) { return font(PdfFontSource.from(location)); }
        public Builder font(PdfFontSource source) { fontSource = source; return this; }
        public PdfOptions build() { return new PdfOptions(this); }
    }
}
