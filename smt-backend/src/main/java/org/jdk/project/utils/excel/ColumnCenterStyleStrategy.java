package org.jdk.project.utils.excel;

import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;

/** Centers cell content for specified columns. */
public class ColumnCenterStyleStrategy implements CellWriteHandler {

  private final int[] columnIndices;
  private final Map<CellStyle, CellStyle> styleCache = new HashMap<>();

  public ColumnCenterStyleStrategy(int... columnIndices) {
    this.columnIndices = columnIndices;
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
    if (!isTargetColumn(cell.getColumnIndex())) {
      return;
    }
    CellStyle original = cell.getCellStyle();
    CellStyle cached = styleCache.get(original);
    if (cached == null) {
      Workbook workbook = cell.getSheet().getWorkbook();
      CellStyle style = workbook.createCellStyle();
      style.cloneStyleFrom(original);
      style.setAlignment(HorizontalAlignment.CENTER);
      style.setVerticalAlignment(VerticalAlignment.CENTER);
      styleCache.put(original, style);
      cached = style;
    }
    cell.setCellStyle(cached);
  }

  private boolean isTargetColumn(int colIndex) {
    for (int index : columnIndices) {
      if (index == colIndex) {
        return true;
      }
    }
    return false;
  }
}
