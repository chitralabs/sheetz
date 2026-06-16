package io.github.chitralabs.sheetz.reader;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.HashMap;
import java.util.Map;

/**
 * Pre-computes (row, col) to master cell lookup for O(1) merged cell reads.
 *
 * <p>When a cell belongs to a merged region, its value is stored only in the
 * top-left (master) cell. This resolver maps any cell coordinate within a
 * merged region back to its master cell so readers can resolve values
 * correctly.</p>
 */
final class MergedRegionResolver {

    private final Sheet sheet;
    private final Map<Long, long[]> mergedMap;

    MergedRegionResolver(Sheet sheet) {
        this.sheet = sheet;
        this.mergedMap = buildMap(sheet);
    }

    /**
     * Returns the value of the master cell for the given row and column.
     * If the cell is not part of a merged region, returns the cell's own value.
     *
     * @param row the 0-based row index
     * @param col the 0-based column index
     * @return the cell value from the master cell, or from the cell itself
     */
    Object resolve(int row, int col) {
        long key = key(row, col);
        long[] master = mergedMap.get(key);
        if (master != null) {
            Row masterRow = sheet.getRow((int) master[0]);
            if (masterRow == null) return null;
            Cell masterCell = masterRow.getCell((int) master[1]);
            return ExcelReader.getCellValue(masterCell);
        }
        Row r = sheet.getRow(row);
        if (r == null) return null;
        return ExcelReader.getCellValue(r.getCell(col));
    }

    /**
     * Returns true if the given cell is part of a merged region but is not
     * the master (top-left) cell.
     */
    boolean isMergedNonMaster(int row, int col) {
        return mergedMap.containsKey(key(row, col));
    }

    private static Map<Long, long[]> buildMap(Sheet sheet) {
        Map<Long, long[]> map = new HashMap<>();
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress region = sheet.getMergedRegion(i);
            int masterRow = region.getFirstRow();
            int masterCol = region.getFirstColumn();
            for (int r = region.getFirstRow(); r <= region.getLastRow(); r++) {
                for (int c = region.getFirstColumn(); c <= region.getLastColumn(); c++) {
                    if (r != masterRow || c != masterCol) {
                        map.put(key(r, c), new long[] { masterRow, masterCol });
                    }
                }
            }
        }
        return map;
    }

    private static long key(int row, int col) {
        return ((long) row << 32) | (col & 0xFFFFFFFFL);
    }
}
