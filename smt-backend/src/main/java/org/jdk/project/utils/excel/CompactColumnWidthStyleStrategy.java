package org.jdk.project.utils.excel;

import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.style.column.AbstractColumnWidthStyleStrategy;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.usermodel.Cell;

/** Auto column width with tighter spacing than the default strategy. */
public class CompactColumnWidthStyleStrategy extends AbstractColumnWidthStyleStrategy {

  private static final int MAX_COLUMN_WIDTH = 255;

  private final Map<Integer, Map<Integer, Integer>> cache = new HashMap<>();
  private final int padding;

  public CompactColumnWidthStyleStrategy() {
    this(1);
  }

  public CompactColumnWidthStyleStrategy(int padding) {
    this.padding = Math.max(0, padding);
  }

  @Override
  protected void setColumnWidth(
      WriteSheetHolder writeSheetHolder,
      List<WriteCellData<?>> cellDataList,
      Cell cell,
      Head head,
      Integer relativeRowIndex,
      Boolean isHead) {
    boolean needSetWidth = Boolean.TRUE.equals(isHead) || !CollectionUtils.isEmpty(cellDataList);
    if (!needSetWidth) {
      return;
    }

    Integer length = dataLength(cellDataList, cell, isHead);
    if (length < 0) {
      return;
    }
    int width = Math.min(MAX_COLUMN_WIDTH, Math.max(1, length + padding));

    Map<Integer, Integer> maxColumnWidthMap =
        cache.computeIfAbsent(writeSheetHolder.getSheetNo(), key -> new HashMap<>(16));
    Integer maxColumnWidth = maxColumnWidthMap.get(cell.getColumnIndex());
    if (maxColumnWidth == null || width > maxColumnWidth) {
      maxColumnWidthMap.put(cell.getColumnIndex(), width);
      writeSheetHolder.getSheet().setColumnWidth(cell.getColumnIndex(), width * 256);
    }
  }

  private Integer dataLength(
      List<WriteCellData<?>> cellDataList, Cell cell, Boolean isHead) {
    if (Boolean.TRUE.equals(isHead)) {
      return displayLength(cell.getStringCellValue());
    }
    WriteCellData<?> cellData = cellDataList.get(0);
    CellDataTypeEnum type = cellData.getType();
    if (type == null) {
      return -1;
    }
    switch (type) {
      case STRING:
        return displayLength(cellData.getStringValue());
      case BOOLEAN:
        return displayLength(String.valueOf(cellData.getBooleanValue()));
      case NUMBER:
        BigDecimal numberValue = cellData.getNumberValue();
        return displayLength(numberValue == null ? "" : numberValue.toString());
      default:
        return -1;
    }
  }

  private int displayLength(String value) {
    if (value == null) {
      return 0;
    }
    int length = 0;
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      length += c <= 0xFF ? 1 : 2;
    }
    return length;
  }
}
