package com.xh.generator.client.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 表格列元数据 VO
 * sunxh 2024/11/22
 */
@Data
@Schema(title = "表格列元数据")
public class TableColumnMateDataVO {

    @Schema(title = "数据库名")
    private String tableCat;

    @Schema(title = "类型名称")
    private String typeName;

    @Schema(title = "表名")
    private String tableName;

    @Schema(title = "数据大小")
    private Long bufferLength;

    @Schema(title = "是否自增")
    private String isAutoincrement;

    @Schema(title = "字段长度")
    private Integer columnSize;

    @Schema(title = "小数点")
    private Integer decimalDigits;

    @Schema(title = "可为空")
    private String isNullable;

    @Schema(title = "字段注释")
    private String remarks;

    @Schema(title = "是否主键")
    private Boolean primaryKey;

    @Schema(title = "字段名称")
    private String columnName;

}
