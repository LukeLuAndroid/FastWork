package com.sdk.javaswing;


import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author luke.lu
 * @date 2020/4/18
 */

public class AESUtil {
    /**
     * 采用对称分组密码体制,密钥长度的最少支持为128、192、256
     */
    
    static String keyInfo = "75fb25428f7f6731ab9deaa955ee4691";
    /**
     * 初始化向量参数，AES 为16bytes. DES 为8bytes， 16*8=128
     */

    String initVector = "0000000000000000";
    IvParameterSpec iv;
    SecretKeySpec skeySpec;
    Cipher cipher;

    private static class Holder {
        private static AESUtil instance = new AESUtil();
    }

    public static AESUtil getInstance() {
        return Holder.instance;
    }

    private AESUtil() {
        try {
            skeySpec = new SecretKeySpec(keyInfo.getBytes("UTF-8"), "AES");
            cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public String encrypt(String value) {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return new String(Base64.getMimeEncoder().encode(encrypted), "US-ASCII");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String decrypt(String encrypted) {
        try {
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);

            byte[] original = cipher.doFinal(Base64.getMimeDecoder().decode(encrypted));
            return new String(original, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}