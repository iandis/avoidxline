package com.avoprojects.avoidxline;

import org.apache.commons.io.IOUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Arrays;

@SpringBootApplication
public class AvoIDXLineApp {
	private static String formatMarketCap(double marketCap){
		double mil = 1_000_000; //juta
		double bil = 1_000_000_000; //milyar
		double tri = bil*1000; //trilyun
		if(marketCap>tri){
			return String.format("%.2fT",(marketCap/tri));
		}else if(marketCap>bil){
			return String.format("%.2fB",(marketCap/bil));
		}else{
			return String.format("%.2fM",(marketCap/mil));
		}
	}
	public static void main(String[] args) {
//		StocksAPI Stocks;
//		String symbol="IHSG";
//		try {
//			//if(symbol.equals("")){symbol=textMessageContent.getText().toUpperCase().substring(6);}
//			symbol=symbol.toLowerCase();
//			ClassLoader classLoader = AvoIDXLineApp.class.getClassLoader();
//			String idx_keys = IOUtils.toString(classLoader.getResourceAsStream("index_keywords.json"));
//			String idkey="NA";
//			JSONObject idxkeys = new JSONObject(idx_keys);
//			try{
//				idkey=idxkeys.getString(symbol);
//			}catch (Exception ignored){}
//			if (!idkey.equals("NA")) {
//				Stocks = new StocksAPI(idkey);
//				Stocks.join();
//				String[] dset = Stocks.getSingleQuote();
//				ArrayList<String> dataset = new ArrayList<String>();
//				dataset.add(symbol.toUpperCase());
//				dataset.addAll(Arrays.asList(dset).subList(0, 4));
//				if (dset[3].contains("+")) {
//					dataset.add("#2E7D32");
//				} else if (dset[3].contains("-")) {
//					dataset.add("#C62828");
//				} else {
//					dataset.add("#000000");
//				}
//				for(int i=0;i<6;i++){
//					System.out.println(dataset.get(i));
//				}
//			} else {
//				System.out.println(idkey);
//			}
//		}catch(Exception ignored){}
			SpringApplication.run(AvoIDXLineApp.class, args);
	}

}
