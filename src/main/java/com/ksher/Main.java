package com.ksher;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main {

    public static void main(String[] args) throws Exception {
	// write your code here
        HashMap<String, String> data = new HashMap<>();
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddhhmmss");
        String orderId = "OrderAt_" + dateFormat.format(date);
        System.out.println("Converted String: " + orderId);
        data.put("amount", "100");
        data.put("redirect_url", "https://www.google.com");
        data.put("redirect_url_fail", "https://www.yahoo.com");
        data.put("merchant_order_id", orderId);
        KsherPay ksherPay = new KsherPay("https://sandboxbkk.vip.ksher.net", "186d6c953c90f39c2973e6dd2e110d4057194996ef08fb4b3338180517b509c7", KsherPay.ApiType.REDIRECT);
        String resp = ksherPay.create(data);
        System.out.println("resp: " + resp);

    }
}
