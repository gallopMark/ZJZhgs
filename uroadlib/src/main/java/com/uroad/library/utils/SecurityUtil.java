package com.uroad.library.utils;

import java.security.MessageDigest;

public class SecurityUtil {

    /**
     * 将字符串转换为MD5加密（32位小写）
     **/
    public static String EncoderByMd5(String str) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("md5");// 返回实现指定摘要算法的
            md5.update(str.getBytes());// 先将字符串转换成byte数组，再用byte 数组更新摘要
            byte[] nStr = md5.digest();// 哈希计算，即加密
            return bytes2Hex(nStr);// 加密的结果是byte数组，将byte数组转换成字符串
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 将字符串转换为SHA-1加密
     **/
    public static String EncoderBySHA1(String str) {
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");// 返回实现指定摘要算法的
            sha1.update(str.getBytes());// 先将字符串转换成byte数组，再用byte 数组更新摘要
            byte[] nStr = sha1.digest();// 哈希计算，即加密
            return bytes2Hex(nStr);// 加密的结果是byte数组，将byte数组转换成字符串
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 将字符串转换为SHA-256加密
     **/
    public static String EncoderBySHA256(String str) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");// 返回实现指定摘要算法的
            // MessageDigest
            // 对象。
            sha256.update(str.getBytes());// 先将字符串转换成byte数组，再用byte 数组更新摘要
            byte[] nStr = sha256.digest();// 哈希计算，即加密
            return bytes2Hex(nStr);// 加密的结果是byte数组，将byte数组转换成字符串
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 将byte数组转换为字符串
     **/
    private static String bytes2Hex(byte[] bts) {
        StringBuilder des = new StringBuilder();
        for (byte bt : bts) {
            String tmp = (Integer.toHexString(bt & 0xFF));
            if (tmp.length() == 1) {
                des.append("0");
            }
            des.append(tmp);
        }
        return des.toString();
    }
}
