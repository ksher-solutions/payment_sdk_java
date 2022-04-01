package com.ksher;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;
//library for displayImg
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;


public class runBScanCAPI {


    private static String put;
    public static void main(String[] args) throws Exception {
	// write your code here
        final String gateway_domain = "https://sxxxxx.vip.ksher.net";
        final String token = "token123";

        HashMap<String, String> data = new HashMap<>();
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddhhmmss");
        String orderId = "OrderAt_" + dateFormat.format(date);
        data.put("mid", "mch38026");
        data.put("amount", "100");
        data.put("channel", "truemoney");
        data.put("merchant_order_id", orderId);

        System.out.println("Enter Barcode:");
        Scanner scanner = new Scanner(System.in);
        String auth_code = scanner.nextLine();

        data.put("auth_code", auth_code);

        KsherPay ksherPay = new KsherPay(gateway_domain, token, KsherPay.ApiType.BSCANC);
        System.out.println("create order's request : " + data);
        HashMap<String, String> resp = ksherPay.create(data);
        System.out.println("create order's resp : " + resp);

        if(resp.get("error_code").equals("SUCCESS")) {
            // Create Order get success response
            HashMap<String, String> param = new HashMap<>();
            param.put("mid", "mch38026");
            resp = ksherPay.query(orderId, param);
            System.out.println("query order's resp : " + resp);
            if (resp.get("status").equals("Available")) {
                // Query the order and it hasn't been paid

                System.out.println("Please make a payment and press 'Enter' to continue... ");
                System.in.read();
                resp = ksherPay.query(orderId, param);
                System.out.println("query order's resp : " + resp);
                if (resp.get("error_code").equals("SUCCESS")) {
                    //successfully make a payment the we try to refund it
                    HashMap<String, String> datarefund = new HashMap<>();
                    datarefund.put("mid", "mch38026");
                    datarefund.put("refund_order_id", "refund_" + orderId);
                    datarefund.put("refund_amount", data.get("amount"));
                    resp = ksherPay.refund(orderId, datarefund);
                    System.out.println("refund order's resp : " + resp);
                    if (resp.get("error_code").equals("REFUNDED")) {
                        System.out.println("========= Payment finnish with success =========");
                        return;
                    }

                }
            } else if (resp.get("status").equals("Paid")) {
                resp = ksherPay.query(orderId, param);
                System.out.println("query order's resp : " + resp);
                if (resp.get("error_code").equals("SUCCESS")) {
                    //successfully make a payment the we try to refund it
                    HashMap<String, String> datarefund = new HashMap<>();
                    datarefund.put("mid", "mch38026");
                    datarefund.put("refund_order_id", "refund_" + orderId);
                    datarefund.put("refund_amount", data.get("amount"));
                    resp = ksherPay.refund(orderId, datarefund);
                    System.out.println("refund order's resp : " + resp);
                    if (resp.get("error_code").equals("REFUNDED")) {
                        System.out.println("========= Payment finnish with success =========");
                        return;
                    }
                }

            }
        }
        System.out.println("========= Payment end with Fail =========");
    }

}
