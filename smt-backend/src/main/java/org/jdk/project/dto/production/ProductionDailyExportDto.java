package org.jdk.project.dto.production;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ContentStyle;
import java.math.BigDecimal;
import lombok.Data;
import com.alibaba.excel.enums.poi.HorizontalAlignmentEnum;
import com.alibaba.excel.enums.poi.VerticalAlignmentEnum;

@Data
public class ProductionDailyExportDto {

  @ExcelProperty("日期")
  @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER, verticalAlignment = VerticalAlignmentEnum.CENTER)
  private String prodDate;

  @ExcelProperty("厂区")
  @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER, verticalAlignment = VerticalAlignmentEnum.CENTER)
  private String factoryName;

  @ExcelProperty("车间")
  @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER, verticalAlignment = VerticalAlignmentEnum.CENTER)
  private String workshopName;

  @ExcelProperty("线别")
  @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER, verticalAlignment = VerticalAlignmentEnum.CENTER)
  private String lineName;

  @ExcelProperty("班别")
  @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER, verticalAlignment = VerticalAlignmentEnum.CENTER)
  private String shift;

  @ExcelProperty("制程段")
  private String processName;

  @ExcelProperty("机台号")
  private String machineNo;

  @ExcelProperty("生产料号")
  private String productCode;

  @ExcelProperty("系列")
  private String seriesName;

  @ExcelProperty("CT")
  private BigDecimal ct;

  @ExcelProperty("目前投入设备量")
  private Integer equipmentCount;

  @ExcelProperty("投产时间(min)")
  private Integer runMinutes;

  @ExcelProperty("目标产能(K)")
  private Integer targetOutput;

  @ExcelProperty("实际产出")
  private Integer actualOutput;

  @ExcelProperty("GAP")
  private Integer gap;

  @ExcelProperty("达成率")
  private String achievementRate; // Changed to String for % formatting

  @ExcelProperty("理论Down机时间(min)")
  private Integer downMinutes;

  @ExcelProperty("FA")
  private String fa;

  @ExcelProperty("CA")
  private String ca;
}
