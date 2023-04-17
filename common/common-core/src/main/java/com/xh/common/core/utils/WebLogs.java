package com.xh.common.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LocationAwareLogger;
import org.springframework.lang.Nullable;

import java.util.Map;

/**
 * 修改日志输出，打印日志的输出位置 方便日志查看
 *
 * @author sunxh
 */
public class WebLogs {

    final static String FQCN = WebLogs.class.getName();
    final static Logger logger = LoggerFactory.getLogger("WebLogs");
    final static String LOG_PREFIX = "$$: ";


    private WebLogs() {
        super();
    }

    public static Logger getLogger() {
        return logger;
    }

    private static void log(Object msg, int levint, Throwable throwable) {
        LocationAwareLogger logger2 = (LocationAwareLogger) logger;
        logger2.log(null, FQCN, levint, msg.toString(), new Object[]{}, throwable);
    }

    public static void debug(Object logcontent) {
        log(logcontent, LocationAwareLogger.DEBUG_INT, null);

    }

    public static void error(Object logcontent) {
        log(logcontent, LocationAwareLogger.ERROR_INT, null);
    }

    public static void warn(Object logcontent) {
        log(logcontent, LocationAwareLogger.WARN_INT, null);
    }

    public static void info(Object logcontent) {
        log(logcontent, LocationAwareLogger.INFO_INT, null);
    }

    public static void sql(String sql, @Nullable Object... args) {
        log("print sql :" + sql, LocationAwareLogger.INFO_INT, null);
        if (args != null) {
            String params = "";
            for (Object arg : args) {
                if (!"".equals(params)) {
                    params += ", ";
                }
                if (arg == null) {
                    params += "null";
                } else {
                    params += arg.getClass().getName() + ": " + arg;
                }
            }
            if (!CommonUtil.isEmpty(params)) {
                log("sql params : [" + params + "]", LocationAwareLogger.INFO_INT, null);
            }
        }
    }


    public static void printStackTrace(Throwable e) {
        log("", LocationAwareLogger.ERROR_INT, e);
    }

    public static void error(Exception e) {
        log("", LocationAwareLogger.ERROR_INT, e);
    }

    public static void error(String msg, Throwable e) {
        log(msg, LocationAwareLogger.ERROR_INT, e);
    }

    public static void info(String mes, Map<String, Object> sqlmap) {
        WebLogs.info(mes);
        StringBuilder sqlcsmes = new StringBuilder();
        for (Map.Entry<String, Object> entry : sqlmap.entrySet()) {
            sqlcsmes.append(entry.getKey()).append(":").append(entry.getValue()).append(";");
        }
        WebLogs.info(sqlcsmes.toString());
    }
}
