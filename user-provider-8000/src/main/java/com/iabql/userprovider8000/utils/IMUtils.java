package com.iabql.userprovider8000.utils;


import java.util.Random;

public class IMUtils {
    /**
     * 生成随机整数
     * @return
     */
    public static String generateUUID(){
        Random random = new Random();
        long uuid =  random.nextInt(100000) + 29607905320L;
        return String.valueOf(uuid);
    }
}
