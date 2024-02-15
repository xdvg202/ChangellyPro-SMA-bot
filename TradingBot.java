import java.io.*;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.util.Base64;

import org.json.JSONArray;
import org.json.JSONObject;

public class TradingBot {
    // insert own api key
    public static final String key = "";
    public static final String priv = "";
    public static boolean positionOpen = false;

    public static final int aboveMA = 1;
    public static final int belowMA = 2;

    public static void main(String[] args) throws Exception {

        while (true) {
            System.out.println("The price right now is: " + getBidPrice() + " and the MA is: " + getMa());
            String loco = "BELOW";
            if (getMa() < getBidPrice()) {
                loco = "ABOVE";
            }

            System.out.println("The price is " + loco + " the moving average.");

            if (crossOver() && checkPriceLocation() == belowMA && !positionOpen) {
                postOrderRequest(true, getAvailableBalance(true));
                positionOpen = true;
                Thread.sleep(300000);

            } else if (crossOver() && checkPriceLocation() == aboveMA && positionOpen) {
                postOrderRequest(false, getAvailableBalance(false));
                positionOpen = false;
            }

            Thread.sleep(1000);
        }

    }

    public static int checkPriceLocation() throws Exception {
        URL url = new URL("https://api.pro.changelly.com/api/3/public/candles/BTCUSDT?period=M5&limit=6");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        BufferedReader webRespMa = new BufferedReader(new InputStreamReader(con.getInputStream()));

        String inputLine = null;
        StringBuffer response = new StringBuffer();
        while ((inputLine = webRespMa.readLine()) != null) {
            response.append(inputLine);
        }
        webRespMa.close();

        JSONArray jsonArray = new JSONArray(response.toString());

        int pos = 0;
        int candleCount = 0;

        for (int i = 1; i < 6; i++) {
            if (jsonArray.getJSONObject(i).getDouble("close") > getMa()) {
                pos = 1;
                candleCount++;
            } else {
                pos = 2;
                candleCount++;
            }

        }
        if (candleCount == 5) {
            return pos;
        }
        return 0;
    }

    public static boolean crossOver() throws Exception {
        double deltaPriceToMA = Math.abs(getMa() - getBidPrice());
        if (deltaPriceToMA <= 5) {
            return true;
        }
        return false;
    }

    public static double getBidPrice() throws Exception {

        URL req = new URL("https://api.pro.changelly.com/api/3/public/ticker/btcusdt");
        HttpURLConnection con = (HttpURLConnection) req.openConnection();
        con.setRequestMethod("GET");
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;

        StringBuffer tempResp = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            tempResp.append(inputLine);
        }
        in.close();

        JSONArray tempArray = new JSONArray("[" + tempResp.toString() + "]");

        double bidPrice = tempArray.getJSONObject(0).getDouble("bid");
        return bidPrice;
    }

    public static double getAvailableBalance(boolean buy) throws Exception {
        String auth = key + ":" + priv;
        byte[] encodedBytes = Base64.getEncoder().encode(auth.getBytes(Charset.forName("US-ASCII")));

        String authHeader = "Basic " + new String(encodedBytes);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(new URI("https://api.pro.changelly.com/api/3/spot/balance"))
                .header("Authorization", authHeader)
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse response = client.send(getRequest, HttpResponse.BodyHandlers.ofString());

        JSONArray jsonArray = new JSONArray(response.body().toString());

        double btcBal = ((JSONObject) jsonArray.get(0)).getDouble("available");
        double usdtBal = ((JSONObject) jsonArray.get(3)).getDouble("available");

        // Buffer since the amount available cant actually be used for market orders

        usdtBal = (int) (usdtBal * 0.8);
        System.out.println(usdtBal);

        if (buy) {
            // this returns the quantity which is what we need
            System.out.println("available btc to buy: " + usdtBal / getBidPrice());
            return (usdtBal / getBidPrice());
        }
        return btcBal;

    }

    public static void postOrderRequest(boolean buy, double balance) throws Exception {

        JSONObject obj = new JSONObject();
        obj.put("client_order_id", "");
        obj.put("symbol", "BTCUSDT");
        if (buy) {
            obj.put("side", "buy");
        } else {
            obj.put("side", "sell");
        }
        obj.put("type", "market");
        obj.put("quantity", balance);

        String auth = key + ":" + priv;
        byte[] encodedBytes = Base64.getEncoder().encode(auth.getBytes(Charset.forName("US-ASCII")));

        String authHeader = "Basic " + new String(encodedBytes);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(new URI("https://api.pro.changelly.com/api/3/spot/order"))
                .header("Authorization", authHeader)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(obj.toString()))
                .build();

        HttpResponse response = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.body());

    }

    public static double getMa() throws Exception {

        URL url = new URL("https://api.pro.changelly.com/api/3/public/candles/BTCUSDT?period=M5&limit=40");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        BufferedReader webRespMa = new BufferedReader(new InputStreamReader(con.getInputStream()));

        String inputLine = null;
        StringBuffer response = new StringBuffer();
        while ((inputLine = webRespMa.readLine()) != null) {
            response.append(inputLine);
        }
        webRespMa.close();

        JSONArray jsonArray = new JSONArray(response.toString());

        double sum = 0;

        for (int i = 0; i < 40; i++) {
            sum += jsonArray.getJSONObject(i).getDouble("close");
        }

        return sum /= 40;
    }
}

class Candle {
    public int timeStamp;

    public double close = 0;
    public double prevClose = 0;
    public double priceGain = 0.0;
    public double priceLoss = 0.0;

    public Candle(JSONObject e, double prevClose) {
        try {
            close = e.getDouble("close");
        } catch (Exception f) {

        }
        this.prevClose = prevClose;

        calcGL();
    }

    public void calcGL() {
        double temp = close - prevClose;

        if (temp < 0) {
            priceLoss = Math.abs(temp);

        } else {
            priceGain = Math.abs(temp);

        }

    }

    public double getClose() {
        return close;
    }

    public double getGain() {
        return priceGain;
    }

    public double getLoss() {
        return priceLoss;
    }
}
