package org.jdk.project.dto.repair;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class RepairRecordExportDto {

  @ExcelProperty("发生时间")
  @ColumnWidth(20)
  private LocalDateTime occurAt;

  @ExcelProperty("班次")
  @ColumnWidth(10)
  private String shift;

  @ExcelProperty("厂区")
  @ColumnWidth(15)
  private String factoryName;

  @ExcelProperty("车间")
  @ColumnWidth(15)
  private String workshopName;

  @ExcelProperty("线别")
  @ColumnWidth(15)
  private String lineName;

  @ExcelProperty("机型")
  @ColumnWidth(15)
  private String modelName;

  @ExcelProperty("机台号")
  @ColumnWidth(15)
  private String machineNo;

  @ExcelProperty("异常类别")
  @ColumnWidth(15)
  private String abnormalCategoryName;

  @ExcelProperty("异常分类")
  @ColumnWidth(15)
  private String abnormalTypeName;

  @ExcelProperty("异常描述")
  @ColumnWidth(30)
  private String abnormalDesc;

  @ExcelProperty("解决对策")
  @ColumnWidth(30)
  private String solution;

  @ExcelProperty("是否修复")
  @ColumnWidth(10)
  private String isFixed;

  @ExcelProperty("修复时间")
  @ColumnWidth(20)
  private LocalDateTime fixedAt;

  @ExcelProperty("维修耗时(分)")
  @ColumnWidth(15)
  private Integer repairMinutes;

  @ExcelProperty("组别")
  @ColumnWidth(15)
  private String teamName;

  @ExcelProperty("责任人")
  @ColumnWidth(15)
  private String responsiblePersonName;

  @ExcelProperty("维修人")
  @ColumnWidth(20)
  private String repairPersonNames;
}
