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

    public static final String key = "4ozEDIwf0T2sM3t8G5nIyjyZEJ74pKh8";
    public static final String priv = "jqYujeWUM9xFIjDfwNIzA9AXTrkrOIqv";

    public static void main(String[] args) throws Exception {
      
        postOrderRequest(true);
        System.out.println(getMa());
        
    }

    public static void postOrderRequest(boolean buy) throws Exception {

        JSONObject obj = new JSONObject();
        obj.put("client_order_id", "");
        obj.put("symbol", "BTCUSDT");
        if (buy) {
            obj.put("side", "buy");
        } else {
            obj.put("side", "sell");
        }
        obj.put("type", "market");
        obj.put("time_in_force", "GTC");
        obj.put("quantity", "2");

        String auth = key + ":" + priv;
        byte[] encodedBytes = Base64.getEncoder().encode(auth.getBytes(Charset.forName("US-ASCII")));

        String authHeader = "Basic " + new String(encodedBytes);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(new URI("https://api.pro.changelly.com/api/3/margin/order"))
                .header("Authorization", authHeader)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(obj.toString()))
                .build();

        HttpResponse response = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.body());
    }

    public static double getMa() throws Exception {

        URL url = new URL("https://api.pro.changelly.com/api/3/public/candles/BTCUSDT?period=M15&limit=15");
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

        for (int i = 0; i < 15; i++) {
            sum += jsonArray.getJSONObject(i).getDouble("close");
        }

        return sum /= 15;
    }

}
