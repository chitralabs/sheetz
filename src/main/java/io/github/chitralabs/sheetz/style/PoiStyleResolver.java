package io.github.chitralabs.sheetz.style;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;

import java.util.HashMap;
import java.util.Map;

/**
 * Converts {@link CellStyleDef} into POI {@link CellStyle} objects with
 * per-workbook caching to avoid exceeding POI's 64K style limit.
 *
 * <p>Each workbook should have its own {@code PoiStyleResolver} instance.
 * Styles are cached by their {@link CellStyleDef}, so identical definitions
 * reuse the same POI style object.</p>
 */
public final class PoiStyleResolver {

    private final Workbook workbook;
    private final Map<CellStyleDef, CellStyle> cache = new HashMap<>();

    public PoiStyleResolver(Workbook workbook) {
        this.workbook = workbook;
    }

    /**
     * Resolves a {@link CellStyleDef} to a POI {@link CellStyle}.
     * Returns null if the definition is null.
     *
     * @param def the style definition
     * @return the resolved POI cell style, or null
     */
    public CellStyle resolve(CellStyleDef def) {
        if (def == null) return null;
        return cache.computeIfAbsent(def, this::createStyle);
    }

    private CellStyle createStyle(CellStyleDef def) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        boolean fontModified = false;

        if (def.fontName() != null && !def.fontName().isEmpty()) {
            font.setFontName(def.fontName());
            fontModified = true;
        }
        if (def.fontSize() > 0) {
            font.setFontHeightInPoints((short) def.fontSize());
            fontModified = true;
        }
        if (def.bold()) {
            font.setBold(true);
            fontModified = true;
        }
        if (def.italic()) {
            font.setItalic(true);
            fontModified = true;
        }
        if (def.underline()) {
            font.setUnderline(Font.U_SINGLE);
            fontModified = true;
        }
        if (def.strikethrough()) {
            font.setStrikeout(true);
            fontModified = true;
        }
        if (def.fontColor() != null && !def.fontColor().isEmpty()) {
            applyFontColor(font, def.fontColor());
            fontModified = true;
        }
        if (fontModified) {
            style.setFont(font);
        }

        if (def.backgroundColor() != null && !def.backgroundColor().isEmpty()) {
            applyBackgroundColor(style, def.backgroundColor(), def.fillPattern());
        } else if (def.fillPattern() != null && !def.fillPattern().isEmpty()) {
            style.setFillPattern(FillPatternType.valueOf(def.fillPattern()));
        }

        if (def.borderStyle() != null && !def.borderStyle().isEmpty()) {
            BorderStyle bs = BorderStyle.valueOf(def.borderStyle());
            style.setBorderTop(bs);
            style.setBorderBottom(bs);
            style.setBorderLeft(bs);
            style.setBorderRight(bs);
        }
        if (def.borderColor() != null && !def.borderColor().isEmpty()) {
            applyBorderColor(style, def.borderColor());
        }

        if (def.horizontalAlignment() != null && !def.horizontalAlignment().isEmpty()) {
            style.setAlignment(HorizontalAlignment.valueOf(def.horizontalAlignment()));
        }
        if (def.verticalAlignment() != null && !def.verticalAlignment().isEmpty()) {
            style.setVerticalAlignment(VerticalAlignment.valueOf(def.verticalAlignment()));
        }

        if (def.wrapText()) {
            style.setWrapText(true);
        }

        if (def.dataFormat() != null && !def.dataFormat().isEmpty()) {
            style.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat(def.dataFormat()));
        }

        return style;
    }

    private void applyFontColor(Font font, String hexColor) {
        byte[] rgb = parseHexColor(hexColor);
        if (font instanceof XSSFFont) {
            ((XSSFFont) font).setColor(new XSSFColor(rgb, null));
        } else {
            font.setColor(IndexedColors.BLACK.getIndex());
        }
    }

    private void applyBackgroundColor(CellStyle style, String hexColor, String pattern) {
        byte[] rgb = parseHexColor(hexColor);
        FillPatternType fillType = (pattern != null && !pattern.isEmpty())
            ? FillPatternType.valueOf(pattern)
            : FillPatternType.SOLID_FOREGROUND;
        style.setFillPattern(fillType);
        if (style instanceof XSSFCellStyle) {
            ((XSSFCellStyle) style).setFillForegroundColor(new XSSFColor(rgb, null));
        } else {
            style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        }
    }

    private void applyBorderColor(CellStyle style, String hexColor) {
        byte[] rgb = parseHexColor(hexColor);
        if (style instanceof XSSFCellStyle) {
            XSSFColor color = new XSSFColor(rgb, null);
            ((XSSFCellStyle) style).setTopBorderColor(color);
            ((XSSFCellStyle) style).setBottomBorderColor(color);
            ((XSSFCellStyle) style).setLeftBorderColor(color);
            ((XSSFCellStyle) style).setRightBorderColor(color);
        }
    }

    static byte[] parseHexColor(String hex) {
        String h = hex.startsWith("#") ? hex.substring(1) : hex;
        return new byte[] {
            (byte) Integer.parseInt(h.substring(0, 2), 16),
            (byte) Integer.parseInt(h.substring(2, 4), 16),
            (byte) Integer.parseInt(h.substring(4, 6), 16)
        };
    }
}
