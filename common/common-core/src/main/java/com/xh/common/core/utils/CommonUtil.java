package com.xh.common.core.utils;

import com.google.common.base.CaseFormat;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * 通用工具类
 * sunxh 2023/4/16
 */
public class CommonUtil {
    /**
     * 获取字符串，null返回空串
     */
    public static String getString(Object object) {
        return object == null ? "" : object.toString();
    }

    /**
     * 判断不为null且不为为空串
     */
    public static boolean isNotEmpty(Object object) {
        return !isEmpty(object);
    }

    /**
     * 判断为null或者为空串
     */
    public static boolean isEmpty(Object object) {
        return object == null || object.toString().length() == 0;
    }

    /**
     * 驼峰转小写下划线
     */
    public static String toLowerUnderscore(String str) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, str);
    }

    /**
     * 驼峰转小写下划线
     */
    public static String toUpperUnderscore(String str) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, str);
    }

    /**
     * 下划线转大写驼峰
     */
    public static String toLowerCamel(String str) {
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, str);
    }

    /**
     * 下划线转大写驼峰
     */
    public static String toUpperCamel(String str) {
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, str);
    }

    /**
     * 获取所有的field包括父级继承的，子类会覆盖父类的field
     */
    public static Collection<Field> getAllFields(Class<?> clazz) {
        if (clazz != null) {
            LinkedList<Class<?>> list = new LinkedList<>();

            Class<?> currentClass = clazz;
            while (currentClass != null) {
                list.addFirst(currentClass);
                currentClass = currentClass.getSuperclass();
            }
            Map<String, Field> fieldMap = new LinkedHashMap<>();
            for (Class<?> aClass : list) {
                Field[] declaredFields = aClass.getDeclaredFields();
                for (Field declaredField : declaredFields) {
                    fieldMap.put(declaredField.getName(), declaredField);
                }
            }
            return fieldMap.values();
        }
        return null;
    }

    /**
     * 获取文件的后缀名
     */
    public static String getFileSuffix(String filename) {
        if (filename == null) return null;
        int index = filename.lastIndexOf(".");
        if (index == -1) return null;
        return filename.substring(index + 1);
    }

    /**
     * 获取文件摘要sha1
     */
    public static String getFileSha1(InputStream inputStream) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] buffer = new byte[1024 * 1024 * 10];
            int len = 0;
            while ((len = inputStream.read(buffer)) > 0) {
                digest.update(buffer, 0, len);
            }
            String sha1 = new BigInteger(1, digest.digest()).toString(16);
            int length = 40 - sha1.length();
            if (length > 0) {
                for (int i = 0; i < length; i++) {
                    sha1 = "0" + sha1;
                }
            }
            return sha1;
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
