package com.avoprojects.avoidxline;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

class StocksAPI implements Runnable {
    private String symbol, longName, price, change, changep;
    private int res;
    private Thread t;
    StocksAPI(String symbol){
        this.symbol=symbol;
        t = new Thread(this);
        t.start();
    }
    public String[] getSingleQuote(){
        if(res>0){
            return new String[]{longName, price, change, changep};
        }else{
            return null;
        }
    }
    public void join(){
        try{
            t.join();
        }catch(InterruptedException e){
            //e.printStackTrace();
        };
    }
    @Override
    public void run() {
        try {
            //yahoo finance api url
            String yfapiUrl = "https://query1.finance.yahoo.com/v7/finance/quote?symbols=";
            URL url = new URL(yfapiUrl +symbol);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            // optional default is GET
            con.setRequestMethod("GET");
            //add request header
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            int responseCode = con.getResponseCode();
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
            JSONObject yfresult = yfjson.getJSONObject("quoteResponse").getJSONArray("result").getJSONObject(0);
            if(yfresult.isEmpty()){
                res=0;
            }else{
                res=1;
                longName = yfresult.getString("longName");
                double mPrice = yfresult.getDouble("regularMarketPrice");
                double mChange = yfresult.getDouble("regularMarketChange");
                double mChangep = yfresult.getDouble("regularMarketChangePercent");
                price = String.format("%.2f",mPrice);
                change = String.format(mChange > 0 ? "+%.2f" : "%.2f",mChange);
                changep = String.format(mChangep > 0 ? "+%.2f%%" : "%.2f%%",mChangep);
            }
        } catch (Exception ex) {
            //System.err.println("Error: No such symbol");
            res = 0;
        }
    }
}
