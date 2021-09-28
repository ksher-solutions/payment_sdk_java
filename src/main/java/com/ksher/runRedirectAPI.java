package com.ksher;
import java.awt.*;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class runRedirectAPI {


    public static void main(String[] args) throws Exception {
	// write your code here
        final String gateway_domain = "https://sandboxbkk.vip.ksher.net";
        final String token = "186d6c953c90f39c2973e6dd2e110d4057194996ef08fb4b3338180517b509c7";

        HashMap<String, String> data = new HashMap<>();
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddhhmmss");
        String orderId = "OrderAt_" + dateFormat.format(date);
        data.put("amount", "100");
        data.put("redirect_url", "https://www.google.com");
        data.put("redirect_url_fail", "https://www.yahoo.com");
        data.put("merchant_order_id", orderId);
        KsherPay ksherPay = new KsherPay(gateway_domain, token, KsherPay.ApiType.REDIRECT);
        HashMap<String, String> resp = ksherPay.create(data);
        System.out.println("create order's resp : " + resp);

        if(resp.get("error_code").equals("SUCCESS")){
            // Create Order get success response
            resp = ksherPay.query(orderId);
            System.out.println("query order's resp : " + resp);
            if(resp.get("error_code").equals("PENDING")){
                // Query the order and it hasn't been paid

                // make a payment in the browser
                Desktop desk = Desktop.getDesktop();
                desk.browse(new URI(resp.get("reference")));
                System.out.println("Please make a payment and press 'Enter' to continue... ");
                System.in.read();
                resp = ksherPay.query(orderId);
                System.out.println("query order's resp : " + resp);
                if(resp.get("error_code").equals("SUCCESS")){
                    //successfully make a payment the we try to refund it
                    resp = ksherPay.refund(orderId,"refund_" + orderId, data.get("amount"));
                    System.out.println("refund order's resp : " + resp);
                    if(resp.get("error_code").equals("REFUNDED")){
                        System.out.println("========= Payment finnish with success =========");
                        return;
                    }

                }
            }

        }

        System.out.println("========= Payment end with Fail =========");
    }
}
