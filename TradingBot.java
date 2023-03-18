import java.io.*;
import java.net.*;
import java.util.Base64;

import org.json.JSONArray;

public class TradingBot {

    public static final String key = "s9N9ZOUuCoTNJDtlpfvWiHq3_QZhES3S";

    public static final String priv = "IGOpMy1NwzKWOjYHgeQQedYspqNb-QHD";

    public static void main(String[] args) throws Exception {
        /*
         * while (true) {
         * System.out.println(getMa());
         * Thread.sleep(50);
         * }
         */
        postOrderRequest(true);
        // TODO figure out when and how much to buy
    }

    public static void postOrderRequest(boolean buy) throws Exception {
        String symbol = "BTCUSDT";
        String side = "buy";
        if (!buy) {
            side = "sell";
        }

        double quantity = 1;
        String type = "market";
        String timeInForce = "GTC";

        URL url = new URL("https://api.pro.changelly.com/api/3/margin/order");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setDoOutput(true);
        con.setRequestMethod("POST");
        String userpass = key + ":" + priv;
        String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
        con.addRequestProperty("Authorization", basicAuth);

        
        con.addRequestProperty("Content-Type ", "application/json");

        

        String postRequestString = "{\"client_order_id\":\"\", \"symbol\": \"BTCUSDT\", \"side\": \"sell\", \"type\": \"market\", \"time_in_force\": \"GTC\", \"quantity\": \"2\"}";

        String temp = "client_order_id=&time_in_force=GTC&reduce_only=&price=&quantity=2&post_only=&strict_validate=&stop_price=&expire_time=&make_rate=&side=sell&take_rate=&symbol=BTCUSDT&type=market";
        try (OutputStream os = con.getOutputStream()) {
            byte[] input = postRequestString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        // con.getOutputStream().write(temp.getBytes());

        BufferedReader webRespOrder = new BufferedReader(new InputStreamReader(con.getInputStream()));

        String responseTemp = new String();
        String inputLine = null;

        while ((inputLine = webRespOrder.readLine()) != null) {
            responseTemp.concat(inputLine);
        }
        System.out.println(responseTemp);
        webRespOrder.close();
        con.disconnect();
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
