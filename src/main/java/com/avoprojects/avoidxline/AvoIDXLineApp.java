package com.avoprojects.avoidxline;

import org.apache.commons.io.IOUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.json.JSONObject;
import java.util.ArrayList;

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
//		try {
//			ClassLoader classLoader = AvoIDXLineApp.class.getClassLoader();
//			ArrayList<String> flexText = new ArrayList<String>();
//			flexText.add("BBCA");
//			flexText.add("Bank Central Asia");
//			flexText.add(formatMarketCap(1_000_000_000*800));
//			flexText.add("32000");
//			flexText.add("+62 (+1.53%)");
//			flexText.add("#2E7D32");
//			String flexTemplate = IOUtils.toString(classLoader.getResourceAsStream("flex_stock.json"));
//			for(int i=1; i<=6;i++){
//				flexTemplate=flexTemplate.replaceAll("Text"+i,flexText.get(i-1));
//			}
//			//JSONObject flexT = new JSONObject(flexTemplate);
//			//flexTemplate=flexT.toString();
//			/*flexTemplate = String.format(flexTemplate,
//					flexText.get(0),
//					flexText.get(1),
//					flexText.get(2),
//					flexText.get(3),
//					flexText.get(4),
//					flexText.get(5));*/
//			System.out.println(flexTemplate);
			SpringApplication.run(AvoIDXLineApp.class, args);
//		}catch(Exception e){
//			e.printStackTrace();
//		}
	}

}
