package io.github.chitralabs.sheetz.cache;

import io.github.chitralabs.sheetz.annotation.Column;
import io.github.chitralabs.sheetz.convert.*;
import io.github.chitralabs.sheetz.exception.SheetzException;
import org.junit.jupiter.api.*;
import java.lang.reflect.Field;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for FieldMapping.
 */
class FieldMappingTest {

    @Test void testPlainField() throws Exception {
        Field field = PlainBean.class.getDeclaredField("name");
        FieldMapping fm = FieldMapping.from(field);
        assertThat(fm.headerName()).isEqualTo("name");
        assertThat(fm.columnIndex()).isEqualTo(-1);
        assertThat(fm.required()).isFalse();
        assertThat(fm.hasCustomConverter()).isFalse();
        assertThat(fm.hasDefaultValue()).isFalse();
        assertThat(fm.hasExplicitIndex()).isFalse();
        assertThat(fm.type()).isEqualTo(String.class);
    }

    @Test void testAnnotatedField() throws Exception {
        Field field = AnnotatedBean.class.getDeclaredField("value");
        FieldMapping fm = FieldMapping.from(field);
        assertThat(fm.headerName()).isEqualTo("Custom Name");
        assertThat(fm.columnIndex()).isEqualTo(3);
        assertThat(fm.required()).isTrue();
        assertThat(fm.format()).isEqualTo("dd/MM/yyyy");
        assertThat(fm.width()).isEqualTo(20);
        assertThat(fm.hasExplicitIndex()).isTrue();
    }

    @Test void testDefaultValue() throws Exception {
        Field field = DefaultBean.class.getDeclaredField("status");
        FieldMapping fm = FieldMapping.from(field);
        assertThat(fm.hasDefaultValue()).isTrue();
        assertThat(fm.defaultValue()).isEqualTo("pending");
    }

    @Test void testGetSetValue() throws Exception {
        Field field = PlainBean.class.getDeclaredField("name");
        FieldMapping fm = FieldMapping.from(field);
        PlainBean obj = new PlainBean();
        fm.setValue(obj, "test");
        assertThat(fm.getValue(obj)).isEqualTo("test");
    }

    @Test void testPrivateFieldAccess() throws Exception {
        Field field = PrivateBean.class.getDeclaredField("secret");
        FieldMapping fm = FieldMapping.from(field);
        PrivateBean obj = new PrivateBean();
        fm.setValue(obj, "hidden");
        assertThat(fm.getValue(obj)).isEqualTo("hidden");
    }

    @Test void testCustomConverter() throws Exception {
        Field field = ConverterBean.class.getDeclaredField("amount");
        FieldMapping fm = FieldMapping.from(field);
        assertThat(fm.hasCustomConverter()).isTrue();
        assertThat(fm.converter()).isNotNull();
    }

    @Test void testBadConverterThrows() {
        assertThatThrownBy(() -> {
            Field field = BadConverterBean.class.getDeclaredField("val");
            FieldMapping.from(field);
        }).isInstanceOf(SheetzException.class)
          .hasMessageContaining("Failed to instantiate converter");
    }

    @Test void testEmptyAnnotationUsesFieldName() throws Exception {
        Field field = EmptyAnnotationBean.class.getDeclaredField("myField");
        FieldMapping fm = FieldMapping.from(field);
        assertThat(fm.headerName()).isEqualTo("myField");
    }

    // Test model classes
    public static class PlainBean { public String name; }
    public static class AnnotatedBean {
        @Column(value = "Custom Name", index = 3, required = true, format = "dd/MM/yyyy", width = 20)
        public String value;
    }
    public static class DefaultBean { @Column(defaultValue = "pending") public String status; }
    public static class PrivateBean { private String secret; }
    public static class ConverterBean {
        @Column(converter = TestConverter.class) public String amount;
    }
    public static class TestConverter implements Converter<String> {
        public String fromCell(Object v, ConvertContext ctx) { return v.toString(); }
        public Object toCell(String v) { return v; }
    }
    public static class BadConverterBean {
        @Column(converter = NoArgConstructorConverter.class) public String val;
    }
    // This converter has no accessible no-arg constructor
    static class NoArgConstructorConverter implements Converter<String> {
        NoArgConstructorConverter(int dummy) {}
        public String fromCell(Object v, ConvertContext ctx) { return null; }
        public Object toCell(String v) { return null; }
    }
    public static class EmptyAnnotationBean { @Column public String myField; }
}
