package com.xh.common.core.dao;

import com.xh.common.core.entity.BaseEntity;
import com.xh.common.core.utils.CommonUtil;
import com.xh.common.core.utils.WebLogs;
import com.xh.common.core.web.PageQuery;
import com.xh.common.core.web.PageResult;
import jakarta.annotation.Resource;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Repository(value = "baseJdbcDao")
@SuppressWarnings("all")
public class BaseJdbcDaoImpl implements BaseJdbcDao {

    @Resource
    protected JdbcTemplate primaryJdbcTemplate;

    @Override
    public <K extends BaseEntity> K findById(Class<K> clazz, Serializable id) {
        return this.findById(clazz, primaryJdbcTemplate, id);
    }

    @Override
    public <K extends BaseEntity> K findById(Class<K> clazz, JdbcTemplate jdbcTemplate, Serializable id) {
        try {
            K entity = clazz.getDeclaredConstructor().newInstance();
            entity.setId(id);
            return findById(entity, jdbcTemplate);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <K extends BaseEntity> K findById(K entity) {
        return this.findById(entity, primaryJdbcTemplate);
    }

    @Override
    public <K extends BaseEntity> K findById(K entity, JdbcTemplate jdbcTemplate) {
        String sql = getSql(entity, "findById");
        SqlParameterSource params = new BeanPropertySqlParameterSource(entity);
        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(jdbcTemplate);
        return template.queryForObject(sql, params, new BeanPropertyRowMapper<K>((Class<K>) entity.getClass()));
    }

    @Override
    public <K> K findBySql(Class<K> classname, String sql, Object... args) throws RuntimeException {
        return findBySql(classname, sql, primaryJdbcTemplate, args);
    }

    @Override
    public <K> K findBySql(Class<K> classname, String sql, JdbcTemplate jdbcTemplate, Object... args) throws RuntimeException {
        WebLogs.sql(sql, args);
        K obj = null;
        List<K> list = null;
        if (Map.class.isAssignableFrom(classname)) {
            list = (List<K>) jdbcTemplate.queryForList(sql, args);
        } else {
            list = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(classname), args);
        }
        if (list.size() > 0) {
            obj = list.get(0);
        }
        return obj;
    }

    @Override
    public <K> List<K> findList(Class<K> clazz, String sql, Object... args) {
        return findList(clazz, sql, primaryJdbcTemplate, args);
    }

    @Override
    public <K> List<K> findList(Class<K> clazz, String sql, JdbcTemplate jdbcTemplate, Object... args) {
        WebLogs.sql(sql);
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<K>(clazz), args);
    }

    @Override
    public PageResult<Map> query(PageQuery pageQuery) {
        return query(Map.class, pageQuery);
    }

    @Override
    public <K> PageResult<K> query(Class<K> clazz, PageQuery pageQuery) {
        return query(clazz, pageQuery, primaryJdbcTemplate);
    }

    @Override
    public <K> PageResult<K> query(Class<K> clazz, PageQuery pageQuery, JdbcTemplate jdbcTemplate) {
        String sql = pageQuery.getBaseSql();
        PageResult<K> pageResult = new PageResult<>();
        if (pageQuery.getIsPage()) {
            String dbType = getDbType(jdbcTemplate);
            String pageSql = "";
            String totalSql = String.format("SELECT COUNT(1) FROM (%s) PAGE", sql);
            if ("MySQL".equals(dbType)) {
                // 构造mysql数据库的分页语句
                pageSql = String.format("SELECT * FROM (%s) PAGE LIMIT %s,%s", sql, (pageQuery.getCurrentPage() - 1) * pageQuery.getPageSize(), pageQuery.getPageSize());
            } else if ("Oracle".equals(dbType)) {
                // 构造oracle数据库的分页语句
                pageSql = """
                        SELECT * FROM (
                                SELECT temp.* ,ROWNUM ROW_NUM FROM (%s) temp WHERE ROWNUM <= %s
                            ) WHERE ROW_NUM > %s
                        """;
                pageSql = String.format(pageSql, sql, pageQuery.getCurrentPage() * pageQuery.getPageSize(), (pageQuery.getCurrentPage() - 1) * pageQuery.getPageSize());
            }
            Integer total = jdbcTemplate.queryForObject(totalSql, Integer.class, pageQuery.getArgs().toArray());
            List<K> list = findList(clazz, pageSql, jdbcTemplate, pageQuery.getArgs().toArray());
            pageResult.setList(list);
            pageResult.setTotal(total);
            pageResult.setIsPage(true);
            pageResult.setCurrentPage(pageQuery.getCurrentPage());
            pageResult.setPageSize(pageQuery.getPageSize());
        } else {
            List<K> list = findList(clazz, sql, jdbcTemplate, pageQuery.getArgs().toArray());
            pageResult = new PageResult<>(list, list.size());
        }
        return pageResult;
    }

    @Override
    public void insert(BaseEntity entity) {
        insert(entity, primaryJdbcTemplate);
    }

    @Override
    public void insert(BaseEntity entity, JdbcTemplate jdbcTemplate) {
        String sql = getSql(entity, "insert");
        WebLogs.sql(sql);
        SqlParameterSource params = new BeanPropertySqlParameterSource(entity);
        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
        GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
        int rowsAffected = template.update(sql, params, generatedKeyHolder);
        Integer id = generatedKeyHolder.getKey().intValue();
        entity.setId(id);
    }

    @Override
    public void update(BaseEntity entity) {
        update(entity, primaryJdbcTemplate);
    }

    @Override
    public void update(BaseEntity entity, JdbcTemplate jdbcTemplate) {
        String sql = getSql(entity, "update");
        SqlParameterSource params = new BeanPropertySqlParameterSource(entity);
        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(jdbcTemplate);
        int rowsAffected = template.update(sql, params);
    }

    @Override
    public void insertOrUpdate(BaseEntity entity) {
        insertOrUpdate(entity, primaryJdbcTemplate);
    }

    @Override
    public void insertOrUpdate(BaseEntity entity, JdbcTemplate jdbcTemplate) {
        if (CommonUtil.isNotEmpty(entity.getId())) {
            BaseEntity en = findById(entity, jdbcTemplate);
            if (en == null) insert(entity, jdbcTemplate);
            else update(entity, jdbcTemplate);
            return;
        }
        insert(entity, jdbcTemplate);
    }

    private String getSql(BaseEntity entity, String flag) {
        Class<? extends BaseEntity> clazz = entity.getClass();
        Table table = clazz.getAnnotation(Table.class);
        String tableName = table.name();
        if (CommonUtil.isEmpty(tableName)) tableName = CommonUtil.toLowerUnderscore(clazz.getSimpleName());
        Collection<Field> fields = CommonUtil.getAllFields(clazz);
        Map<String, String> columnMap = new LinkedHashMap<>();
        Map<String, String> idMap = new HashMap<>();
        for (Field field : fields) {
            String fieldName = field.getName();
            Transient ignoredField = field.getAnnotation(Transient.class);
            if (ignoredField == null) {
                Id id = field.getAnnotation(Id.class);
                Column column = field.getAnnotation(Column.class);
                String columnName = null;
                if (column != null) columnName = column.name();
                if (CommonUtil.isEmpty(columnName)) columnName = CommonUtil.toLowerUnderscore(fieldName);
                columnMap.put(columnName, fieldName);
                if (id != null) idMap.put(columnName, fieldName);
            }
        }
        String sql = null;
        if ("insert".equals(flag)) {
            if (entity.getCreateTime() == null) entity.setCreateTime(LocalDateTime.now());
            String formatStr = "INSERT INTO `%s` (%s) VALUES (%s)";
            String columnStr = columnMap.keySet().stream().collect(Collectors.joining("`,`", "`", "`"));
            String valueStr = columnMap.values().stream().map(i -> ":" + i).collect(Collectors.joining(","));
            sql = String.format(formatStr, tableName, columnStr, valueStr);
        } else if ("update".equals(flag)) {
            entity.setUpdateTime(LocalDateTime.now());
            String formatStr = "UPDATE `%s` SET %s WHERE %s";
            String columnMapStr = columnMap.entrySet().stream().map(i -> String.format("`%s`=:%s", i.getKey(), i.getValue())).collect(Collectors.joining(","));
            String idMapStr = idMap.entrySet().stream().map(i -> String.format("`%s`=:%s", i.getKey(), i.getValue())).collect(Collectors.joining(" and "));
            sql = String.format(formatStr, tableName, columnMapStr, idMapStr);
        } else if ("findById".equals(flag)) {
            String formatStr = "select %s FROM `%s` WHERE %s";
            String columnMapStr = columnMap.entrySet().stream().map(i -> String.format("`%s` as `%s`", i.getKey(), i.getValue())).collect(Collectors.joining(","));
            String idMapStr = idMap.entrySet().stream().map(i -> String.format("`%s`=:%s", i.getKey(), i.getValue())).collect(Collectors.joining(" and "));
            sql = String.format(formatStr, columnMapStr, tableName, idMapStr);
        }
        return sql;
    }

    /**
     * 获取数据库类型
     */
    protected String getDbType(JdbcTemplate jdbcTemplate) {
        String dbType;
        try (Connection connection = Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection()) {
            dbType = connection.getMetaData().getDatabaseProductName();
            return dbType;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
