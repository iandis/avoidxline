package com.avoprojects.avoidxline;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

public class StocksAPI {
    public ArrayList<ArrayList<String>> getQuote(String[] symbol)
    {
        try {

            //yahoo finance api url
            String yfapiUrl = "https://query1.finance.yahoo.com/v7/finance/quote?symbols=";
                String sim="";
                for(int i=0;i<symbol.length;i++){
                    sim=sim+symbol[i];
                    if(i!=symbol.length-1){
                        sim=sim+",";
                    }
                }
            URL url = new URL(yfapiUrl + sim);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            // optional default is GET
            con.setRequestMethod("GET");
            //add request header
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            con.getResponseCode();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            con.disconnect();
            //Read JSON response and print
            JSONObject yfjson = new JSONObject(response.toString());
            JSONArray yfresult = yfjson.getJSONObject("quoteResponse").getJSONArray("result");//.getJSONObject(0);

            if (yfresult.isEmpty()) {
                return null;
            } else {
                String change, changep, marketCap, shortName, price, lastchange;
                ArrayList<ArrayList<String>> hasil = new ArrayList<>();
                for(int i=0; i<yfresult.length();i++) {
                    shortName = yfresult.getJSONObject(i).getString("shortName");
                    double mPrice = yfresult.getJSONObject(i).getDouble("regularMarketPrice");
                    double mChange = yfresult.getJSONObject(i).getDouble("regularMarketChange");
                    double mChangep = yfresult.getJSONObject(i).getDouble("regularMarketChangePercent");
                    try {
                        double mMarketcap = yfresult.getJSONObject(i).getDouble("marketCap");
                        marketCap = formatMarketCap(mMarketcap);
                    } catch (Exception e) {
                        marketCap = "N/A";
                    }
                    price = String.format("%.2f", mPrice);
                    change = String.format(mChange > 0 ? "+%.2f" : "%.2f", mChange);
                    changep = String.format(mChangep > 0 ? "+%.2f%%" : "%.2f%%", mChangep);
                    lastchange = change + " (" + changep + ")";
                    ArrayList<String> hsl = new ArrayList<>();
                    hsl.add(shortName);
                    hsl.add(marketCap);
                    hsl.add(price);
                    hsl.add(lastchange);
                    hsl.add(String.valueOf(mChangep));
                    hasil.add(hsl);
                }
                return hasil;
            }
        } catch (Exception ex) {
            //System.err.println("Error: No such symbol");
            return null;
        }
    }

    private static String formatMarketCap(double marketCap) {
        double mil = 1_000_000; //juta
        double bil = 1_000_000_000; //milyar
        double tri = bil * 1000; //trilyun
        if (marketCap > tri) {
            return String.format("%.2fT", (marketCap / tri));
        } else if (marketCap > bil) {
            return String.format("%.2fB", (marketCap / bil));
        } else {
            return String.format("%.2fM", (marketCap / mil));
        }
    }
}

