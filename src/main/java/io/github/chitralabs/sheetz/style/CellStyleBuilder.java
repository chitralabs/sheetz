package io.github.chitralabs.sheetz.style;

/**
 * Fluent builder for creating {@link CellStyleDef} instances programmatically.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * CellStyleDef style = CellStyleBuilder.create()
 *     .bold(true)
 *     .fontColor("#0000FF")
 *     .backgroundColor("#FFFF00")
 *     .horizontalAlignment("CENTER")
 *     .build();
 * }</pre>
 */
public final class CellStyleBuilder {

    private String fontName = "";
    private int fontSize = 0;
    private boolean bold = false;
    private boolean italic = false;
    private boolean underline = false;
    private boolean strikethrough = false;
    private String fontColor = "";
    private String backgroundColor = "";
    private String fillPattern = "";
    private String borderStyle = "";
    private String borderColor = "";
    private String horizontalAlignment = "";
    private String verticalAlignment = "";
    private boolean wrapText = false;
    private String dataFormat = "";
    private boolean hyperlink = false;
    private String hyperlinkType = "URL";

    private CellStyleBuilder() {}

    /** Creates a new builder with all defaults. */
    public static CellStyleBuilder create() {
        return new CellStyleBuilder();
    }

    public CellStyleBuilder fontName(String fontName) { this.fontName = fontName; return this; }
    public CellStyleBuilder fontSize(int fontSize) { this.fontSize = fontSize; return this; }
    public CellStyleBuilder bold(boolean bold) { this.bold = bold; return this; }
    public CellStyleBuilder italic(boolean italic) { this.italic = italic; return this; }
    public CellStyleBuilder underline(boolean underline) { this.underline = underline; return this; }
    public CellStyleBuilder strikethrough(boolean strikethrough) { this.strikethrough = strikethrough; return this; }
    public CellStyleBuilder fontColor(String fontColor) { this.fontColor = fontColor; return this; }
    public CellStyleBuilder backgroundColor(String backgroundColor) { this.backgroundColor = backgroundColor; return this; }
    public CellStyleBuilder fillPattern(String fillPattern) { this.fillPattern = fillPattern; return this; }
    public CellStyleBuilder borderStyle(String borderStyle) { this.borderStyle = borderStyle; return this; }
    public CellStyleBuilder borderColor(String borderColor) { this.borderColor = borderColor; return this; }
    public CellStyleBuilder horizontalAlignment(String horizontalAlignment) { this.horizontalAlignment = horizontalAlignment; return this; }
    public CellStyleBuilder verticalAlignment(String verticalAlignment) { this.verticalAlignment = verticalAlignment; return this; }
    public CellStyleBuilder wrapText(boolean wrapText) { this.wrapText = wrapText; return this; }
    public CellStyleBuilder dataFormat(String dataFormat) { this.dataFormat = dataFormat; return this; }
    public CellStyleBuilder hyperlink(boolean hyperlink) { this.hyperlink = hyperlink; return this; }
    public CellStyleBuilder hyperlinkType(String hyperlinkType) { this.hyperlinkType = hyperlinkType; return this; }

    /** Builds an immutable {@link CellStyleDef} from the current state. */
    public CellStyleDef build() {
        return new CellStyleDef(fontName, fontSize, bold, italic, underline, strikethrough,
            fontColor, backgroundColor, fillPattern, borderStyle, borderColor,
            horizontalAlignment, verticalAlignment, wrapText, dataFormat, hyperlink, hyperlinkType);
    }
}
