package com.android.xlwlibrary.helper;

import android.text.TextUtils;
import android.util.Base64;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by xu on 2019/10/16.
 */
public class XStringHelper {
    /**
     * 判断字符是否为null或空串.
     *
     * @param src 待判断的字符
     * @return
     */
    public  boolean isEmpty(String src) {
        return src == null || "".equals(src.trim())
                || "null".equalsIgnoreCase(src);
    }

    //MAC转纯数字
    public  String macTonumber(String mac){
        String newMac=mac.replace(":","");
        StringBuffer sb = new StringBuffer();
        for(int i = 0;i<newMac.length();i++){
            String c = String.valueOf(newMac.charAt(i));
            if(c.matches("[a-zA-Z]+")){
                sb.append(letterToNumber(String.valueOf(c)));
            }else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
    private  int letterToNumber(String letter) {
        int length = letter.length();
        int num = 0;
        int number = 0;
        for(int i = 0; i < length; i++) {
            char ch = letter.charAt(length - i - 1);
            num = (int)(ch - 'A' + 1) ;
            num *= Math.pow(26, i);
            number += num;
        }
        return number;
    }

    /**
     * 32位MD5加密
     * @param content -- 待加密内容
     * @return
     */
    public  String md5Decode(String content) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(content.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("NoSuchAlgorithmException",e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UnsupportedEncodingException", e);
        }
        //对生成的16字节数组进行补零操作
        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10){
                hex.append("0");
            }
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }

    /**
     * 将图片转换成Base64编码的字符串
     */
    public  String imageToBase64(String path){
        if(TextUtils.isEmpty(path)){
            return null;
        }
        InputStream is = null;
        byte[] data = null;
        String result = null;
        try{
            is = new FileInputStream(path);
            //创建一个字符流大小的数组。
            data = new byte[is.available()];
            //写入数组
            is.read(data);
            //用android的编码格式进行编码
            result = Base64.encodeToString(data,Base64.NO_WRAP);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(null !=is){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    /**
     * 签名，拼接参数(所有上传参数按照key的字典排序，拼接成QueryString格式，含areaKey)
     * 列如：appVersion=XXX&areaId=XXX&areaKey=XXX&roleId=XXX&tranTime=XXX&userId=XXX
     * @param signMap 需要拼接参数的数据集
     * @return 签名
     */
    public  String getSign(Map<String, Object> signMap) {
        String sign = "";
        String subSign = "";
        String md5Sign = "";
        Collection<String> keySet = signMap.keySet();
        List<String> list = new ArrayList<>(keySet);

        //对key键值按字典升序排序
        Collections.sort(list);
        for (int i = 0; i < list.size(); i++) {
            String key = list.get(i);
            sign = sign + key + "=" + signMap.get(key) + "&";
        }
        subSign = sign.substring(0, sign.length() - 1);
        md5Sign = md5Decode(subSign);
        return md5Sign;
    }
}
