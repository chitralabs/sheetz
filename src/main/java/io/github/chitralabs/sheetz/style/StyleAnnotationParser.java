package io.github.chitralabs.sheetz.style;

import io.github.chitralabs.sheetz.annotation.Style;

/**
 * Converts a {@link Style} annotation into a {@link CellStyleDef}.
 */
public final class StyleAnnotationParser {

    private StyleAnnotationParser() {}

    /**
     * Parses a {@link Style} annotation into a {@link CellStyleDef}.
     *
     * @param style the annotation (may be null)
     * @return the parsed style definition, or null if annotation is null
     */
    public static CellStyleDef parse(Style style) {
        if (style == null) return null;
        CellStyleDef def = new CellStyleDef(
            style.fontName(),
            style.fontSize(),
            style.bold(),
            style.italic(),
            style.underline(),
            style.strikethrough(),
            style.fontColor(),
            style.backgroundColor(),
            style.fillPattern(),
            style.borderStyle(),
            style.borderColor(),
            style.horizontalAlignment(),
            style.verticalAlignment(),
            style.wrapText(),
            style.dataFormat(),
            style.hyperlink(),
            style.hyperlinkType()
        );
        return def.isEmpty() ? null : def;
    }
}
