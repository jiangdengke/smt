package org.jdk.project.utils.excel;

import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 * Merge vertical cells by grouping columns.
 *
 * <p>Cells in merge columns will be merged only when all grouping columns have the same
 * values as the previous row.
 */
public class GroupColMergeStrategy implements CellWriteHandler {

  private final int[] mergeColIndices;
  private final int[] groupColIndices;
  private final int mergeRowIndex;
  private final int maxMergeColIndex;
  private final Integer skipMarkerColIndex;
  private final String skipMarkerToken;

  public GroupColMergeStrategy(int mergeRowIndex, int[] mergeColIndices, int[] groupColIndices) {
    this(mergeRowIndex, mergeColIndices, groupColIndices, null, null);
  }

  public GroupColMergeStrategy(
      int mergeRowIndex,
      int[] mergeColIndices,
      int[] groupColIndices,
      Integer skipMarkerColIndex,
      String skipMarkerToken) {
    this.mergeRowIndex = mergeRowIndex;
    this.mergeColIndices = mergeColIndices;
    this.groupColIndices = groupColIndices;
    this.maxMergeColIndex = findMaxIndex(mergeColIndices);
    this.skipMarkerColIndex = skipMarkerColIndex;
    this.skipMarkerToken = skipMarkerToken;
  }

  @Override
  public void afterCellDispose(
      WriteSheetHolder writeSheetHolder,
      WriteTableHolder writeTableHolder,
      List<WriteCellData<?>> cellDataList,
      Cell cell,
      Head head,
      Integer relativeRowIndex,
      Boolean isHead) {

    if (isHead) {
      return;
    }

    int curRowIndex = cell.getRowIndex();
    int curColIndex = cell.getColumnIndex();

    if (curColIndex != maxMergeColIndex) {
      return;
    }

    if (curRowIndex <= mergeRowIndex) {
      return;
    }

    Sheet sheet = writeSheetHolder.getSheet();
    Row preRow = sheet.getRow(curRowIndex - 1);
    if (preRow == null) {
      return;
    }

    if (shouldSkipRow(sheet, curRowIndex) || shouldSkipRow(sheet, curRowIndex - 1)) {
      return;
    }

    if (!groupColumnsMatch(sheet, curRowIndex)) {
      return;
    }

    for (int mergeColIndex : mergeColIndices) {
      Cell curCell = sheet.getRow(curRowIndex).getCell(mergeColIndex);
      Cell preCell = preRow.getCell(mergeColIndex);
      if (!getCellValue(curCell).equals(getCellValue(preCell))) {
        continue;
      }
      List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();
      boolean merged = false;
      for (int i = 0; i < mergedRegions.size(); i++) {
        CellRangeAddress region = mergedRegions.get(i);
        if (region.isInRange(curRowIndex - 1, mergeColIndex)) {
          sheet.removeMergedRegion(i);
          region.setLastRow(curRowIndex);
          sheet.addMergedRegion(region);
          merged = true;
          break;
        }
      }
      if (!merged) {
        CellRangeAddress region =
            new CellRangeAddress(curRowIndex - 1, curRowIndex, mergeColIndex, mergeColIndex);
        sheet.addMergedRegion(region);
      }
    }
  }

  private boolean groupColumnsMatch(Sheet sheet, int curRowIndex) {
    for (int colIndex : groupColIndices) {
      Cell curCell = sheet.getRow(curRowIndex).getCell(colIndex);
      Cell preCell = sheet.getRow(curRowIndex - 1).getCell(colIndex);
      if (!getCellValue(curCell).equals(getCellValue(preCell))) {
        return false;
      }
    }
    return true;
  }

  private Object getCellValue(Cell cell) {
    if (cell == null) {
      return "";
    }
    if (cell.getCellType() == CellType.STRING) {
      return cell.getStringCellValue();
    }
    if (cell.getCellType() == CellType.NUMERIC) {
      return cell.getNumericCellValue();
    }
    if (cell.getCellType() == CellType.BOOLEAN) {
      return cell.getBooleanCellValue();
    }
    return "";
  }

  private int findMaxIndex(int[] indices) {
    int max = -1;
    for (int index : indices) {
      if (index > max) {
        max = index;
      }
    }
    return max;
  }

  private boolean shouldSkipRow(Sheet sheet, int rowIndex) {
    if (skipMarkerColIndex == null || skipMarkerToken == null) {
      return false;
    }
    Row row = sheet.getRow(rowIndex);
    if (row == null) {
      return false;
    }
    Cell cell = row.getCell(skipMarkerColIndex);
    String value = String.valueOf(getCellValue(cell));
    return value.contains(skipMarkerToken);
  }
}
