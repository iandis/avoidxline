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
//		String teks = "+nama Iandi\n+status Anjay";
//		String nama = teks.substring(6,teks.indexOf("\n+status"));
//		String status = teks.substring(teks.indexOf("+status ")+8);
//		if (!nama.equals("##") && !status.equals("##")) {
//			System.out.println("Sukses ganti nama menjadi " + nama + "\ndan status menjadi " + status);
//		} else if (!nama.equals("##")) {
//			System.out.println("Sukses ganti nama menjadi " + nama);
//		} else if (!status.equals("##")) {
//			System.out.println("Sukses ganti status menjadi " + status);
//		} else if (nama.equals("##") && status.equals("##")) {
//			System.out.println("Sukses gak ganti apa-apa:)");
//		} else {
//			System.out.println("Yah sepertinya ada yang salah dengan Avo:(");
//		}
		SpringApplication.run(AvoIDXLineApp.class, args);
	}

}
