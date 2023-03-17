import java.io.*;
import java.net.*;

import org.json.JSONArray;

public class TradingBot {
    public static void main(String[] args) throws Exception {
        while (true) {
            System.out.println(getMa());
            Thread.sleep(50);
        }
    }

    public static void postOrderRequest(boolean buy) throws Exception {
        String symbol = "BTCUSDT";
        String side = "buy";
        if (!buy) {
            side = "sell";
        }

        double quantity = 0.009;
        String type = "market";

        URL url = new URL("https://api.pro.changelly.com/api/3/public/candles/BTCUSDT?period=M15&limit=15");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        //TODO add the api key and priv key.
        con.setRequestProperty("", "");

        BufferedReader webRespOrder = new BufferedReader(new InputStreamReader(con.getInputStream()));

        String responseTemp = new String();
        String inputLine = null;

        while ((inputLine = webRespOrder.readLine()) != null) {
            responseTemp.concat(inputLine);
        }
        webRespOrder.close();
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
