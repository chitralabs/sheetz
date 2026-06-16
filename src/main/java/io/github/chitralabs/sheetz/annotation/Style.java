package io.github.chitralabs.sheetz.annotation;

import java.lang.annotation.*;

/**
 * Annotation for customizing the visual style of a spreadsheet column.
 *
 * <p>While {@link Column} handles data mapping, {@code @Style} controls
 * visual presentation: fonts, colors, borders, alignment, and more.</p>
 *
 * <h2>Examples</h2>
 * <pre>
 * public class Product {
 *     &#64;Column("Product Name")
 *     &#64;Style(bold = true, fontColor = "#0000FF")
 *     public String name;
 *
 *     &#64;Style(backgroundColor = "#FFFF00", horizontalAlignment = "CENTER")
 *     public Double price;
 *
 *     &#64;Style(dataFormat = "#,##0.00")
 *     public BigDecimal amount;
 *
 *     &#64;Style(hyperlink = true)
 *     public String url;
 * }
 * </pre>
 *
 * @see Column
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Style {

    /** Font family name (e.g., "Arial", "Calibri"). */
    String fontName() default "";

    /** Font size in points (e.g., 12). 0 means default. */
    int fontSize() default 0;

    /** Whether the font is bold. */
    boolean bold() default false;

    /** Whether the font is italic. */
    boolean italic() default false;

    /** Whether the font is underlined. */
    boolean underline() default false;

    /** Whether the font has strikethrough. */
    boolean strikethrough() default false;

    /**
     * Font color as a hex RGB string (e.g., "#FF0000" for red).
     * Empty string means default color.
     */
    String fontColor() default "";

    /**
     * Background (fill) color as a hex RGB string (e.g., "#FFFF00" for yellow).
     * Empty string means no fill.
     */
    String backgroundColor() default "";

    /**
     * Fill pattern type name. Matches {@code org.apache.poi.ss.usermodel.FillPatternType}
     * enum names (e.g., "SOLID_FOREGROUND", "FINE_DOTS").
     * Default is "SOLID_FOREGROUND" when backgroundColor is set.
     */
    String fillPattern() default "";

    /**
     * Border style for all four sides. Matches {@code org.apache.poi.ss.usermodel.BorderStyle}
     * enum names (e.g., "THIN", "MEDIUM", "THICK", "DOUBLE").
     */
    String borderStyle() default "";

    /**
     * Border color as a hex RGB string. Applied to all four sides.
     */
    String borderColor() default "";

    /**
     * Horizontal alignment. Matches {@code org.apache.poi.ss.usermodel.HorizontalAlignment}
     * enum names (e.g., "CENTER", "LEFT", "RIGHT", "JUSTIFY").
     */
    String horizontalAlignment() default "";

    /**
     * Vertical alignment. Matches {@code org.apache.poi.ss.usermodel.VerticalAlignment}
     * enum names (e.g., "CENTER", "TOP", "BOTTOM").
     */
    String verticalAlignment() default "";

    /** Whether to enable text wrapping in the cell. */
    boolean wrapText() default false;

    /**
     * Excel data format string (e.g., "#,##0.00", "0%", "yyyy-mm-dd").
     * Applied via {@code DataFormat.getFormat()}.
     */
    String dataFormat() default "";

    /** Whether this field represents a hyperlink. */
    boolean hyperlink() default false;

    /**
     * Hyperlink type. Matches {@code org.apache.poi.common.usermodel.HyperlinkType}
     * enum names: "URL", "DOCUMENT", "EMAIL", "FILE".
     * Default is "URL".
     */
    String hyperlinkType() default "URL";
}
