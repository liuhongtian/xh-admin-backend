package com.xh.common.core.web;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.xh.common.core.utils.CommonUtil;
import com.xh.common.core.utils.WebLogs;
import lombok.Data;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 页面查询
 * sunxh 2023/3/16
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class PageQuery<T> {

    /**
     * 是否分页
     */
    private Boolean isPage = true;
    /**
     * 当前页
     */
    private int currentPage = 1;
    /**
     * 页面大小
     */
    private int pageSize = 20;
    /**
     * 可以携带自定义参数
     */
    private T param;
    /**
     * 高级组合查询
     */
    private List<FilterColumn> filters;

    /**
     * 查询基础sql
     */
    private String baseSql;
    /**
     * 查询sql的占位符参数
     */
    private LinkedList<Object> args = new LinkedList<>();

    //尾部添加参数
    public void addArg(Object ...arg) {
        this.args.addAll(Arrays.asList(arg));
    }

    //头部添加参数
    public void addFirst(Object ...arg) {
        for (int i = arg.length -1 ; i >= 0; i++) {
            this.args.addFirst(arg[i]);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class FilterColumn {
        private Boolean enabled;
        private String data;
        private String logic;
        private String field;
        private String alias;
        private ComparatorEnum condition;
        private Object value1;
        private Object value2;
        private List<FilterColumn> children;

        public FilterColumn() {
        }

        private Object getValue(int flag) {
            Object value = this.value1;
            if (flag == 2) {
                value = this.value2;
            }
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS Z", Locale.US);
            DateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if (CommonUtil.isEmpty(value)) return "null";
            if ("number".equals(data)) {
                return value;
            } else if ("date".equals(data) || "datetime".equals(data)) {
                Date date = null;
                try {
                    date = dateFormat.parse(CommonUtil.getString(value).replace("Z", " UTC"));
                } catch (ParseException e) {
                    WebLogs.error(e);
                    e.printStackTrace();
                }
                return "str_to_date('" + dateFormat2.format(date) + "', '%Y-%m-%d %H:%i:%s')";
            } else if ("month".equals(data)) {
                Date date = null;
                try {
                    date = dateFormat.parse(CommonUtil.getString(value).replace("Z", " UTC"));
                } catch (ParseException e) {
                    WebLogs.error(e);
                    e.printStackTrace();
                }
                return "date_format('" + dateFormat2.format(date) + "', '%Y-%m')";
            }
            return "'" + value + "'";
        }

        private String getInValues() {
            String[] values = CommonUtil.getString(this.value1).split(",");
            StringBuilder sb = new StringBuilder();
            if ("number".equals(data)) {
                for (String value : values) {
                    sb.append(",").append(value);
                }
            } else {
                for (String value : values) {
                    sb.append(",'").append(value).append("'");
                }
            }
            return sb.substring(1);
        }
    }


    public enum ComparatorEnum {
        /**
         * 等于
         */
        eq,
        /**
         * 不等于
         */
        ne,
        /**
         * 大于
         */
        gt,
        /**
         * 大于等于
         */
        ge,
        /**
         * 小于
         */
        lt,
        /**
         * 小于等于
         */
        le,
        /**
         * 含哪几个值
         */
        in,
        /**
         * not like
         */
        notcontains,
        /**
         * like
         */
        contains,
        /**
         * between ... and ...
         */
        bt
    }
}
