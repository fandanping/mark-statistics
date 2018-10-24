package com.neusoft.mark.statistics.statistics.utils;

import java.util.UUID;

/**
 * ID生成器
 * @name zhengchj
 * @email zhengchj@neusoft.com
 */
public class IDGenerator {
    public static String generate(){
        return UUID.randomUUID().toString().toUpperCase().replaceAll("-", "");
    }
}
