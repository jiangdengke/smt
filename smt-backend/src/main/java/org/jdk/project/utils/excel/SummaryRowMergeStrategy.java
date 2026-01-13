package org.jdk.project.utils.excel;

import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

/** Merges a summary row horizontally when the marker cell contains the token. */
public class SummaryRowMergeStrategy implements CellWriteHandler {

  private final int markerColumnIndex;
  private final String markerToken;
  private final int startColIndex;
  private final int endColIndex;
  private final int mergeRowIndex;

  public SummaryRowMergeStrategy(
      int mergeRowIndex,
      int markerColumnIndex,
      String markerToken,
      int startColIndex,
      int endColIndex) {
    this.mergeRowIndex = mergeRowIndex;
    this.markerColumnIndex = markerColumnIndex;
    this.markerToken = markerToken;
    this.startColIndex = startColIndex;
    this.endColIndex = endColIndex;
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

    int rowIndex = cell.getRowIndex();
    int colIndex = cell.getColumnIndex();
    if (rowIndex <= mergeRowIndex || colIndex != markerColumnIndex) {
      return;
    }

    String markerValue = cell.getStringCellValue();
    if (markerValue == null || !markerValue.contains(markerToken)) {
      return;
    }

    Sheet sheet = writeSheetHolder.getSheet();
    CellRangeAddress region = new CellRangeAddress(rowIndex, rowIndex, startColIndex, endColIndex);
    if (isMergedRegionExists(sheet, region)) {
      return;
    }
    sheet.addMergedRegion(region);
  }

  private boolean isMergedRegionExists(Sheet sheet, CellRangeAddress target) {
    List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();
    for (CellRangeAddress region : mergedRegions) {
      if (region.getFirstRow() == target.getFirstRow()
          && region.getLastRow() == target.getLastRow()
          && region.getFirstColumn() == target.getFirstColumn()
          && region.getLastColumn() == target.getLastColumn()) {
        return true;
      }
    }
    return false;
  }
}
