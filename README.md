# Payment_sdk_python

This is java sdk for integrating your java application with Ksher Payment Gateway. 
Please refer to our official api document [here](https://doc.vip.ksher.net)

## Requirement
- Java 11
    - this has been tested and run on java 11.

- Ksher Payment API Account
    - Requesting sandbox account please contact support@ksher.com

- API_URL
    - Along with a sandbox accout, you will be receiving a API_URL in this format: s[UNIQUE_NAME].vip.ksher.net

- API_TOKEN
    - Log in into API_URL using given sandbox account and get the token. see (How to get API Token)[https://doc.vip.ksher.net/docs/howto/api_token]


The Payment SDK for accessing *.vip.ksher.net

## How to Install

please see the dependency tag that need to be add in your project's pom.xml file [here](https://github.com/ksher-solutions/payment_sdk_java/packages/1016715)

### package cannot be found
if after adding the tag and you got error saying it cannot find the package,
you probably didn't config your project settings.xml to check github package.
please follow this guide [here](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry#authenticating-with-a-personal-access-token) on how to update settings.xml


## How to Use
you need to first init the payment object, then you can use it to;
- Init Payment Object
- Create New Order
- Query Order Status
- Refund the Order


### Init Payment Object
ksherpay have multiple api (apiType) such as;
- redirect API is for Website and Mobile App integration.
- settlement API is for checking the settlement information.
- miniapp API is for WeChat and Alipay Mini-Program integration.
- event API is for checking the events deliveried.
- C scan B API is for C scan B(merchant present QR code) or Kiosk integration.
- B scan C API is for B scan C(customer present QR code) or POS integration.

you can read about it [here](https://doc.vip.ksher.net/docs/user_guide/swagger)

Currently, this python sdk support only two api; 'redirect api' and 'c scan b api'

#### Redirect API

```java
String gateway_domain = "https://sandboxbkk.vip.ksher.net";
String token = "testtoken1234";
KsherPay ksherPay = new KsherPay(gateway_domain, token, KsherPay.ApiType.REDIRECT);
```

#### C_Scan_B API

```java
String gateway_domain = "https://sandboxbkk.vip.ksher.net";
String token = "testtoken1234";
KsherPay ksherPay = new KsherPay(gateway_domain, token, KsherPay.ApiType.CSCANB);
```

### Create New Order
***merchant_order_id need to be unique or else the request will end with error***

to create new order, each apiType has slightly different required parameters

#### Redirect API
```java
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
```
#### C_Scan_B API
for 'C_Scan_B API', redirect_url is not needed and you can specified one channel at a time.
```java
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
```

### Query order status
```java
resp = ksherPay.query(orderId);
```

### Refund
***Refund_id need to be unique or else the request will end with error***

```java
Sting amount = "100"; // this mean refund 1 bath
resp = ksherPay.refund(orderId,"refund_" + orderId, data.get("amount"));

```
