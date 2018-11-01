package com.one;

/**
 * @author One
 * @description
 * @date 2018/11/01
 */
public class StringUtil {

    public static String lowerFirstLetter(String name) {
        String[] split = name.split("\\.");
        String s = split[split.length - 1];
        String first = s.substring(0, 1);
        String substring = s.substring(1);
        return first.toLowerCase() + substring;
    }

    public static boolean isNotBank(String string) {
        return string != null && string.trim() != "";
    }

}
