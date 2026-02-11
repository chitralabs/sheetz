package io.github.chitralabs.sheetz.cache;

import io.github.chitralabs.sheetz.annotation.Column;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe, class-level cache that stores the result of reflecting
 * over a model class and building its {@link FieldMapping} list.
 *
 * <p>The cache uses a {@link java.util.concurrent.ConcurrentHashMap}
 * internally, so the first call to {@link #get(Class)} for a given
 * type performs the reflection work while every subsequent call returns
 * the previously computed {@link ClassMapping} without locking.</p>
 *
 * @see FieldMapping
 * @see ColumnResolver
 */
public final class MappingCache {
    private static final Map<Class<?>, ClassMapping> CACHE = new ConcurrentHashMap<>();
    private MappingCache() {}
    
    public static ClassMapping get(Class<?> type) { return CACHE.computeIfAbsent(type, MappingCache::createMapping); }
    public static void clear() { CACHE.clear(); }
    
    private static ClassMapping createMapping(Class<?> type) {
        List<FieldMapping> fields = new ArrayList<>();
        Class<?> current = type;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers()) || field.isSynthetic()) continue;
                Column ann = field.getAnnotation(Column.class);
                if (ann != null && ann.ignore()) continue;
                fields.add(FieldMapping.from(field));
            }
            current = current.getSuperclass();
        }
        return new ClassMapping(type, fields);
    }
    
    public static final class ClassMapping {
        private final Class<?> type;
        private final List<FieldMapping> fields;
        private final Map<String, FieldMapping> byName;
        
        ClassMapping(Class<?> type, List<FieldMapping> fields) {
            this.type = type;
            this.fields = Collections.unmodifiableList(new ArrayList<>(fields));
            Map<String, FieldMapping> map = new HashMap<>();
            for (FieldMapping f : fields) {
                map.put(f.headerName().toLowerCase(), f);
                map.put(f.field().getName().toLowerCase(), f);
            }
            this.byName = Collections.unmodifiableMap(map);
        }
        
        public Class<?> type() { return type; }
        public List<FieldMapping> fields() { return fields; }
        public FieldMapping findByName(String name) { return byName.get(name.toLowerCase()); }
        @SuppressWarnings("unchecked")
        public <T> T newInstance() throws ReflectiveOperationException { return (T) type.getDeclaredConstructor().newInstance(); }
    }
}
