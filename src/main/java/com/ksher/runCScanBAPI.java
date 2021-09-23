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

//library for displayImg
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;


public class runCScanBAPI {


    public static void main(String[] args) throws Exception {
	// write your code here
        final String gateway_domain = "https://sxxxxx.vip.ksher.net";
        final String token = "token123";

        HashMap<String, String> data = new HashMap<>();
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddhhmmss");
        String orderId = "OrderAt_" + dateFormat.format(date);
        data.put("amount", "100");
        data.put("channel", "truemoney");
        data.put("merchant_order_id", orderId);
        KsherPay ksherPay = new KsherPay(gateway_domain, token, KsherPay.ApiType.CSCANB);
        HashMap<String, String> resp = ksherPay.create(data);
        System.out.println("create order's resp : " + resp);

        if(resp.get("error_code").equals("SUCCESS")){
            // Create Order get success response
            resp = ksherPay.query(orderId);
            System.out.println("query order's resp : " + resp);
            if(resp.get("status").equals("Available")){
                // Query the order and it hasn't been paid

                // make a payment in the browser
                displayQRImg(resp.get("reserved1"));
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

    public static void displayQRImg(String inlineImg) throws IOException {
        String base64ImageString = inlineImg.replace("data:image/png;base64,", "");
        byte[] imgInBytes = Base64.getDecoder().decode(base64ImageString);
        InputStream in = new ByteArrayInputStream(imgInBytes);
        BufferedImage bufferedImage = ImageIO.read(in);
        DisplayImage(bufferedImage);
    }
    public static void DisplayImage(BufferedImage img) throws IOException
    {
        ImageIcon icon=new ImageIcon(img);
        JFrame frame=new JFrame();
        frame.setLayout(new FlowLayout());
        frame.setSize(600,600);
        JLabel lbl=new JLabel();
        lbl.setIcon(icon);
        frame.add(lbl);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
}
