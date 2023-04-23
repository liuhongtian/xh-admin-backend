package com.xh.common.core.dao;

import com.xh.common.core.entity.BaseEntity;
import com.xh.common.core.web.MyException;
import com.xh.common.core.web.PageQuery;
import com.xh.common.core.web.PageResult;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@SuppressWarnings("all")
public interface BaseJdbcDao {
    <K extends BaseEntity> K findById(Class<K> clazz, Serializable id);

    <K extends BaseEntity> K findById(Class<K> clazz, JdbcTemplate jdbcTemplate, Serializable id);

    <K extends BaseEntity> K findById(K entity);

    <K extends BaseEntity> K findById(K entity, JdbcTemplate jdbcTemplate);

    <K> K findBySql(Class<K> classname, String sql, Object... args) throws MyException;

    <K> K findBySql(Class<K> classname, String sql, JdbcTemplate jdbcTemplate, Object... args) throws MyException;

    <K> List<K> findList(Class<K> clazz, String sql, Object... args);

    <K> List<K> findList(Class<K> clazz, String sql, JdbcTemplate jdbcTemplate, Object... args);

    PageResult<Map> query(PageQuery<?> pageQuery);

    <K> PageResult<K> query(Class<K> clazz, PageQuery<?> pageQuery);

    <K> PageResult<K> query(Class<K> clazz, PageQuery<?> pageQuery, JdbcTemplate jdbcTemplate);

    void insert(BaseEntity entity);

    void insert(BaseEntity entity, JdbcTemplate jdbcTemplate);

    void update(BaseEntity entity);

    void update(BaseEntity entity, JdbcTemplate jdbcTemplate);

    void insertOrUpdate(BaseEntity entity);

    void insertOrUpdate(BaseEntity entity, JdbcTemplate jdbcTemplate);
}
