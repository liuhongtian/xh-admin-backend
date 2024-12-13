package com.xh.common.core.dao;

import com.xh.common.core.web.MyException;
import com.xh.common.core.web.PageQuery;
import com.xh.common.core.web.PageResult;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface BaseJdbcDao {
    <K> K findById(Class<K> clazz, Serializable id);

    <K> K findById(Class<K> clazz, JdbcTemplate jdbcTemplate, Serializable id);

    <K> K findBySql(Class<K> classname, String sql, Object... args) throws MyException;

    <K> K findBySql(Class<K> classname, String sql, JdbcTemplate jdbcTemplate, Object... args) throws MyException;

    <K> List<K> findList(Class<K> clazz, String sql, Object... args);

    <K> List<K> findList(Class<K> clazz, String sql, JdbcTemplate jdbcTemplate, Object... args);

    PageResult<Map> query(PageQuery<?> pageQuery);

    <K> PageResult<K> query(Class<K> clazz, PageQuery<?> pageQuery);

    <K> PageResult<K> query(Class<K> clazz, PageQuery<?> pageQuery, JdbcTemplate jdbcTemplate);

    <E> void insert(JdbcTemplate jdbcTemplate, E[] entities);

    <E> void insert(E[] entities);

    <E> void insert(E entity);

    <E> void insert(JdbcTemplate jdbcTemplate, E entity);

    <E> void update(E entity);

    <E> void update(JdbcTemplate jdbcTemplate, E entity);

    <E> void deleteById(Class<E> clazz, Serializable id);

    <E> void deleteById(Class<E> clazz, JdbcTemplate jdbcTemplate, Serializable id);
}
