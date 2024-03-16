package com.xh.common.core.dao;

import com.xh.common.core.web.MyException;
import com.xh.common.core.web.PageQuery;
import com.xh.common.core.web.PageResult;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface BaseJdbcDao {
    <K> K findById(Class<K> clazz, Serializable id);

    <K> K findById(Class<K> clazz, JdbcTemplate jdbcTemplate, Serializable id);

    <K> K findById(K entity);

    <K> K findById(K entity, JdbcTemplate jdbcTemplate);

    <K> K findBySql(Class<K> classname, String sql, Object... args) throws MyException;

    <K> K findBySql(Class<K> classname, String sql, JdbcTemplate jdbcTemplate, Object... args) throws MyException;

    <K> List<K> findList(Class<K> clazz, String sql, Object... args);

    <K> List<K> findList(Class<K> clazz, String sql, JdbcTemplate jdbcTemplate, Object... args);

    PageResult<Map> query(PageQuery<?> pageQuery);

    <K> PageResult<K> query(Class<K> clazz, PageQuery<?> pageQuery);

    <K> PageResult<K> query(Class<K> clazz, PageQuery<?> pageQuery, JdbcTemplate jdbcTemplate);

    void insert(Collection<Object> entities);

    void insert(Object entity);

    void insert(Object entity, JdbcTemplate jdbcTemplate);

    void update(Object entity);

    void update(Object entity, JdbcTemplate jdbcTemplate);
}
