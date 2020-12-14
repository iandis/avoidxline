package com.avoprojects.avoidxline;
//import com.avoprojects.avoidxline.database.Dao;
//import com.avoprojects.avoidxline.database.DaoImpl;
//import com.avoprojects.avoidxline.model.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.context.annotation.Bean;
//import org.springframework.jdbc.datasource.DriverManagerDataSource;
//import javax.sql.DataSource;
//import java.util.*;
//import java.util.List;
//import java.util.Map;
//import java.util.function.BiConsumer;
//import org.json.JSONObject;
//import org.json.JSONArray;
//
//import java.util.HashMap;

@SpringBootApplication
public class AvoIDXLineApp {

	public static void main(String[] args) {
//		String jsonDataString = "{\n" +
//				"  \"type\": \"carousel\",\n" +
//				"  \"contents\": [\n" +
//				"    {\n" +
//				"      \"type\": \"bubble\",\n" +
//				"      \"header\": {\n" +
//				"        \"type\": \"box\",\n" +
//				"        \"layout\": \"vertical\",\n" +
//				"        \"contents\": [\n" +
//				"          {\n" +
//				"            \"type\": \"text\",\n" +
//				"            \"text\": \"Text1\",\n" +
//				"            \"color\": \"#FAFAFA\",\n" +
//				"            \"weight\": \"bold\",\n" +
//				"            \"align\": \"center\",\n" +
//				"            \"size\": \"xl\"\n" +
//				"          }\n" +
//				"        ]\n" +
//				"      },\n" +
//				"      \"body\": {\n" +
//				"        \"type\": \"box\",\n" +
//				"        \"layout\": \"horizontal\",\n" +
//				"        \"contents\": [\n" +
//				"          {\n" +
//				"            \"type\": \"text\",\n" +
//				"            \"text\": \"Laba/Rugi:\",\n" +
//				"            \"weight\": \"bold\",\n" +
//				"            \"align\": \"start\"\n" +
//				"          },\n" +
//				"          {\n" +
//				"            \"type\": \"text\",\n" +
//				"            \"text\": \"LabaRugi%\",\n" +
//				"            \"weight\": \"bold\",\n" +
//				"            \"align\": \"end\"\n" +
//				"          }\n" +
//				"        ]\n" +
//				"      },\n" +
//				"      \"footer\": {\n" +
//				"        \"type\": \"box\",\n" +
//				"        \"layout\": \"vertical\",\n" +
//				"        \"spacing\": \"sm\",\n" +
//				"        \"contents\": [\n" +
//				"SeparatorSimbol\n" +
//				"          {\n" +
//				"            \"type\": \"separator\",\n" +
//				"            \"margin\": \"md\"\n" +
//				"          },\n" +
//				"          {\n" +
//				"            \"type\": \"box\",\n" +
//				"            \"layout\": \"vertical\",\n" +
//				"            \"contents\": [\n" +
//				"              {\n" +
//				"                \"type\": \"button\",\n" +
//				"                \"action\": {\n" +
//				"                  \"type\": \"message\",\n" +
//				"                  \"label\": \"Kembali ke Menu\",\n" +
//				"                  \"text\": \"menu\"\n" +
//				"                },\n" +
//				"                \"style\": \"primary\",\n" +
//				"                \"color\": \"#2196F3\"\n" +
//				"              }\n" +
//				"            ]\n" +
//				"          },\n" +
//				"          {\n" +
//				"            \"type\": \"spacer\",\n" +
//				"            \"size\": \"sm\"\n" +
//				"          }\n" +
//				"        ],\n" +
//				"        \"flex\": 0\n" +
//				"      },\n" +
//				"      \"styles\": {\n" +
//				"        \"header\": {\n" +
//				"          \"backgroundColor\": \"#0D47A1\"\n" +
//				"        },\n" +
//				"        \"body\": {\n" +
//				"          \"backgroundColor\": \"#EEEEEE\"\n" +
//				"        }\n" +
//				"      }\n" +
//				"    }\n" +
//				"  ]\n" +
//				"}";
//		String tambahan="          {\n" +
//				"            \"type\": \"box\",\n" +
//				"            \"layout\": \"baseline\",\n" +
//				"            \"contents\": [\n" +
//				"              {\n" +
//				"                \"type\": \"text\",\n" +
//				"                \"text\": \"SimbolX\",\n" +
//				"                \"flex\": 1,\n" +
//				"                \"color\": \"#2196F3\",\n" +
//				"                \"weight\": \"bold\",\n" +
//				"                \"align\": \"start\",\n" +
//				"                \"action\": {\n" +
//				"                  \"type\": \"message\",\n" +
//				"                  \"text\": \"saham SimbolX\"\n" +
//				"                }\n" +
//				"              },\n" +
//				"              {\n" +
//				"                \"type\": \"text\",\n" +
//				"                \"text\": \"NamaX\",\n" +
//				"                \"flex\": 1,\n" +
//				"                \"align\": \"start\"\n" +
//				"              },\n" +
//				"              {\n" +
//				"                \"type\": \"text\",\n" +
//				"                \"flex\": 0,\n" +
//				"                \"weight\": \"bold\",\n" +
//				"                \"text\": \"❌\",\n" +
//				"                \"action\": {\n" +
//				"                  \"type\": \"message\",\n" +
//				"                  \"text\": \"-portofolio SimbolX\"\n" +
//				"                }\n" +
//				"              }\n" +
//				"            ]\n" +
//				"          },\nSeparatorSimbol";
//		/*ArrayList<Map<String,String>> dt = new ArrayList<>();
//		for(int i=0;i<3;i++) {
//			Map<String,String> dd = new HashMap<>();
//			dd.put("simbol", "simbol-"+(i+1));
//			dd.put("nama", "nama-"+(i+1));
//			dt.add(dd);
//		}
//		for(int i=0;i<3;i++){
//			Map<String,String> dd;
//			dd=dt.get(i);
//			String tbh=tambahan.replaceAll("SimbolX",dd.get("simbol"));
//			tbh=tbh.replaceAll("NamaX",dd.get("nama"));
//			jsonDataString=jsonDataString.replaceAll("SeparatorSimbol",tbh);
//		}
//		jsonDataString=jsonDataString.replaceAll("SeparatorSimbol","");*/
//		System.out.print(tambahan);
		SpringApplication.run(AvoIDXLineApp.class, args);
	}
}
