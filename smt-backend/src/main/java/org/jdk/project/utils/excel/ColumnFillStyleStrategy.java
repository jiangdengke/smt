package org.jdk.project.utils.excel;

import com.alibaba.excel.write.handler.RowWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import java.util.HashMap;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

/** Applies a fill color to specified columns. */
public class ColumnFillStyleStrategy implements RowWriteHandler {

  private final int[] columnIndices;
  private final short fillColorIndex;
  private final Map<CellStyle, CellStyle> styleCache = new HashMap<>();

  public ColumnFillStyleStrategy(short fillColorIndex, int... columnIndices) {
    this.columnIndices = columnIndices;
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
    Workbook workbook = row.getSheet().getWorkbook();
    for (int colIndex : columnIndices) {
      Cell cell = row.getCell(colIndex);
      if (cell == null) {
        cell = row.createCell(colIndex);
      }
      CellStyle original = cell.getCellStyle();
      CellStyle cached = styleCache.get(original);
      if (cached == null) {
        CellStyle style = workbook.createCellStyle();
        style.cloneStyleFrom(original);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFillForegroundColor(fillColorIndex);
        styleCache.put(original, style);
        cached = style;
      }
      cell.setCellStyle(cached);
    }
  }
}
