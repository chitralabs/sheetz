package io.github.chitralabs.sheetz.style;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CellStyleBuilderTest {

    @Test
    void defaultBuilder_createsEmptyStyle() {
        CellStyleDef def = CellStyleBuilder.create().build();

        assertThat(def.isEmpty()).isTrue();
        assertThat(def.bold()).isFalse();
        assertThat(def.italic()).isFalse();
        assertThat(def.fontSize()).isZero();
        assertThat(def.fontName()).isEmpty();
    }

    @Test
    void builder_setsAllProperties() {
        CellStyleDef def = CellStyleBuilder.create()
            .fontName("Arial")
            .fontSize(14)
            .bold(true)
            .italic(true)
            .underline(true)
            .strikethrough(true)
            .fontColor("#FF0000")
            .backgroundColor("#00FF00")
            .fillPattern("SOLID_FOREGROUND")
            .borderStyle("THIN")
            .borderColor("#0000FF")
            .horizontalAlignment("CENTER")
            .verticalAlignment("TOP")
            .wrapText(true)
            .dataFormat("#,##0.00")
            .hyperlink(true)
            .hyperlinkType("URL")
            .build();

        assertThat(def.isEmpty()).isFalse();
        assertThat(def.fontName()).isEqualTo("Arial");
        assertThat(def.fontSize()).isEqualTo(14);
        assertThat(def.bold()).isTrue();
        assertThat(def.italic()).isTrue();
        assertThat(def.underline()).isTrue();
        assertThat(def.strikethrough()).isTrue();
        assertThat(def.fontColor()).isEqualTo("#FF0000");
        assertThat(def.backgroundColor()).isEqualTo("#00FF00");
        assertThat(def.fillPattern()).isEqualTo("SOLID_FOREGROUND");
        assertThat(def.borderStyle()).isEqualTo("THIN");
        assertThat(def.borderColor()).isEqualTo("#0000FF");
        assertThat(def.horizontalAlignment()).isEqualTo("CENTER");
        assertThat(def.verticalAlignment()).isEqualTo("TOP");
        assertThat(def.wrapText()).isTrue();
        assertThat(def.dataFormat()).isEqualTo("#,##0.00");
        assertThat(def.hyperlink()).isTrue();
        assertThat(def.hyperlinkType()).isEqualTo("URL");
    }

    @Test
    void equalStyles_areEqual() {
        CellStyleDef a = CellStyleBuilder.create().bold(true).fontColor("#FF0000").build();
        CellStyleDef b = CellStyleBuilder.create().bold(true).fontColor("#FF0000").build();

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void differentStyles_areNotEqual() {
        CellStyleDef a = CellStyleBuilder.create().bold(true).build();
        CellStyleDef b = CellStyleBuilder.create().italic(true).build();

        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void boldOnly_isNotEmpty() {
        CellStyleDef def = CellStyleBuilder.create().bold(true).build();

        assertThat(def.isEmpty()).isFalse();
    }

    @Test
    void toString_containsSetProperties() {
        CellStyleDef def = CellStyleBuilder.create().bold(true).fontColor("#FF0000").build();

        assertThat(def.toString()).contains("bold");
        assertThat(def.toString()).contains("#FF0000");
    }
}
