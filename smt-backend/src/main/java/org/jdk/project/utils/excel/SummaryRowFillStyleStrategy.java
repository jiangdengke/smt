package org.jdk.project.utils.excel;

import com.alibaba.excel.write.handler.RowWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import java.util.HashMap;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/** Applies a fill color to summary rows identified by a marker token. */
public class SummaryRowFillStyleStrategy implements RowWriteHandler {

  private final int markerColumnIndex;
  private final String markerToken;
  private final int startColIndex;
  private final int endColIndex;
  private final int mergeRowIndex;
  private final short fillColorIndex;
  private final Map<CellStyle, CellStyle> styleCache = new HashMap<>();

  public SummaryRowFillStyleStrategy(
      int mergeRowIndex,
      int markerColumnIndex,
      String markerToken,
      int startColIndex,
      int endColIndex,
      short fillColorIndex) {
    this.mergeRowIndex = mergeRowIndex;
    this.markerColumnIndex = markerColumnIndex;
    this.markerToken = markerToken;
    this.startColIndex = startColIndex;
    this.endColIndex = endColIndex;
    this.fillColorIndex = fillColorIndex;
  }

  @Override
  public void afterRowDispose(
      WriteSheetHolder writeSheetHolder,
      WriteTableHolder writeTableHolder,
      Row row,
      Integer relativeRowIndex,
      Boolean isHead) {
    if (Boolean.TRUE.equals(isHead) || row == null) {
      return;
    }

    int rowIndex = row.getRowNum();
    if (rowIndex <= mergeRowIndex) {
      return;
    }

    Cell markerCell = row.getCell(markerColumnIndex);
    if (markerCell == null) {
      return;
    }
    String markerValue = String.valueOf(getCellValue(markerCell));
    if (!markerValue.contains(markerToken)) {
      return;
    }

    Sheet sheet = writeSheetHolder.getSheet();
    for (int col = startColIndex; col <= endColIndex; col++) {
      Cell targetCell = row.getCell(col);
      if (targetCell == null) {
        targetCell = row.createCell(col);
      }
      CellStyle original = targetCell.getCellStyle();
      CellStyle cached = styleCache.get(original);
      if (cached == null) {
        Workbook workbook = sheet.getWorkbook();
        CellStyle style = workbook.createCellStyle();
        style.cloneStyleFrom(original);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFillForegroundColor(fillColorIndex);
        styleCache.put(original, style);
        cached = style;
      }
      targetCell.setCellStyle(cached);
    }
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
}
