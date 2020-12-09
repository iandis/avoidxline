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
		/*String teks = "+nama Iandi Santulus\n+status Yuk Nabung";
		String nama = teks.substring(6,teks.indexOf("\n+status"));
		String status = teks.substring(teks.indexOf("+status ")+8);
		System.out.println(nama);
		System.out.println(status);*/
		SpringApplication.run(AvoIDXLineApp.class, args);
	}

}
