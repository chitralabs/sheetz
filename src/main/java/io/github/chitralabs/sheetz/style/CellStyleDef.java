package io.github.chitralabs.sheetz.style;

import java.util.Objects;

/**
 * Immutable, POI-independent value object holding cell style properties.
 *
 * <p>Instances are created via {@link CellStyleBuilder} or parsed from
 * {@link io.github.chitralabs.sheetz.annotation.Style} annotations via
 * {@link StyleAnnotationParser}.</p>
 *
 * @see CellStyleBuilder
 * @see PoiStyleResolver
 */
public final class CellStyleDef {

    private final String fontName;
    private final int fontSize;
    private final boolean bold;
    private final boolean italic;
    private final boolean underline;
    private final boolean strikethrough;
    private final String fontColor;
    private final String backgroundColor;
    private final String fillPattern;
    private final String borderStyle;
    private final String borderColor;
    private final String horizontalAlignment;
    private final String verticalAlignment;
    private final boolean wrapText;
    private final String dataFormat;
    private final boolean hyperlink;
    private final String hyperlinkType;

    CellStyleDef(String fontName, int fontSize, boolean bold, boolean italic,
                 boolean underline, boolean strikethrough, String fontColor,
                 String backgroundColor, String fillPattern, String borderStyle,
                 String borderColor, String horizontalAlignment, String verticalAlignment,
                 boolean wrapText, String dataFormat, boolean hyperlink, String hyperlinkType) {
        this.fontName = fontName;
        this.fontSize = fontSize;
        this.bold = bold;
        this.italic = italic;
        this.underline = underline;
        this.strikethrough = strikethrough;
        this.fontColor = fontColor;
        this.backgroundColor = backgroundColor;
        this.fillPattern = fillPattern;
        this.borderStyle = borderStyle;
        this.borderColor = borderColor;
        this.horizontalAlignment = horizontalAlignment;
        this.verticalAlignment = verticalAlignment;
        this.wrapText = wrapText;
        this.dataFormat = dataFormat;
        this.hyperlink = hyperlink;
        this.hyperlinkType = hyperlinkType;
    }

    public String fontName() { return fontName; }
    public int fontSize() { return fontSize; }
    public boolean bold() { return bold; }
    public boolean italic() { return italic; }
    public boolean underline() { return underline; }
    public boolean strikethrough() { return strikethrough; }
    public String fontColor() { return fontColor; }
    public String backgroundColor() { return backgroundColor; }
    public String fillPattern() { return fillPattern; }
    public String borderStyle() { return borderStyle; }
    public String borderColor() { return borderColor; }
    public String horizontalAlignment() { return horizontalAlignment; }
    public String verticalAlignment() { return verticalAlignment; }
    public boolean wrapText() { return wrapText; }
    public String dataFormat() { return dataFormat; }
    public boolean hyperlink() { return hyperlink; }
    public String hyperlinkType() { return hyperlinkType; }

    /**
     * Returns true if this style definition has no properties set
     * (all defaults).
     */
    public boolean isEmpty() {
        return (fontName == null || fontName.isEmpty())
            && fontSize == 0
            && !bold && !italic && !underline && !strikethrough
            && (fontColor == null || fontColor.isEmpty())
            && (backgroundColor == null || backgroundColor.isEmpty())
            && (fillPattern == null || fillPattern.isEmpty())
            && (borderStyle == null || borderStyle.isEmpty())
            && (borderColor == null || borderColor.isEmpty())
            && (horizontalAlignment == null || horizontalAlignment.isEmpty())
            && (verticalAlignment == null || verticalAlignment.isEmpty())
            && !wrapText
            && (dataFormat == null || dataFormat.isEmpty())
            && !hyperlink;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CellStyleDef)) return false;
        CellStyleDef that = (CellStyleDef) o;
        return fontSize == that.fontSize && bold == that.bold && italic == that.italic
            && underline == that.underline && strikethrough == that.strikethrough
            && wrapText == that.wrapText && hyperlink == that.hyperlink
            && Objects.equals(fontName, that.fontName) && Objects.equals(fontColor, that.fontColor)
            && Objects.equals(backgroundColor, that.backgroundColor)
            && Objects.equals(fillPattern, that.fillPattern)
            && Objects.equals(borderStyle, that.borderStyle) && Objects.equals(borderColor, that.borderColor)
            && Objects.equals(horizontalAlignment, that.horizontalAlignment)
            && Objects.equals(verticalAlignment, that.verticalAlignment)
            && Objects.equals(dataFormat, that.dataFormat)
            && Objects.equals(hyperlinkType, that.hyperlinkType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fontName, fontSize, bold, italic, underline, strikethrough,
            fontColor, backgroundColor, fillPattern, borderStyle, borderColor,
            horizontalAlignment, verticalAlignment, wrapText, dataFormat, hyperlink, hyperlinkType);
    }

    @Override
    public String toString() {
        return "CellStyleDef{" +
            (bold ? "bold, " : "") +
            (italic ? "italic, " : "") +
            (fontName != null && !fontName.isEmpty() ? "font=" + fontName + ", " : "") +
            (fontSize > 0 ? "size=" + fontSize + ", " : "") +
            (fontColor != null && !fontColor.isEmpty() ? "color=" + fontColor + ", " : "") +
            (backgroundColor != null && !backgroundColor.isEmpty() ? "bg=" + backgroundColor + ", " : "") +
            (hyperlink ? "hyperlink, " : "") +
            '}';
    }
}
