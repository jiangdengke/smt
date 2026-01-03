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
 * Column Merge Strategy. Merges vertical cells in specified columns if they have the same value.
 */
public class ColMergeStrategy implements CellWriteHandler {

  private final int[] mergeColIndices;
  private final int mergeRowIndex;

  public ColMergeStrategy(int mergeRowIndex, int... mergeColIndices) {
    this.mergeRowIndex = mergeRowIndex;
    this.mergeColIndices = mergeColIndices;
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

    // Only process body rows
    if (isHead) {
      return;
    }

    int curRowIndex = cell.getRowIndex();
    int curColIndex = cell.getColumnIndex();

    // Check if this column is one we should merge
    if (!isMergeCol(curColIndex)) {
      return;
    }

    // Only start merging from the specified row index (e.g. skip headers)
    if (curRowIndex <= mergeRowIndex) {
      return;
    }

    Object curData =
        cell.getCellType() == CellType.STRING
            ? cell.getStringCellValue()
            : cell.getCellType() == CellType.NUMERIC ? cell.getNumericCellValue() : "";

    // Get previous row's cell data for comparison
    Sheet sheet = writeSheetHolder.getSheet();
    Row preRow = sheet.getRow(curRowIndex - 1);
    Cell preCell = preRow.getCell(curColIndex);

    Object preData =
        preCell.getCellType() == CellType.STRING
            ? preCell.getStringCellValue()
            : preCell.getCellType() == CellType.NUMERIC ? preCell.getNumericCellValue() : "";

    // Compare
    if (curData.equals(preData)) {
      // If values match, we might need to merge.
      // But we can only merge if the *previous* merge is already established or if we are extending
      // it.
      // Actually, standard logic is:
      // If current == previous, extend the merge region of the previous cell.
      // BUT, we also need to ensure that the *parent* columns (left of this one) are also
      // merged/identical.
      // e.g. If Date changes, Shift should not merge even if Shift is "Day" for both.

      boolean parentColumnsMatch = true;
      for (int i = 0; i < curColIndex; i++) {
        // Check if parent columns are identical
        Cell pCell = sheet.getRow(curRowIndex).getCell(i);
        Cell pPreCell = sheet.getRow(curRowIndex - 1).getCell(i);
        Object pVal = pCell.getCellType() == CellType.STRING ? pCell.getStringCellValue() : "";
        Object pPreVal =
            pPreCell.getCellType() == CellType.STRING ? pPreCell.getStringCellValue() : "";
        if (!pVal.equals(pPreVal)) {
          parentColumnsMatch = false;
          break;
        }
      }

      if (parentColumnsMatch) {
        List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();
        boolean merged = false;
        for (int i = 0; i < mergedRegions.size(); i++) {
          CellRangeAddress cellRangeAddress = mergedRegions.get(i);
          if (cellRangeAddress.isInRange(curRowIndex - 1, curColIndex)) {
            // Extend existing merge
            sheet.removeMergedRegion(i);
            cellRangeAddress.setLastRow(curRowIndex);
            sheet.addMergedRegion(cellRangeAddress);
            merged = true;
            break;
          }
        }
        if (!merged) {
          // Create new merge
          CellRangeAddress cellRangeAddress =
              new CellRangeAddress(curRowIndex - 1, curRowIndex, curColIndex, curColIndex);
          sheet.addMergedRegion(cellRangeAddress);
        }
      }
    }
  }

  private boolean isMergeCol(int colIndex) {
    for (int index : mergeColIndices) {
      if (index == colIndex) {
        return true;
      }
    }
    return false;
  }
}
