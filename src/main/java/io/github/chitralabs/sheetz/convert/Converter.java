package io.github.chitralabs.sheetz.convert;

public interface Converter<T> {
    T fromCell(Object value, ConvertContext ctx);
    Object toCell(T value);
    
    final class None implements Converter<Object> {
        private None() {}
        @Override public Object fromCell(Object v, ConvertContext c) { throw new UnsupportedOperationException(); }
        @Override public Object toCell(Object v) { throw new UnsupportedOperationException(); }
    }
}
