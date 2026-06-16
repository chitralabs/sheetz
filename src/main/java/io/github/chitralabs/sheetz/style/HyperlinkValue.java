package io.github.chitralabs.sheetz.style;

import java.util.Objects;

/**
 * Value type holding both display text and a URL for hyperlink-aware fields.
 *
 * <p>When a field of this type is encountered during writing, the cell
 * will contain the display text with a clickable hyperlink to the URL.</p>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * public class Product {
 *     public String name;
 *     public HyperlinkValue website;
 * }
 *
 * product.website = new HyperlinkValue("Acme Corp", "https://acme.example.com");
 * }</pre>
 */
public final class HyperlinkValue {

    private final String displayText;
    private final String url;

    public HyperlinkValue(String displayText, String url) {
        this.displayText = Objects.requireNonNull(displayText, "displayText cannot be null");
        this.url = Objects.requireNonNull(url, "url cannot be null");
    }

    /** The text displayed in the cell. */
    public String displayText() { return displayText; }

    /** The hyperlink URL. */
    public String url() { return url; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HyperlinkValue)) return false;
        HyperlinkValue that = (HyperlinkValue) o;
        return displayText.equals(that.displayText) && url.equals(that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(displayText, url);
    }

    @Override
    public String toString() {
        return displayText + " <" + url + ">";
    }
}
