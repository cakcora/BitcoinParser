import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;


public class PriceFetcher {

    public static Object[] getCurrentBtcPriceInUSD() throws ParseException, MalformedURLException, java.text.ParseException {


        URL url = new URL("https://api.coindesk.com/v1/bpi/currentprice.json");
        Object[] arr = new Object[0];
        int responseCode = 0;
        Scanner scanner;
        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            //Getting the response code
            responseCode = conn.getResponseCode();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (responseCode != 200) {
            throw new RuntimeException("HttpResponseCode: " + responseCode);
        } else {

            String inline = "";
            InputStream source = null;
            try {
                source = url.openStream();

                scanner = new Scanner(source);

                //Write all the JSON data into a string using a scanner
                while (scanner.hasNext()) {
                    inline += scanner.nextLine();
                }

                //Close the scanner
                scanner.close();

                //Using the JSON simple library parse the string into a json object
                JSONParser parse = new JSONParser();
                JSONObject data_obj = (JSONObject) parse.parse(inline);

                //Get the required object from the above created object
                JSONObject obj = (JSONObject) data_obj.get("time");
                String utcTimeString = (obj.get("updatedISO").toString());
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
                LocalDateTime localTimeObj = LocalDateTime.parse(utcTimeString, formatter);

                JSONObject priceInfo = (JSONObject) data_obj.get("bpi");
                JSONObject usd = (JSONObject) priceInfo.get("USD");
                double btcPrice = Double.parseDouble(String.valueOf(usd.get("rate_float")));
                //Get the required data using its key
                arr = new Object[2];
                arr[0] = localTimeObj;
                arr[1] = btcPrice;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return arr;
    }

    public static void main(String[] args) throws ParseException, java.text.ParseException, MalformedURLException, InterruptedException {
        while (true) {
            Object[] priceArr = getCurrentBtcPriceInUSD();
            if (priceArr.length == 2) {
                LocalDateTime datetime = (LocalDateTime) priceArr[0];

                double btcPrice = (double) priceArr[1];
                try {
                    System.out.println(datetime + " price is " + btcPrice);
                    DBConnection.savePriceToDB(datetime, btcPrice);
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                TimeUnit.MINUTES.sleep(2);
            }
        }
    }
}
