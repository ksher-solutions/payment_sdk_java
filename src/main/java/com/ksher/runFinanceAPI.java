package com.ksher;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


public class runFinanceAPI {


    private static String put;
    public static void main(String[] args) throws Exception {
	// write your code here
        final String gateway_domain = "https://sxxxxx.vip.ksher.net";
        final String token = "token123";

        KsherPay ksherPay = new KsherPay(gateway_domain, token, KsherPay.ApiType.FINANCE);

        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddhhmmss");
        String orderId = "OrderAt_" + dateFormat.format(date);
        String yyyymmdd="20220317";

        HashMap<String, String> param_channels = new HashMap<>();
        param_channels.put("mid", "mch35618");
        HashMap<String, String> resp_channels = ksherPay.channels(param_channels);
        System.out.println("check chanel available success response : " + resp_channels);

        HashMap<String, String> param_order = new HashMap<>();
        param_order.put("mid", "mch35618");
        param_order.put("offset","0");
        param_order.put("limit","50");
        HashMap<String, String> resp_order = ksherPay.order(yyyymmdd, param_order);
        System.out.println("order success response : " + resp_order);

        HashMap<String, String> param_settlements = new HashMap<>();
        param_settlements.put("mid", "mch35618");
        param_settlements.put("channel","truemoney");
        HashMap<String, String> resp_settlements = ksherPay.settlements(yyyymmdd, param_settlements);
        System.out.println("settlements response : " + resp_settlements);

        HashMap<String, String> param_settlement_order = new HashMap<>();
        param_settlement_order.put("reference_id","20220317_35618_GDHTQL");
        param_settlement_order.put("offset","0");
        param_settlement_order.put("limit","50");
        HashMap<String, String> resp_settlement_order = ksherPay.settlement_order(param_settlement_order);
        System.out.println("settlement_order success response : " + resp_settlement_order);

//        if(resp.get("error_code").equals("SUCCESS")) {
//             check chanel available success response
//            HashMap<String, String> param = new HashMap<>();
//            param.put("mid", "mch38026");
//            resp = ksherPay.query(orderId, param);
//            System.out.println("query order's resp : " + resp);
//            if (resp.get("status").equals("Available")) {
//                // Query the order and it hasn't been paid
//
//                System.out.println("Please make a payment and press 'Enter' to continue... ");
//                System.in.read();
//                resp = ksherPay.query(orderId, param);
//                System.out.println("query order's resp : " + resp);
//                if (resp.get("error_code").equals("SUCCESS")) {
//                    //successfully make a payment the we try to refund it
//                    HashMap<String, String> datarefund = new HashMap<>();
//                    datarefund.put("mid", "mch38026");
//                    datarefund.put("refund_order_id", "refund_" + orderId);
//                    datarefund.put("refund_amount", data.get("amount"));
//                    resp = ksherPay.refund(orderId, datarefund);
//                    System.out.println("refund order's resp : " + resp);
//                    if (resp.get("error_code").equals("REFUNDED")) {
//                        System.out.println("========= Payment finnish with success =========");
//                        return;
//                    }
//
//                }
//            } else if (resp.get("status").equals("Paid")) {
//                resp = ksherPay.query(orderId, param);
//                System.out.println("query order's resp : " + resp);
//                if (resp.get("error_code").equals("SUCCESS")) {
//                    //successfully make a payment the we try to refund it
//                    HashMap<String, String> datarefund = new HashMap<>();
//                    datarefund.put("mid", "mch38026");
//                    datarefund.put("refund_order_id", "refund_" + orderId);
//                    datarefund.put("refund_amount", data.get("amount"));
//                    resp = ksherPay.refund(orderId, datarefund);
//                    System.out.println("refund order's resp : " + resp);
//                    if (resp.get("error_code").equals("REFUNDED")) {
//                        System.out.println("========= Payment finnish with success =========");
//                        return;
//                    }
//                }
//
//            }
//        }
//        System.out.println("========= Payment end with Fail =========");
   }


}
