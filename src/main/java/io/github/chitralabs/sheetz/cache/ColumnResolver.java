package io.github.chitralabs.sheetz.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Resolves field names to column indices using a three-tier matching
 * strategy: exact match, case-insensitive match, and normalised match
 * (lower-case with whitespace, underscores, and hyphens stripped).
 *
 * <p>This tiered approach lets users define model fields like
 * {@code productName} and still match headers such as
 * {@code Product Name}, {@code product_name}, or {@code PRODUCTNAME}
 * without any annotation.</p>
 *
 * @see io.github.chitralabs.sheetz.cache.MappingCache
 */
public final class ColumnResolver {
    private final List<String> headers;
    private final Map<String, Integer> exactMatch;
    private final Map<String, Integer> lowerMatch;
    private final Map<String, Integer> normalizedMatch;
    
    public ColumnResolver(List<String> headers) {
        this.headers = headers;
        this.exactMatch = new HashMap<>();
        this.lowerMatch = new HashMap<>();
        this.normalizedMatch = new HashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            String h = headers.get(i);
            if (h != null) {
                exactMatch.putIfAbsent(h, i);
                lowerMatch.putIfAbsent(h.toLowerCase(), i);
                normalizedMatch.putIfAbsent(normalize(h), i);
            }
        }
    }
    
    public int resolve(String name) {
        if (name == null) return -1;
        Integer idx = exactMatch.get(name);
        if (idx != null) return idx;
        idx = lowerMatch.get(name.toLowerCase());
        if (idx != null) return idx;
        idx = normalizedMatch.get(normalize(name));
        return idx != null ? idx : -1;
    }
    
    public String headerAt(int index) { return (index >= 0 && index < headers.size()) ? headers.get(index) : null; }
    public int size() { return headers.size(); }
    private static String normalize(String s) { return s.toLowerCase().replaceAll("[\\s_\\-]+", ""); }
}
