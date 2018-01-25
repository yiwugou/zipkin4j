package com.yiwugou.zipkin4j.example.util;

import com.alibaba.fastjson.JSONObject;

/**
 * 
 * <pre>
 * CommonUtils
 * </pre>
 * 
 * @author zhanxiaoyong@yiwugou.com
 *
 * @since 2018年1月25日 下午4:20:23
 */
public class CommonUtils {
    public static String methodAndArgs(String method, Object... args) {
        StringBuilder sb = new StringBuilder(method + "(");
        if (args.length > 0) {
            for (Object arg : args) {
                if (arg == null) {
                    sb.append(null + ",");
                } else {
                    sb.append(JSONObject.toJSONString(arg) + ",");
                }
            }
            sb.deleteCharAt(sb.length() - 1);
        }

        return sb.append(")").toString();
    }

    public static String errorToString(Throwable e) {
        if (e != null) {
            StringBuilder sb = new StringBuilder();
            for (StackTraceElement s : e.getStackTrace()) {
                sb.append(s.toString()).append(System.lineSeparator());
            }
            return sb.toString();
        }
        return "";
    }
}
