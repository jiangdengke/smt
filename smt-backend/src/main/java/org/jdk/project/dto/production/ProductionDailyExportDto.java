package org.jdk.project.dto.production;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ContentStyle;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;
import com.alibaba.excel.enums.poi.HorizontalAlignmentEnum;
import com.alibaba.excel.enums.poi.VerticalAlignmentEnum;

@Data
public class ProductionDailyExportDto {

  @ExcelProperty("日期")
  @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER, verticalAlignment = VerticalAlignmentEnum.CENTER)
  @ColumnWidth(12)
  private LocalDate prodDate;

  @ExcelProperty("厂区")
  @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER, verticalAlignment = VerticalAlignmentEnum.CENTER)
  @ColumnWidth(12)
  private String factoryName;

  @ExcelProperty("车间")
  @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER, verticalAlignment = VerticalAlignmentEnum.CENTER)
  @ColumnWidth(12)
  private String workshopName;

  @ExcelProperty("线别")
  @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER, verticalAlignment = VerticalAlignmentEnum.CENTER)
  @ColumnWidth(12)
  private String lineName;

  @ExcelProperty("班别")
  @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER, verticalAlignment = VerticalAlignmentEnum.CENTER)
  @ColumnWidth(8)
  private String shift;

  @ExcelProperty("制程段")
  @ColumnWidth(15)
  private String processName;

  @ExcelProperty("机台号")
  @ColumnWidth(12)
  private String machineNo;

  @ExcelProperty("生产料号")
  @ColumnWidth(20)
  private String productCode;

  @ExcelProperty("系列")
  @ColumnWidth(12)
  private String seriesName;

  @ExcelProperty("CT")
  @ColumnWidth(10)
  private BigDecimal ct;

  @ExcelProperty("目前投入设备量")
  @ColumnWidth(15)
  private Integer equipmentCount;

  @ExcelProperty("投产时间(min)")
  @ColumnWidth(15)
  private Integer runMinutes;

  @ExcelProperty("目标产能(K)")
  @ColumnWidth(12)
  private Integer targetOutput;

  @ExcelProperty("实际产出")
  @ColumnWidth(12)
  private Integer actualOutput;

  @ExcelProperty("GAP")
  @ColumnWidth(10)
  private Integer gap;

  @ExcelProperty("达成率")
  @ColumnWidth(12)
  private String achievementRate; // Changed to String for % formatting

  @ExcelProperty("理论Down机时间(min)")
  @ColumnWidth(20)
  private Integer downMinutes;

  @ExcelProperty("FA")
  @ColumnWidth(30)
  private String fa;

  @ExcelProperty("CA")
  @ColumnWidth(30)
  private String ca;
}
