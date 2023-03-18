import java.io.*;
import java.net.*;

import org.json.JSONArray;

public class TradingBot { 

    public static final String key = "s9N9ZOUuCoTNJDtlpfvWiHq3_QZhES3S";

    public static final String priv = "IGOpMy1NwzKWOjYHgeQQedYspqNb-QHD";
    public static void main(String[] args) throws Exception {
        /*while (true) {
            System.out.println(getMa());
            Thread.sleep(50);
        }*/
        postOrderRequest(true);
        //TODO figure out when and how much to buy
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
        con.setRequestMethod("POST");
        
        con.setRequestProperty("s9N9ZOUuCoTNJDtlpfvWiHq3_QZhES3S", "IGOpMy1NwzKWOjYHgeQQedYspqNb-QHD");

//TODO setup the post request to the endpoint.

        //byte[] props = {side.getBytes()}; 
        //System.out.println(props.toString());
con.getOutputStream().write(null);

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
