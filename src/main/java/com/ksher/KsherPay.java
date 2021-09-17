package com.ksher;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import com.google.gson.Gson;


import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;
import java.util.*;

///***
// * ksher 生存的RSA私钥是pkcs1格式，即-----BEGIN RSA PRIVATE KEY----- 开头的。java需要pkcs8格式的，
// * 是以-----BEGIN PRIVATE KEY-----开通的，以下命令可以装有openssl环境的linux机器上转化pcks1到pcks8格式。
// * 需要pkcs8格式的可以调用命令行转换:
// * openssl pkcs8 -topk8 -inform PEM -in private.key -outform pem -nocrypt -out pkcs8.pem
// * 1、PKCS1私钥生成
// * openssl genrsa -out private.pem 1024
// * 2、PKCS1私钥转换为PKCS8(该格式一般Java调用)
// * openssl pkcs8 -topk8 -inform PEM -in private.pem -outform pem -nocrypt -out pkcs8.pem
// */

public class KsherPay {

    public enum ApiType{
        REDIRECT,
        CSCANB
    }
    private final String gateway_domain;
    private final String token;
    private final String apiEndpoint;


    private final java.text.SimpleDateFormat timeStampFormat = new java.text.SimpleDateFormat("yyyyMMddHHmmss");

    // KsherPay can be init with 2, 3, 4 params
    public KsherPay(String gateway_domain, String token, ApiType apiType) {
        if (gateway_domain.endsWith("/")){
            this.gateway_domain = gateway_domain.substring(0, gateway_domain.length() -1);
        }else{
            this.gateway_domain = gateway_domain;
        }

        this.token = token;
        switch(apiType){
            case REDIRECT:
                apiEndpoint = "/api/v1/redirect/orders";
                break;
            case CSCANB:
                apiEndpoint = "/api/v1/cscanb/orders";
                break;
            default:
                apiEndpoint = "/api/v1/redirect/orders";
                break;
        }
    }

    /**
     * Transfer binary array to HEX string.
     */
    public static String byte2hex(byte[] bytes) {
        StringBuilder sign = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() == 1) {
                sign.append("0");
            }
            sign.append(hex.toUpperCase());
        }
        return sign.toString();
    }

    /**
     * hex string to byte
     * @param sign
     * @return
     */
    public byte[] unHexVerify(String sign) {
        int length = sign.length();
        byte[] result = new byte[length / 2];
        for (int i = 0; i < length; i += 2)
            result[i / 2] = (byte) ((Character.digit(sign.charAt(i), 16) << 4) + Character.digit(sign.charAt(i + 1), 16));
        return result;
    }

    private static byte[] encryptHMACSHA256(String data, String secret) throws IOException {
        byte[] bytes = null;
        try {
            SecretKey secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            Mac mac = Mac.getInstance(secretKey.getAlgorithm());
            mac.init(secretKey);
            bytes = mac.doFinal(data.getBytes("utf-8"));
        } catch (GeneralSecurityException | UnsupportedEncodingException gse) {
            throw new IOException(gse.toString());
        }
        return bytes;
    }

    /**
     * 签名
     * @param params
     * @return
     */
    public String make_sign(String url, Map<String, String> params) throws Exception {

        // first: sort all text parameters
        String[] keys = params.keySet().toArray(new String[0]);
        Arrays.sort(keys);

        // second: connect all text parameters with key and value
        StringBuilder dataStrB = new StringBuilder();
        dataStrB.append(url);
        for (String key : keys) {
            String value = params.get(key);
            dataStrB.append(key).append(value);
        }
        System.out.println("dataStr: " + dataStrB.toString());
        // next : sign the whole request
        byte[] bytes = null;
        bytes = encryptHMACSHA256(dataStrB.toString(), this.token);



        // finally : transfer sign result from binary to upper hex string
        return byte2hex(bytes);
    }

//    /**
//     * 校验数字签名
//     * @param data
//     * @param sign
//     * @return 校验成功返回true，失败返回false
//     */
//    public boolean KsherVerify(Map data, String sign) throws Exception {
//        boolean flag = false;
//        //将私钥加密数据字符串转换为字节数组
//        byte[] dataByte = getParamsSort(data);
//        // 解密由base64编码的公钥
//        byte[] publicKeyBytes = Base64.decodeBase64(publicKey.getBytes());
//        // 构造X509EncodedKeySpec对象
//        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
//        // 指定的加密算法
//        KeyFactory factory = KeyFactory.getInstance(KEY_RSA);
//        // 取公钥对象
//        PublicKey key = factory.generatePublic(keySpec);
//        // 用公钥验证数字签名
//        Signature signature = Signature.getInstance(KEY_RSA_SIGNATURE);
//        signature.initVerify(key);
//        signature.update(dataByte);
//        return signature.verify(unHexVerify(sign));
//    }

//    /**
//     * post请求(用于key-value格式的参数)
//     *
//     * @param url
//     * @param params
//     * @return
//     */
    private String request(String url, String method, HashMap<String, String> data) throws Exception {
        URL obj = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
        connection.setRequestMethod(method);
        connection.setRequestProperty("Content-Type", "application/json");

        //prep to post body data
        Gson gson = new Gson();
        data.put("timestamp", timeStampFormat.format(new java.util.Date()));
        String sign = make_sign(apiEndpoint, data);
        data.put("signature", sign);
        String jsonStr = gson.toJson(data);
        System.out.println("jsonStr: " + jsonStr);

        connection.setDoOutput(true);
        OutputStream os = connection.getOutputStream();
        os.write(jsonStr.getBytes());
        os.flush();
        os.close();




        int responseCode = connection.getResponseCode();
//        System.out.println("POST Response Code :  " + responseCode);
//        System.out.println("POST Response Message : " + connection.getResponseMessage());


        BufferedReader in = new BufferedReader(new InputStreamReader(
                connection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in .readLine()) != null) {
            response.append(inputLine);
        } in .close();

        return response.toString();




    }

    public String create( HashMap<String, String> data) throws Exception{
        final String endpointUrl = this.gateway_domain + this.apiEndpoint;
        return this.request(endpointUrl, "POST", data);
    }
}