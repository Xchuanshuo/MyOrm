package com.legend.orm.core.utils;

import java.nio.charset.Charset;

/**
 * @author Legend
 * @data by on 18-12-10.
 * @description
 */
public class Utils {

    public static Charset UTF8 = Charset.forName("utf8");

    public static String tableWithSuffix(String table, String suffix) {
        if (suffix == null) return table;
        return table + "_" + suffix;
    }
}
