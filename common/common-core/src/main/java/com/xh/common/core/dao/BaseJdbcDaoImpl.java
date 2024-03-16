package com.xh.common.core.dao;

import com.xh.common.core.entity.AutoSet;
import com.xh.common.core.utils.CommonUtil;
import com.xh.common.core.utils.WebLogs;
import com.xh.common.core.web.PageQuery;
import com.xh.common.core.web.PageResult;
import jakarta.annotation.Resource;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
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
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository(value = "baseJdbcDao")
public class BaseJdbcDaoImpl implements BaseJdbcDao {

    @Resource
    protected JdbcTemplate primaryJdbcTemplate;

    @Override
    public <K> K findById(Class<K> clazz, Serializable id) {
        return this.findById(clazz, primaryJdbcTemplate, id);
    }

    @Override
    public <K> K findById(Class<K> clazz, JdbcTemplate jdbcTemplate, Serializable id) {
        try {
            K entity = clazz.getDeclaredConstructor().newInstance();
            SqlStaff staff = getSql(entity, PersistenceType.FIND_BY_ID);
            staff.setId(id);
            return findById(entity, jdbcTemplate);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <K> K findById(K entity) {
        return this.findById(entity, primaryJdbcTemplate);
    }

    @Override
    public <K> K findById(K entity, JdbcTemplate jdbcTemplate) {
        SqlStaff staff = getSql(entity, PersistenceType.FIND_BY_ID);
        SqlParameterSource params = new BeanPropertySqlParameterSource(staff.getSqlArgs());
        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(jdbcTemplate);
        return template.queryForObject(staff.getSql(), params, new BeanPropertyRowMapper<K>((Class<K>) entity.getClass()));
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
    public PageResult<Map> query(PageQuery<?> pageQuery) {
        return query(Map.class, pageQuery);
    }

    @Override
    public <K> PageResult<K> query(Class<K> clazz, PageQuery<?> pageQuery) {
        return query(clazz, pageQuery, primaryJdbcTemplate);
    }

    @Override
    public <K> PageResult<K> query(Class<K> clazz, PageQuery<?> pageQuery, JdbcTemplate jdbcTemplate) {
        String sql = pageQuery.getSql();
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
    public void insert(Collection<Object> entities) {
        entities.forEach(this::insert);
    }

    @Override
    public void insert(Object entity) {
        insert(entity, primaryJdbcTemplate);
    }

    @Override
    public void insert(Object entity, JdbcTemplate jdbcTemplate) {
        SqlStaff staff = getSql(entity, PersistenceType.INSERT);
        WebLogs.sql(staff.getSql());
        SqlParameterSource params = new BeanPropertySqlParameterSource(entity);
        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(jdbcTemplate);
        GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
        template.update(staff.getSql(), params, generatedKeyHolder);
        Integer key = generatedKeyHolder.getKey().intValue();
        staff.setId(key);
    }

    @Override
    public void update(Object entity) {
        update(entity, primaryJdbcTemplate);
    }

    @Override
    public void update(Object entity, JdbcTemplate jdbcTemplate) {
        SqlStaff staff = getSql(entity, PersistenceType.UPDATE);
        SqlParameterSource params = new BeanPropertySqlParameterSource(entity);
        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(jdbcTemplate);
        int rowsAffected = template.update(staff.getSql(), params);
    }

    private SqlStaff getSql(Object entity, PersistenceType persistenceType) {
        Class<?> clazz = entity.getClass();
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

                // 自动注入值
                AutoSet autoSet = field.getAnnotation(AutoSet.class);
                if (autoSet != null) {
                    AutoSetFun[] autoSetFuns = autoSet.value();
                    for (AutoSetFun autoSetFun : autoSetFuns) {
                        autoSetFun.fun.exec(persistenceType, field, entity);
                    }
                }
            }
        }
        String sql = null;
        if (persistenceType == PersistenceType.INSERT) {
            String columnStr = columnMap.keySet().stream().collect(Collectors.joining("`,`", "`", "`"));
            String valueStr = columnMap.values().stream().map(i -> ":" + i).collect(Collectors.joining(","));
            sql = "INSERT INTO `%s` (%s) VALUES (%s)".formatted(tableName, columnStr, valueStr);
        } else if (persistenceType == PersistenceType.UPDATE) {
            String columnMapStr = columnMap.entrySet().stream().map(i -> String.format("`%s`=:%s", i.getKey(), i.getValue())).collect(Collectors.joining(","));
            String idMapStr = idMap.entrySet().stream().map(i -> String.format("`%s`=:%s", i.getKey(), i.getValue())).collect(Collectors.joining(" and "));
            sql = "UPDATE `%s` SET %s WHERE %s".formatted(tableName, columnMapStr, idMapStr);
        } else if (persistenceType == PersistenceType.FIND_BY_ID) {
            String columnMapStr = columnMap.entrySet().stream().map(i -> String.format("`%s` as `%s`", i.getKey(), i.getValue())).collect(Collectors.joining(","));
            String idMapStr = idMap.entrySet().stream().map(i -> String.format("`%s`=:%s", i.getKey(), i.getValue())).collect(Collectors.joining(" and "));
            sql = "select %s FROM `%s` WHERE %s".formatted(columnMapStr, tableName, idMapStr);
        }

        SqlStaff sqlStaff = new SqlStaff();
        sqlStaff.setEntity(entity);
        sqlStaff.setClazz(clazz);
        sqlStaff.setTable(tableName);
        sqlStaff.setColumnMap(columnMap);
        sqlStaff.setIdMap(idMap);
        sqlStaff.setPersistenceType(persistenceType);
        sqlStaff.setSql(sql);
        sqlStaff.setSqlArgs(entity);

        return sqlStaff;
    }

    /**
     * 获取数据库类型
     */
    private static String dbType = null;

    protected String getDbType(JdbcTemplate jdbcTemplate) {
        if (dbType == null) {
            try (Connection connection = Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection()) {
                dbType = connection.getMetaData().getDatabaseProductName();
                return dbType;
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }
        return dbType;
    }


    @Data
    static class SqlStaff {
        private Object entity;

        private Class<?> clazz;

        private String table;

        private PersistenceType persistenceType;

        private Map<String, String> idMap;

        private Map<String, String> columnMap;

        private String sql;

        private Object sqlArgs;

        //设置主键值
        public void setId(Object id) {
            for (String fieldName : this.idMap.values()) {
                Field field = CommonUtil.getField(this.clazz, fieldName);
                field.setAccessible(true);
                try {
                    if (field.get(this.entity) == null)
                        field.set(this.entity, id);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
