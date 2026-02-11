package io.github.chitralabs.sheetz.annotation;

import io.github.chitralabs.sheetz.convert.Converter;
import java.lang.annotation.*;

/**
 * Annotation for customizing how a field is mapped to a spreadsheet column.
 * 
 * <p>This annotation can be used to:</p>
 * <ul>
 *   <li>Map to a different column header name</li>
 *   <li>Map by explicit column index</li>
 *   <li>Mark fields as required</li>
 *   <li>Provide default values for empty cells</li>
 *   <li>Specify custom date formats</li>
 *   <li>Use custom converters</li>
 *   <li>Ignore fields during mapping</li>
 *   <li>Set column width for writing</li>
 * </ul>
 * 
 * <h2>Examples</h2>
 * <pre>{@code
 * public class Product {
 *     @Column("Product Name")  // Map to different header
 *     public String name;
 *     
 *     @Column(index = 1)  // Map by column index (0-based)
 *     public Double price;
 *     
 *     @Column(required = true)  // Fail validation if empty
 *     public String sku;
 *     
 *     @Column(defaultValue = "pending")  // Default for empty cells
 *     public String status;
 *     
 *     @Column(format = "dd/MM/yyyy")  // Custom date format
 *     public LocalDate orderDate;
 *     
 *     @Column(converter = MoneyConverter.class)  // Custom converter
 *     public BigDecimal amount;
 *     
 *     @Column(ignore = true)  // Skip this field
 *     public String internalId;
 *     
 *     @Column(width = 20)  // Column width in characters
 *     public String description;
 * }
 * }</pre>
 * 
 * @see Converter
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Column {
    /**
     * The column header name to map to.
     * If empty, the field name is used (with fuzzy matching).
     * 
     * @return the column header name
     */
    String value() default "";
    
    /**
     * The explicit column index (0-based).
     * If set to a non-negative value, this takes precedence over header matching.
     * 
     * @return the column index, or -1 to use header matching
     */
    int index() default -1;
    
    /**
     * Date/time format pattern for parsing and formatting.
     * Uses {@link java.time.format.DateTimeFormatter} patterns.
     * 
     * @return the format pattern
     */
    String format() default "";
    
    /**
     * Custom converter class for this field.
     * Must have a no-arg constructor.
     * 
     * @return the converter class
     */
    @SuppressWarnings("rawtypes") Class<? extends Converter> converter() default Converter.None.class;
    
    /**
     * Whether this field is required (cannot be empty).
     * If true and the cell is empty, validation will fail.
     * 
     * @return true if required
     */
    boolean required() default false;
    
    /**
     * Default value to use when the cell is empty.
     * The string is converted using the field's converter.
     * 
     * @return the default value as a string
     */
    String defaultValue() default "";
    
    /**
     * Whether to ignore this field during mapping.
     * Ignored fields are not read from or written to the spreadsheet.
     * 
     * @return true to ignore
     */
    boolean ignore() default false;
    
    /**
     * Column width in characters (for writing only).
     * If positive, the column is set to this width.
     * 
     * @return the width in characters, or 0 for auto
     */
    int width() default 0;
}
