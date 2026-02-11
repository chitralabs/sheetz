package io.github.chitralabs.sheetz.cache;

import io.github.chitralabs.sheetz.annotation.Column;
import org.junit.jupiter.api.*;
import java.util.*;
import java.util.stream.Collectors;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for MappingCache.
 */
class MappingCacheTest {

    @BeforeEach void setUp() { MappingCache.clear(); }

    @Test void testBasicMapping() {
        MappingCache.ClassMapping mapping = MappingCache.get(SimpleBean.class);
        assertThat(mapping.type()).isEqualTo(SimpleBean.class);
        assertThat(mapping.fields()).hasSize(2);
        assertThat(mapping.fields().get(0).headerName()).isEqualTo("name");
        assertThat(mapping.fields().get(1).headerName()).isEqualTo("value");
    }

    @Test void testCachingReturnsSameInstance() {
        MappingCache.ClassMapping m1 = MappingCache.get(SimpleBean.class);
        MappingCache.ClassMapping m2 = MappingCache.get(SimpleBean.class);
        assertThat(m1).isSameAs(m2);
    }

    @Test void testClearInvalidatesCache() {
        MappingCache.ClassMapping m1 = MappingCache.get(SimpleBean.class);
        MappingCache.clear();
        MappingCache.ClassMapping m2 = MappingCache.get(SimpleBean.class);
        assertThat(m1).isNotSameAs(m2);
    }

    @Test void testStaticFieldsExcluded() {
        MappingCache.ClassMapping mapping = MappingCache.get(StaticFieldBean.class);
        assertThat(mapping.fields()).hasSize(1);
        assertThat(mapping.fields().get(0).headerName()).isEqualTo("instance");
    }

    @Test void testTransientFieldsExcluded() {
        MappingCache.ClassMapping mapping = MappingCache.get(TransientFieldBean.class);
        assertThat(mapping.fields()).hasSize(1);
        assertThat(mapping.fields().get(0).headerName()).isEqualTo("persistent");
    }

    @Test void testIgnoredFieldsExcluded() {
        MappingCache.ClassMapping mapping = MappingCache.get(IgnoredFieldBean.class);
        assertThat(mapping.fields()).hasSize(1);
        assertThat(mapping.fields().get(0).headerName()).isEqualTo("visible");
    }

    @Test void testInheritance() {
        MappingCache.ClassMapping mapping = MappingCache.get(ChildBean.class);
        List<String> fieldNames = mapping.fields().stream().map(f -> f.field().getName()).collect(Collectors.toList());
        assertThat(fieldNames).contains("childField", "name", "value");
    }

    @Test void testAnnotatedHeaderName() {
        MappingCache.ClassMapping mapping = MappingCache.get(AnnotatedBean.class);
        FieldMapping fm = mapping.fields().get(0);
        assertThat(fm.headerName()).isEqualTo("Display Name");
    }

    @Test void testFindByName() {
        MappingCache.ClassMapping mapping = MappingCache.get(SimpleBean.class);
        assertThat(mapping.findByName("name")).isNotNull();
        assertThat(mapping.findByName("NAME")).isNotNull(); // case insensitive
        assertThat(mapping.findByName("unknown")).isNull();
    }

    @Test void testNewInstance() throws Exception {
        MappingCache.ClassMapping mapping = MappingCache.get(SimpleBean.class);
        SimpleBean instance = mapping.newInstance();
        assertThat(instance).isNotNull();
        assertThat(instance).isInstanceOf(SimpleBean.class);
    }

    @Test void testFieldsAreUnmodifiable() {
        MappingCache.ClassMapping mapping = MappingCache.get(SimpleBean.class);
        assertThatThrownBy(() -> mapping.fields().add(null)).isInstanceOf(UnsupportedOperationException.class);
    }

    // Test model classes
    public static class SimpleBean { public String name; public int value; }
    public static class StaticFieldBean { public static String STATIC = "x"; public String instance; }
    public static class TransientFieldBean { public transient String temp; public String persistent; }
    public static class IgnoredFieldBean { @Column(ignore = true) public String hidden; public String visible; }
    public static class ChildBean extends SimpleBean { public String childField; }
    public static class AnnotatedBean { @Column("Display Name") public String name; }
}
