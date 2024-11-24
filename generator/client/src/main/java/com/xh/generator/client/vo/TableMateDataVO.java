package com.xh.generator.client.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 数据库表元数据 VO
 * sunxh 2024/11/22
 */
@Data
@Schema(title = "数据库表元数据")
public class TableMateDataVO {

    @Schema(title = "类型")
    private String tableType;

    @Schema(title = "数据库名")
    private String tableCat;

    @Schema(title = "表注释")
    private String remarks;

    @Schema(title = "表名")
    private String tableName;

    @Schema(title = "数据库表列明细")
    List<TableColumnMateDataVO> columns;
}
