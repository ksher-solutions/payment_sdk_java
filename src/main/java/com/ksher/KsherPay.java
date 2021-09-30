package com.ksher;

import java.io.*;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;
import java.util.*;



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


    private static byte[] encryptHMACSHA256(String data, String secret) throws IOException {
        byte[] bytes;
        try {
            SecretKey secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            Mac mac = Mac.getInstance(secretKey.getAlgorithm());
            mac.init(secretKey);
            bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException gse) {
            throw new IOException(gse.toString());
        }
        return bytes;
    }

    /**
     * making signature from concat url and all pairs of data
     * @param  url api endpoint for example /api/v1/app/orders
     * @param  params a combination of parameters and body payload sorted alphabetically
     * @return a string of signature
     */
    private String _makeSignature(String url, Map<String, String> params) throws Exception {

        // first: sort all text parameters
        String[] keys = params.keySet().toArray(new String[0]);
        Arrays.sort(keys);

        // second: connect all text parameters with key and value
        StringBuilder dataStrB = new StringBuilder();
        dataStrB.append(url);
        for (String key : keys) {
            String value = params.get(key);
            if(value.equals("false"))
            {
                value = "False";
            }

            if(value.equals("true")){
                value = "True";
            }
            dataStrB.append(key).append(value);
        }
        // next : sign the whole request
        byte[] bytes;
        bytes = encryptHMACSHA256(dataStrB.toString(), this.token);



        // finally : transfer sign result from binary to upper hex string
        return byte2hex(bytes);
    }

    public boolean checkSignature(String url, Map<String, String> params) throws Exception {

        String signature = (params.remove("signature")).toUpperCase();
        if (signature.isEmpty()){
            return false;
        }

        String signFromData = _makeSignature(url, params).toUpperCase();


        return  signature.equals(signFromData) ;

    }

    public  String makeParamString(HashMap<String, String> data){
        // first: sort all text parameters
        String[] keys = data.keySet().toArray(new String[0]);
        Arrays.sort(keys);
        StringBuilder dataStrB = new StringBuilder();
        for(String key: keys){
            if(dataStrB.length() == 0){
                dataStrB.append("?");
            }else{
                dataStrB.append("&");
            }
            dataStrB.append(key+"="+data.get(key));
        }
        return dataStrB.toString();

    }
    private HashMap<String, String> _request(String endpoint, String method, HashMap<String, String> data) throws Exception {

        //prep to post body data
        String url;
        Gson gson = new Gson();
        data.put("timestamp", timeStampFormat.format(new java.util.Date()));
        String sign = _makeSignature(endpoint, data);
        data.put("signature", sign);
        HashMap<String, String> respData;

        if (method.equals("GET") || method.equals("DELETE")){
            // for GET and DELETE we put the data into parameter
            url = this.gateway_domain + endpoint + this.makeParamString(data);
        }else
        {
            url = this.gateway_domain + endpoint;
        }
        URL obj = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
        connection.setRequestMethod(method);


        if(method.equals("POST") || method.equals("PUT")){
            // for POST and PUT we put the data into request body
            connection.setRequestProperty("Content-Type", "application/json");
            String jsonStr = gson.toJson(data);
            connection.setDoOutput(true);
            OutputStream os = connection.getOutputStream();
            os.write(jsonStr.getBytes());
            os.flush();
            os.close();
        }



        // process the response
        BufferedReader in;
        int responseCode = connection.getResponseCode();

        // see which buffer to be read from.
        if (responseCode == HttpURLConnection.HTTP_OK){
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));

        } else {
            in = new BufferedReader(new InputStreamReader(
                    connection.getErrorStream()));
        }

        // read data from the buffer
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in .readLine()) != null) {
            response.append(inputLine);
        } in .close();

        // covert json into hashmap
        String respStr = response.toString();
        Type type = new TypeToken<HashMap<String, String>>(){}.getType();

        try{
            respData = gson.fromJson(respStr, type);
        } catch (Exception e){
            HashMap<String, String> failData = new HashMap<>();
            failData.put("force_clear","false");
            failData.put("cleared","false");
            failData.put("error_code","INVALID_RESP");
            failData.put("error_message",respStr);
            failData.put("locked","false");
            return failData;
        }


        // for success resp check signature
        if (responseCode == HttpURLConnection.HTTP_OK){
            boolean checkResult = this.checkSignature(endpoint, respData);
            if(!checkResult ){
                HashMap<String, String> failData = new HashMap<>();
                failData.put("force_clear","false");
                failData.put("cleared","false");
                failData.put("error_code","VERIFY_KSHER_SIGN_FAIL");
                failData.put("error_message","verify signature failed");
                failData.put("locked","false");

                return failData;

            }
            return respData;
        }

        //otherwise send back error message from request response
        HashMap<String, String> failData = new HashMap<>();
        failData.put("error_code",respData.get("code"));
        failData.put("error_message",respData.get("description"));

        return failData;
    }
    public HashMap<String, String> query(String orderId) throws Exception{
        HashMap<String, String> data = new HashMap<>();
        final String endpointUrl = this.apiEndpoint + "/" + orderId;
        return this._request(endpointUrl, "GET", data);
    }
    public HashMap<String, String> cancle(String orderId) throws Exception{
        HashMap<String, String> data = new HashMap<>();
        final String endpointUrl = this.apiEndpoint + "/" + orderId;
        return this._request(endpointUrl, "DELETE", data);
    }
    /**
     * making a refund to a specified order
     * @param  orderId the order that you want to make a refund
     * @param  refundId a new unique id to refer to this refund transaction
     * @param  refundAmount string of an amount you want to be refund in cent unit eg 1 baht = "100"
     * @return if the signature matched return true, otherwise false.
     */
    public HashMap<String, String> refund(String orderId, String refundId, String refundAmount) throws Exception{
        HashMap<String, String> data = new HashMap<>();
        data.put("refund_amount",refundAmount);
        data.put("refund_order_id",refundId);
        final String endpointUrl = this.apiEndpoint + "/" + orderId;
        return this._request(endpointUrl, "PUT", data);
    }

    public HashMap<String, String> create( HashMap<String, String> data) throws Exception{
        final String endpointUrl = this.apiEndpoint;
        return this._request(endpointUrl, "POST", data);
    }
}