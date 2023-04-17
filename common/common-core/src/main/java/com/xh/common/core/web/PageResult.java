package com.xh.common.core.web;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 分页结果
 * sunxh
 * 2023-02-25
 */
@Setter
@Getter
@NoArgsConstructor
public class PageResult<T> implements Serializable {
    //列表数据
    private List<T> list = new ArrayList<>();

    //总数
    private Integer total = 0;

    //是否分页
    private Boolean isPage;

    //分页号
    private Integer currentPage;

    //分页大小
    private Integer pageSize;

    public PageResult(List<T> list, Integer total) {
        this.list = list;
        this.total = total;
    }
}
