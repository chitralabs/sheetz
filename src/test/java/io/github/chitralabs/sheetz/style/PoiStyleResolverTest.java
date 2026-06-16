package io.github.chitralabs.sheetz.style;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PoiStyleResolverTest {

    private Workbook workbook;
    private PoiStyleResolver resolver;

    @BeforeEach
    void setUp() {
        workbook = new XSSFWorkbook();
        resolver = new PoiStyleResolver(workbook);
    }

    @AfterEach
    void tearDown() throws Exception {
        workbook.close();
    }

    @Test
    void nullDef_returnsNull() {
        assertThat(resolver.resolve(null)).isNull();
    }

    @Test
    void boldStyle_createsCorrectFont() {
        CellStyleDef def = CellStyleBuilder.create().bold(true).build();

        CellStyle style = resolver.resolve(def);

        assertThat(style).isNotNull();
        Font font = workbook.getFontAt(style.getFontIndex());
        assertThat(font.getBold()).isTrue();
    }

    @Test
    void italicStyle_createsCorrectFont() {
        CellStyleDef def = CellStyleBuilder.create().italic(true).build();

        CellStyle style = resolver.resolve(def);

        Font font = workbook.getFontAt(style.getFontIndex());
        assertThat(font.getItalic()).isTrue();
    }

    @Test
    void fontName_setsCorrectly() {
        CellStyleDef def = CellStyleBuilder.create().fontName("Courier New").build();

        CellStyle style = resolver.resolve(def);

        Font font = workbook.getFontAt(style.getFontIndex());
        assertThat(font.getFontName()).isEqualTo("Courier New");
    }

    @Test
    void fontSize_setsCorrectly() {
        CellStyleDef def = CellStyleBuilder.create().fontSize(16).build();

        CellStyle style = resolver.resolve(def);

        Font font = workbook.getFontAt(style.getFontIndex());
        assertThat(font.getFontHeightInPoints()).isEqualTo((short) 16);
    }

    @Test
    void backgroundColor_setsCorrectly() {
        CellStyleDef def = CellStyleBuilder.create().backgroundColor("#FF0000").build();

        CellStyle style = resolver.resolve(def);

        assertThat(style.getFillPattern()).isEqualTo(FillPatternType.SOLID_FOREGROUND);
        XSSFCellStyle xStyle = (XSSFCellStyle) style;
        XSSFColor color = xStyle.getFillForegroundXSSFColor();
        assertThat(color).isNotNull();
    }

    @Test
    void borderStyle_setsAllFourSides() {
        CellStyleDef def = CellStyleBuilder.create().borderStyle("THIN").build();

        CellStyle style = resolver.resolve(def);

        assertThat(style.getBorderTop()).isEqualTo(BorderStyle.THIN);
        assertThat(style.getBorderBottom()).isEqualTo(BorderStyle.THIN);
        assertThat(style.getBorderLeft()).isEqualTo(BorderStyle.THIN);
        assertThat(style.getBorderRight()).isEqualTo(BorderStyle.THIN);
    }

    @Test
    void alignment_setsCorrectly() {
        CellStyleDef def = CellStyleBuilder.create()
            .horizontalAlignment("CENTER")
            .verticalAlignment("TOP")
            .build();

        CellStyle style = resolver.resolve(def);

        assertThat(style.getAlignment()).isEqualTo(HorizontalAlignment.CENTER);
        assertThat(style.getVerticalAlignment()).isEqualTo(VerticalAlignment.TOP);
    }

    @Test
    void wrapText_setsCorrectly() {
        CellStyleDef def = CellStyleBuilder.create().wrapText(true).build();

        CellStyle style = resolver.resolve(def);

        assertThat(style.getWrapText()).isTrue();
    }

    @Test
    void dataFormat_setsCorrectly() {
        CellStyleDef def = CellStyleBuilder.create().dataFormat("#,##0.00").build();

        CellStyle style = resolver.resolve(def);

        String formatStr = workbook.createDataFormat().getFormat(style.getDataFormat());
        assertThat(formatStr).isEqualTo("#,##0.00");
    }

    @Test
    void sameDefResolvedTwice_returnsCachedInstance() {
        CellStyleDef def = CellStyleBuilder.create().bold(true).build();

        CellStyle first = resolver.resolve(def);
        CellStyle second = resolver.resolve(def);

        assertThat(first).isSameAs(second);
    }

    @Test
    void parseHexColor_parsesCorrectly() {
        byte[] rgb = PoiStyleResolver.parseHexColor("#FF8000");
        assertThat(rgb[0]).isEqualTo((byte) 0xFF);
        assertThat(rgb[1]).isEqualTo((byte) 0x80);
        assertThat(rgb[2]).isEqualTo((byte) 0x00);
    }

    @Test
    void parseHexColor_withoutHash_parsesCorrectly() {
        byte[] rgb = PoiStyleResolver.parseHexColor("00FF00");
        assertThat(rgb[0]).isEqualTo((byte) 0x00);
        assertThat(rgb[1]).isEqualTo((byte) 0xFF);
        assertThat(rgb[2]).isEqualTo((byte) 0x00);
    }
}
