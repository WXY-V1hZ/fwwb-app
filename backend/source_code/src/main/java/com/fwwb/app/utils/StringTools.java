package com.fwwb.app.utils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.util.DigestUtils;


public class StringTools {

    public static boolean isEmpty(String str) {
        if (null == str || str.isEmpty() || "null".equals(str) || "\u0000".equals(str)) {
            return true;
        } else if (str.trim().isEmpty()) {
            return true;
        }
        return false;
    }

    public static String getRandomString(Integer count) {
        return RandomStringUtils.random(count, true, true);
    }

    public static String getRandomNumber(Integer count) {
        return RandomStringUtils.random(count, false, true);
    }

    public static final String encodeByMd5(String originString) {
        return StringTools.isEmpty(originString) ? null : DigestUtils.md5DigestAsHex(originString.getBytes());
    }

    public static boolean pathIsOk(String path) {
        if (StringTools.isEmpty(path)) {
            return true;
        }
        if (path.contains("../") || path.contains("..\\")) {
            return false;
        }
        return true;
    }

    public static String getFileSuffix(String fileName) {
        if (StringTools.isEmpty(fileName) || !fileName.contains(".")) {
            return null;
        }
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        return suffix;
    }
}
