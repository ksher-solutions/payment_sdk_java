package com.ksher;

import org.junit.Assert;
import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.awt.Desktop;
import java.net.URI;


public class KsherPayTest {
    public final String gateway_domain = "https://sxxxxx.vip.ksher.net";
    public final String token = "token123";

    @Test
    public void testCreateOrderFail() throws Exception {
    //  try to put not all the required field and the result should be fail
        HashMap<String, String> data = new HashMap<>();
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddhhmmss");
        String orderId = "OrderAt_" + dateFormat.format(date);
        data.put("redirect_url", "https://www.google.com");
        data.put("redirect_url_fail", "https://www.yahoo.com");
        data.put("merchant_order_id", orderId);
        KsherPay ksherPay = new KsherPay(gateway_domain, token, KsherPay.ApiType.REDIRECT);
        HashMap<String, String> resp = ksherPay.create(data);
        System.out.println("resp: " + resp);
        Assert.assertEquals(resp.get("error_code"),"400");
    }

    @Test
    public void testCreateOrderSuccess() throws Exception {

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
        Assert.assertEquals(resp.get("error_code"),"SUCCESS");
        Assert.assertEquals(resp.get("status"),"Available");
        assertThat(resp.get("reference"),containsString("gateway.ksher.com"));
        if(resp.get("error_code").equals("SUCCESS")){
            resp = ksherPay.query(orderId);
            System.out.println("query order's resp : " + resp);
            Assert.assertEquals(resp.get("error_code"),"PENDING");
            Desktop desk = Desktop.getDesktop();
            desk.browse(new URI(resp.get("reference")));
        }

    }


}
