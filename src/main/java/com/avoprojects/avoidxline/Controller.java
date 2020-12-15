package com.avoprojects.avoidxline;

import com.avoprojects.avoidxline.database.DbSvc;
import com.avoprojects.avoidxline.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.client.LineSignatureValidator;
import com.linecorp.bot.client.MessageContentResponse;
//import com.linecorp.bot.model.Multicast;
//import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.*;
import com.linecorp.bot.model.event.message.*;
import com.linecorp.bot.model.event.source.*;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.FlexMessage;
import com.linecorp.bot.model.message.StickerMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.flex.container.FlexContainer;
import com.linecorp.bot.model.objectmapper.ModelObjectMapper;
import com.linecorp.bot.model.profile.UserProfileResponse;
import org.apache.commons.io.IOUtils;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.io.InputStream;

@RestController
public class Controller {
    private static String[] keywords=new String[]{
            /*"jadwal uts",*/"saham",
            "indeks","index",
            "profile","edit profile",
            "+nama","+bio",
            "+daftar","daftar","-leave", "menu",
            "+portofolio","+watchlist",
            "portofolio","watchlist",
            "-portofolio","-watchlist",
            "intipindex","intipindeks"};
    //private String plus="➕";
    @Autowired
    @Qualifier("lineMessagingClient")
    private LineMessagingClient lineMessagingClient;
    @Autowired
    @Qualifier("lineSignatureValidator")
    private LineSignatureValidator lineSignatureValidator;

    @Autowired
    private DbSvc Dbs;

    @RequestMapping(value="/webhook", method= RequestMethod.POST)
    public ResponseEntity<String> callback(
            @RequestHeader("X-Line-Signature") String xLineSignature,
            @RequestBody String eventsPayload)
    {
        try {
            if (!lineSignatureValidator.validateSignature(eventsPayload.getBytes(), xLineSignature)) {
                throw new RuntimeException("Invalid Signature Validation");
            }
            // parsing event
            ObjectMapper objectMapper = ModelObjectMapper.createNewObjectMapper();
            EventsModel eventsModel = objectMapper.readValue(eventsPayload, EventsModel.class);

            eventsModel.getEvents().forEach((event)->{
                if (event instanceof MessageEvent) {
                    if (event.getSource() instanceof GroupSource || event.getSource() instanceof RoomSource) {
                        handleGroupRoomChats((MessageEvent) event);
                    } else {
                        handleOneOnOneChats((MessageEvent) event);
                    }
                }else if(event instanceof JoinEvent){
                    if (event.getSource() instanceof GroupSource || event.getSource() instanceof RoomSource){
                        greetingMsg(((ReplyEvent) event).getReplyToken(),1);
                    }
                }else if(event instanceof FollowEvent){
                    greetingMsg(((ReplyEvent) event).getReplyToken(),2);
                }
            });

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
    private void replyMenu(String replyToken){
        ArrayList<String> msg = new ArrayList<>();
        msg.add("default");
        replyFlexMessage(replyToken,1,msg);
    }
    private void greetingMsg(String replyToken,int JoinOrFollow){ //1: Join //2: Follow
        if(JoinOrFollow==1) {
            List<Message> msgArray = new ArrayList<>();
            msgArray.add(new TextMessage(
                    "Hai!\n" +
                            "Terimakasih telah menambahkan Avo kesini"));
            msgArray.add(new StickerMessage("1", "2"));
            msgArray.add(new TextMessage(
                    "Ketik \"menu\" kalau kamu mau liat list perintahnya ya.\n" +
                            "Tapi pastikan kamu udah jadiin aku teman kamu dulu okey?:)"));
            ReplyMessage replyMessage = new ReplyMessage(replyToken, msgArray);
            reply(replyMessage);
        }else if(JoinOrFollow==2){
            replyMenu(replyToken);
        }
    }
    private void handleOneOnOneChats(MessageEvent event) {
        if  (event.getMessage() instanceof AudioMessageContent
                || event.getMessage() instanceof ImageMessageContent
                || event.getMessage() instanceof VideoMessageContent
                || event.getMessage() instanceof FileMessageContent
        ) {
            handleContentMessage(event);
        } else if(event.getMessage() instanceof TextMessageContent) {
            handleTextMessage(event);
        } else {
            replyText(event.getReplyToken(), "Unknown Message");
        }
    }
    private void handleGroupRoomChats(MessageEvent event) {
        String senderId   = event.getSource().getSenderId();
        if(senderId!=null) {
            handleTextMessage(event);
        } else {
            replyText(event.getReplyToken(), "Hai, tambahin Avo dulu dong jadi teman kamu:)");
        }
    }
    private void handleContentMessage(MessageEvent event) {
        Random random = new Random();
        int maxrand=6;
        int randInt=random.nextInt(maxrand)+1;
        switch (randInt) {
            case 1:
            case 4:
                List<Message> msgArray = new ArrayList<>();
                msgArray.add(new TextMessage("Apaan tuh."));
                msgArray.add(new StickerMessage("1", "17"));
                ReplyMessage replyMessage = new ReplyMessage(((ReplyEvent) event).getReplyToken(), msgArray);
                reply(replyMessage);
                break;
            case 3:
            case 6:
                List<Message> msgArray2 = new ArrayList<>();
                msgArray2.add(new TextMessage("Dahlah males."));
                msgArray2.add(new StickerMessage("1", "113"));
                ReplyMessage replyMessage2 = new ReplyMessage(((ReplyEvent) event).getReplyToken(), msgArray2);
                reply(replyMessage2);
                break;
            case 2:
            case 5:
                List<Message> msgArray3 = new ArrayList<>();
                msgArray3.add(new TextMessage("Hoam."));
                msgArray3.add(new StickerMessage("1", "405"));
                ReplyMessage replyMessage3 = new ReplyMessage(((ReplyEvent) event).getReplyToken(), msgArray3);
                reply(replyMessage3);
                break;
        }
    }
    private void handleTextMessage(MessageEvent event) {
        TextMessageContent textMessageContent = (TextMessageContent) event.getMessage();
        String msg = textMessageContent.getText();
        for (String keyword : keywords) {
            if (msg.length() >= keyword.length()) {
                if (msg.toLowerCase().substring(0, keyword.length()).equals(keyword)) {
                    String symbol = "";
                    String userid = event.getSource().getUserId();
                    StocksAPI Stocks = new StocksAPI();
                    switch (keyword) {
                        case "menu":
                            replyMenu(event.getReplyToken());
                            return;
                        case "intipindex":
                        case "intipindeks":
                            replyFlexIndices(event.getReplyToken());
                            return;
                        case "watchlist":
                            replyWlistFlex(event.getReplyToken(),userid);
                            return;
                        case "+watchlist":
                            if(Dbs.isUserExist(userid)){
                                symbol = msg.toUpperCase().substring("+watchlist ".length());
                                String isidx = isIndex(symbol.toLowerCase());
                                String sim=isidx.equals("NA") ? symbol + ".JK" : isidx;
                                ArrayList<ArrayList<String>> dataaset=Stocks.getQuote(new String[]{sim});
                                if (dataaset != null) {
                                    int b = Dbs.insertWlist(userid,sim);
                                    if(b>0) {
                                        replyWlistFlex(event.getReplyToken(),userid, "Yeay! Berhasil menambahkan " + symbol + " ke watchlist kamu.");
                                    }else if(b==-1){
                                        replyFallback(event.getReplyToken(),18); //simbol sudah ada
                                    }else if(b==-2){
                                        replyFallback(event.getReplyToken(),16); //porto penuh
                                    }else{
                                        replyFallback(event.getReplyToken(),14);
                                    }
                                }else{
                                    replyText(event.getReplyToken(), "Index " +symbol + " tidak ditemukan.");
                                }
                            }else{
                                replyFallback(event.getReplyToken(),3);
                            }
                            return;
                        case "-watchlist":
                            if(Dbs.isUserExist(userid)){
                                symbol = msg.toUpperCase().substring("-watchlist ".length());
                                String isidx = isIndex(symbol.toLowerCase());
                                String sim=isidx.equals("NA") ? symbol + ".JK" : isidx;
                                int b = Dbs.deleteWlist(userid,sim);
                                if (b>0) {
                                    replyWlistFlex(event.getReplyToken(),userid, symbol + " berhasil dihapus dari watchlist kamu.");
                                }else if(b==-1){
                                    replyFallback(event.getReplyToken(),13); //Watchlist kosong
                                }else if(b==-2){
                                    replyText(event.getReplyToken(), symbol + " tidak ditemukan di watchlist kamu."); //simbol tidak ditemukan
                                }else{
                                    replyFallback(event.getReplyToken(),14);
                                }
                            }else{
                                replyFallback(event.getReplyToken(),3);
                            }
                            return;
                        case "portofolio":
                            replyPortoFlex(event.getReplyToken(),userid);
                            return;
                        case "+portofolio":
                            if(Dbs.isUserExist(userid)){
                                symbol = msg.toUpperCase().substring("+portofolio ".length());
                                if(!isIndex(symbol).equals("NA")){
                                    replyFallback(event.getReplyToken(),19); //indeks tidak bsa di porto
                                    return;
                                }
                                ArrayList<ArrayList<String>> dataaset=Stocks.getQuote(new String[]{symbol + ".JK"});
                                if (dataaset != null) {
                                    int b = Dbs.insertPorto(userid,symbol+".JK");
                                    if(b>0) {
                                        replyPortoFlex(event.getReplyToken(),userid, "Yeay! Berhasil menambahkan " + symbol + " ke portofolio kamu.");
                                    }else if(b==-1){
                                        replyFallback(event.getReplyToken(),17); //simbol sudah ada
                                    }else if(b==-2){
                                        replyFallback(event.getReplyToken(),15); //porto penuh
                                    }else{
                                        replyFallback(event.getReplyToken(),14);
                                    }
                                }else{
                                    replyText(event.getReplyToken(), "Saham " +symbol + " tidak ditemukan.");
                                }
                            }else{
                                replyFallback(event.getReplyToken(),3);
                            }
                            return;
                        case "-portofolio":
                            if(Dbs.isUserExist(userid)){
                                symbol = msg.toUpperCase().substring("-portofolio ".length());
                                int b = Dbs.deletePorto(userid,symbol+".JK");
                                if (b>0) {
                                    replyPortoFlex(event.getReplyToken(),userid,symbol + " berhasil dihapus dari portofolio kamu.");
                                }else if(b==-1){
                                    replyFallback(event.getReplyToken(),12); //porto kosong
                                }else if(b==-2){
                                    replyText(event.getReplyToken(), symbol + " tidak ditemukan di portofolio kamu."); //simbol tidak ditemukan
                                }else{
                                    replyFallback(event.getReplyToken(),14);
                                }
                            }else{
                                replyFallback(event.getReplyToken(),3);
                            }
                            return;
                        case "saham":
                            symbol = msg.toUpperCase().substring(6); //misal teks "saham BBCA", berarti memisahkan teks "saham " dengan "BBCA"
                            ArrayList<ArrayList<String>> dataaset = isIndex(symbol.toLowerCase()).equals("NA") ? Stocks.getQuote(new String[]{symbol + ".JK"}) : null;
                            if (dataaset != null) {
                                ArrayList<String> dataset = new ArrayList<>();
                                dataset.add(symbol);
                                for(int i=0;i<4;i++){
                                    dataset.add(dataaset.get(0).get(i));
                                }
                                if (dataset.get(4).contains("+")) {
                                    dataset.add("#2E7D32");
                                } else if (dataset.get(4).contains("-")) {
                                    dataset.add("#C62828");
                                } else {
                                    dataset.add("#000000");
                                }
                                dataset.add(symbol);
                                dataset.add(symbol);
                                replyFlexMessage(event.getReplyToken(), 2, dataset);
                            } else {
                                replyText(event.getReplyToken(), "Saham " +symbol + " tidak ditemukan.");
                            }
                            return;
                        case "indeks":
                            symbol = msg.toLowerCase().substring(7);
                        case "index":
                                if (symbol.equals("")) {
                                    symbol = msg.toLowerCase().substring(6);
                                }
                                String idkey = isIndex(symbol);
                                if (!idkey.equals("NA")) {
                                    ArrayList<ArrayList<String>> dset=Stocks.getQuote(new String[]{idkey});
                                    ArrayList<String> dataset = new ArrayList<>();
                                    dataset.add(symbol.toUpperCase());
                                    for(int i=0;i<4;i++){
                                        dataset.add(dset.get(0).get(i));
                                    }
                                    if (dataset.get(4).contains("+")) {
                                        dataset.add("#2E7D32");
                                    } else if (dataset.get(4).contains("-")) {
                                        dataset.add("#C62828");
                                    } else {
                                        dataset.add("#000000");
                                    }
                                    dataset.add(symbol);
                                    dataset.add(symbol);
                                    replyFlexMessage(event.getReplyToken(), 2, dataset);
                                } else {
                                    replyText(event.getReplyToken(), "Index " +symbol.toUpperCase() + " tidak ditemukan.");
                                }
                            return;
                        case "profile":
                                if(Dbs.isUserExist(userid)){
                                    User usr = Dbs.getUserProfile(userid);
                                    ArrayList<String> user = new ArrayList<>();
                                    user.add(usr.uname);
                                    user.add(usr.ustatus);
                                    user.add(getProfile(userid).getPictureUrl());
                                    replyFlexMessage(event.getReplyToken(), 3, user);
                                }else{
                                    replyFallback(event.getReplyToken(),3);
                                }
                                return;
                        case "daftar":
                        case "+daftar":
                            if(Dbs.isUserExist(userid)){
                                List<Message> msgArray = new ArrayList<>();
                                msgArray.add(new TextMessage(String.format("Hai %s! Kamu udah terdaftar kok, tenang aja!",getProfile(userid).getDisplayName())));
                                msgArray.add(new StickerMessage("1", "13"));
                                ReplyMessage replyMessage = new ReplyMessage(((ReplyEvent) event).getReplyToken(), msgArray);
                                reply(replyMessage);
                            }else{
                                if(!msg.substring(0,6).equals("daftar")) {
                                    if (msg.contains("\n+bio")) {
                                        String nama = msg.substring(8, msg.indexOf("\n+bio")); //8 didapat dari length("+daftar ")
                                        if (nama.equals("")) {
                                            replyFallback(event.getReplyToken(), 5);
                                            return;
                                        } else if (nama.length() > 20) {
                                            replyFallback(event.getReplyToken(), 6);
                                            return;
                                        }
                                        String bio = msg.substring(msg.indexOf("\n+bio") + 6); //6 didapat dari length("\n+bio ")
                                        if (bio.equals("")) {
                                            replyFallback(event.getReplyToken(), 7);
                                            return;
                                        } else if (bio.length() > 20) {
                                            replyFallback(event.getReplyToken(), 8);
                                            return;
                                        }
                                        Dbs.createUser(userid, nama, bio);
                                        replyText(event.getReplyToken(), "Yeay! Pendaftaran kamu sukses!");
                                    } else {
                                        replyFallback(event.getReplyToken(), 4);
                                    }
                                }else{
                                    replyFallback(event.getReplyToken(),11);
                                }
                            }
                            return;
                        case "edit profile":
                            if (event.getSource() instanceof GroupSource || event.getSource() instanceof RoomSource) {
                                replyFallback(event.getReplyToken(),10);
                            } else {
                                if(Dbs.isUserExist(userid)) {
                                    replyFallback(event.getReplyToken(),9);
                                }else {
                                    replyFallback(event.getReplyToken(),3);
                                }
                            }
                            return;
                        case "+nama":
                            if (event.getSource() instanceof GroupSource || event.getSource() instanceof RoomSource) {
                                replyFallback(event.getReplyToken(),10);
                            } else {
                                if(!Dbs.isUserExist(userid)) {
                                    replyFallback(event.getReplyToken(),3);
                                    return;
                                }
                                if (msg.contains("\n+bio")) {
                                    String nama = msg.substring(6, msg.indexOf("\n+bio")); //6 didapat dari length("+nama ")
                                    if(nama.equals("")){
                                        replyFallback(event.getReplyToken(),5);
                                        return;
                                    }else if(nama.length()>20){
                                        replyFallback(event.getReplyToken(),6);
                                        return;
                                    }
                                    String bio = msg.substring(msg.indexOf("\n+bio") + 6); //6 didapat dari length("\n+bio ")
                                    if(bio.equals("")){
                                        replyFallback(event.getReplyToken(),7);
                                        return;
                                    }else if(bio.length()>20){
                                        replyFallback(event.getReplyToken(),8);
                                        return;
                                    }
                                    if (!nama.equals("##") && !bio.equals("##")) {
                                        Dbs.updateUser(userid,nama,bio);
                                        replyText(event.getReplyToken(),"Sukses ganti nama menjadi " + nama + "\ndan bio menjadi " + bio);
                                    } else if (!nama.equals("##")) {
                                        Dbs.updateUsername(userid,nama);
                                        replyText(event.getReplyToken(),"Sukses ganti nama menjadi " + nama);
                                    } else if (!bio.equals("##")) {
                                        Dbs.updateUserbio(userid,bio);
                                        replyText(event.getReplyToken(),"Sukses ganti bio menjadi " + bio);
                                    } else if (nama.equals("##") && bio.equals("##")) {
                                        replyText(event.getReplyToken(),"Sukses gak ganti apa-apa:)");
                                    } else {
                                        replyFallback(event.getReplyToken(),14);
                                    }
                                } else {
                                    replyFallback(event.getReplyToken(),2);
                                }
                            }
                            return;
                        case "+bio":
                            if (event.getSource() instanceof GroupSource || event.getSource() instanceof RoomSource) {
                                replyFallback(event.getReplyToken(),10);
                            } else {
                                replyFallback(event.getReplyToken(),2);
                            }
                        case "-leave":
                            if (event.getSource() instanceof GroupSource) {
                                replyText(event.getReplyToken(), "Yah:( Yaudah deh kalo gitu Avo pamit dulu ya.");
                                leaveGroup(event.getSource().getSenderId());
                            } else if (event.getSource() instanceof RoomSource) {
                                replyText(event.getReplyToken(), "Yah:( Yaudah deh kalo gitu Avo pamit dulu ya.");
                                leaveRoom(event.getSource().getSenderId());
                            } else {
                                replyText(event.getReplyToken(), "Duh jangan usir Avo dong:(");
                            }
                            return;
                    }
                }
            }
        }
        if((event.getSource() instanceof GroupSource) || (event.getSource() instanceof RoomSource)) {
            //
        }else{
            replyFallback(event.getReplyToken(),1);
        }
    }
    private void replyFlexIndices(String replyToken){
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            String flexTemplate=IOUtils.toString(classLoader.getResourceAsStream("carousel_flex_template.json"));
            JSONObject yfjson = new JSONObject(flexTemplate);
            Iterator<String> it = yfjson.keys();
            String[]twlist = new String[36];
            int j=0;
            while (it.hasNext()) {
                twlist[j] = it.next();j++;
            }
            StocksAPI Stocks = new StocksAPI();
            ArrayList<ArrayList<String>> dataaset = Stocks.getQuote(twlist);
            String flextwlist = IOUtils.toString(classLoader.getResourceAsStream("flex_topwlist.json"));
            String twlistbubble = IOUtils.toString(classLoader.getResourceAsStream("flex_topwlist_bubble.json"));
            for (int i = 0; i < 36; i++) {
                String ftw = flextwlist;
                ftw = ftw.replaceAll("SimbolX", yfjson.getString(twlist[i]).toUpperCase());
                double changex = Double.parseDouble(dataaset.get(i).get(4));
                String changepx = String.format(changex > 0 ? "+%.2f%%" : "%.2f%%", changex);
                ftw = ftw.replaceAll("ChangeX", changepx);
                String color = "#000000";
                if (changex > 0) {
                    color = "#2E7D32"; //hijau
                } else if (changex < 0) {
                    color = "#C62828"; //merah
                }
                ftw = ftw.replaceAll("ColorCX", color);
                String shortName = dataaset.get(i).get(0);
                if (shortName.equals("N/A")) {
                    String simx = dataaset.get(i).get(5);
                    if (simx.contains("^JK")) {
                        shortName = simx.substring(simx.indexOf("^JK") + "^JK".length());
                    } else if (simx.contains(".JK")) {
                        shortName = simx.replaceAll(".JK", "");
                    } else {
                        shortName = simx;
                    }
                }
                ftw = ftw.replaceAll("shortNameX", shortName);
                if((i+1) % 6 == 0 && (i+1)!=36){
                    String bub = twlistbubble;
                    twlistbubble = twlistbubble.replaceAll("SeparatorSimbol","");
                    twlistbubble = twlistbubble.replaceAll("SeparatorCarousel",bub);
                }
                twlistbubble = twlistbubble.replaceAll("SeparatorSimbol", ftw);
            }
            twlistbubble = twlistbubble.replaceAll("SeparatorSimbol", "");
            twlistbubble = twlistbubble.replaceAll(",SeparatorCarousel", "");
            twlistbubble = twlistbubble.replaceAll("Top Watchlist","Daftar Kode Indeks");
            flexTemplate = flexTemplate.replaceAll("SeparatorCarousel", twlistbubble);
            ObjectMapper objectMapper = ModelObjectMapper.createNewObjectMapper();
            FlexContainer flexContainer = objectMapper.readValue(flexTemplate, FlexContainer.class);
            ReplyMessage replyMessage = new ReplyMessage(replyToken,new FlexMessage("Daftar Kode Indeks",flexContainer));
            reply(replyMessage);
        }catch(Exception e){
            ReplyMessage replyMessage = new ReplyMessage(replyToken,new TextMessage(e.toString()));
            reply(replyMessage);
        }
    }
    private String isIndex(String simbol){
        return isIndex(simbol,1);
    }
    private String isIndex(String simbol,int tipe){ //1: cek keyword 2: cek value
        String idkey="NA";
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            String idx_keys;
            if(tipe==1) {
                idx_keys = IOUtils.toString(classLoader.getResourceAsStream("index_keywords.json"));
            }else{
                idx_keys = IOUtils.toString(classLoader.getResourceAsStream("index_keyreversed.json"));
            }
            JSONObject idxkeys = new JSONObject(idx_keys);
            try {
                 idkey = idxkeys.getString(simbol);
            } catch (Exception ignored) {
            }
        }catch(Exception ignored){
        }
        return idkey;
    }
    private void replyPortoFlex(String replyToken, String uid, String msg){
        replyPWFlex(replyToken,uid,1,msg);
    }
    private void replyPortoFlex(String replyToken, String uid){
        replyPWFlex(replyToken,uid,1,"default");
    }
    private void replyWlistFlex(String replyToken, String uid, String msg){
        replyPWFlex(replyToken,uid,2,msg);
    }
    private void replyWlistFlex(String replyToken, String uid){
        replyPWFlex(replyToken,uid,2,"default");
    }
    private void replyPWFlex(String replyToken, String uid, int PW,String additionalMsg){ //1:porto 2:watchlist
        List<PortoWatchlist> porto = (PW == 1 ? Dbs.getUserPorto(uid) : Dbs.getUserWlist(uid));
        StocksAPI Stocks = new StocksAPI();
        if(porto.size()==0){
            if(additionalMsg.equals("default")){
                replyFallback(replyToken,PW==1 ? 12 : 13);
            }else{
                replyText(replyToken,additionalMsg);
            }
            return;
        }
        String[] simbols=new String[porto.size()];
        for (int i = 0; i < porto.size(); i++) {
            simbols[i]=porto.get(i).simbol;
        }
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            String carousel = IOUtils.toString(classLoader.getResourceAsStream("carousel_flex_template.json"));
            String bubble = IOUtils.toString(classLoader.getResourceAsStream("bubble_flex_template.json"));
            String portowlist = IOUtils.toString(classLoader.getResourceAsStream("portowlist_fraction.json"));
            ArrayList<ArrayList<String>> dataaset = Stocks.getQuote(simbols);
            double changep=0;
            int dtsize=dataaset.size();
            for(int i = 0; i<dtsize;i++){
                String portox=portowlist;
                if(PW==1) {
                    portox = portox.replaceAll("SimbolX", simbols[i].replaceAll(".JK", ""));
                }else{
                    String simidx=isIndex(simbols[i],2);
                    if(!simidx.equals("NA")){
                        portox=portox.replaceAll("saham SimbolX","index SimbolX");
                        portox=portox.replaceAll("-portofolio SimbolX","-watchlist SimbolX");
                        portox=portox.replaceAll("SimbolX",simidx.toUpperCase());
                    }else{
                        portox = portox.replaceAll("SimbolX", simbols[i].replaceAll(".JK", ""));
                    }
                }
                double changex=Double.parseDouble(dataaset.get(i).get(4));
                changep=changep+changex;
                String changepx = String.format(changex > 0 ? "+%.2f%%" : "%.2f%%", changex);
                portox=portox.replaceAll("ChangeX",changepx);
                String color="#000000";
                if (changex>0) {
                    color="#2E7D32"; //hijau
                } else if (changex<0) {
                    color="#C62828"; //merah
                }
                String shortName = dataaset.get(i).get(0);
                if(shortName.equals("N/A")){
                    String sim = dataaset.get(i).get(5);
                    if(sim.contains("^JK")){
                        shortName=sim.substring(sim.indexOf("^JK")+"^JK".length());
                    }else if(sim.contains(".JK")){
                        shortName=sim.replaceAll(".JK","");
                    }else{
                        shortName=sim;
                    }
                }
                portox=portox.replaceAll("shortNameX",shortName);
                portox=portox.replaceAll("ColorCX",color);
                if(i==10){
                    String bub = bubble;
                    bubble = bubble.replaceAll("SeparatorSimbol","");
                    bubble = bubble.replaceAll("SeparatorCarousel",bub);
                }
                bubble = bubble.replaceAll("SeparatorSimbol", portox);
            }
            bubble=bubble.replaceAll("Text1",PW==1 ? "Portofolioku":"Watchlistku");
            changep=changep/dtsize;
            String changepx = String.format(changep > 0 ? "+%.2f%%" : "%.2f%%", changep);
            bubble=bubble.replaceAll("LabaRugiX",PW==1 ? changepx : "--");
            String color="#000000";
            if (changep>0) {
                color="#2E7D32";
            } else if (changep<0) {
                color="#C62828";
            }
            bubble=bubble.replaceAll("ColorLR",PW==1 ?color : "#000000");
            bubble=bubble.replaceAll("SeparatorSimbol","");
            bubble=bubble.replaceAll(",SeparatorCarousel","");
            carousel=carousel.replaceAll("SeparatorCarousel",bubble);
            if(PW==2){
                carousel=carousel.replaceAll("Laba/Rugi:", "Kode");
                carousel=carousel.replaceAll("LabaRugiX", "Perubahan");
            }
            ObjectMapper objectMapper = ModelObjectMapper.createNewObjectMapper();
            FlexContainer flexContainer = objectMapper.readValue(carousel, FlexContainer.class);
            List<Message> rep = new ArrayList<>();
            rep.add(new FlexMessage(PW==1 ? "Portofolioku":"Watchlistku", flexContainer));
            if(!additionalMsg.equals("default")){rep.add(new TextMessage(additionalMsg));}
            ReplyMessage replyMessage= new ReplyMessage(replyToken,rep);
            reply(replyMessage);
        }catch(Exception e){
            //replyText(replyToken,e.toString());
            replyFallback(replyToken,14);
        }
    }
    private void replyFallback(String replyToken, int Fallbackcode){
        switch(Fallbackcode){
            case 1: //keyword salah
                ArrayList<String> arrMsg = new ArrayList<>();
                arrMsg.add("Keyword salah:(");
                replyFlexMessage(replyToken,1,arrMsg);
                break;
            case 2: //format edit profile salah
                List<String> multimsg = new ArrayList<>();
                multimsg.add(
                        "Aduh formatnya salah nih:(\n" +
                                "Formatnya:");
                multimsg.add(
                        "+nama Namabaru\n" +
                                "+bio Biobaru");
                multimsg.add("Untuk bagian yang gak ingin kamu ganti, cukup isi dengan \"##\" aja ya. Misalnya: nama ##\nNama & bio gak boleh kosong ya. Untuk nama maksimal 20 karakter, kalau bio maksimal 30 karakter.");
                replyMultiMsg(replyToken, multimsg);
                break;
            case 3: //akun belum terdaftar
                List<String> multimsg2 = new ArrayList<>();
                multimsg2.add("Akun kamu belum terdaftar:( Silahkan daftar dulu ya dengan ketik:");
                multimsg2.add("+daftar Nama\n+bio Biokamu");
                multimsg2.add("Misalnya: \n+daftar Avo's IDX\n+bio Yuk Nabung Saham!\nNama & bio gak boleh kosong ya. Untuk nama maksimal 20 karakter, kalau bio maksimal 30 karakter.");
                replyMultiMsg(replyToken, multimsg2);
                break;
            case 4: //format daftar akun salah
                List<String> multimsg3 = new ArrayList<>();
                multimsg3.add("Wah formatnya salah nih. Untuk daftar formatnya:");
                multimsg3.add("+daftar Nama\n+bio Biokamu");
                multimsg3.add("Misalnya: \n+daftar Avo's IDX\n+bio Yuk Nabung Saham!\nNama & bio gak boleh kosong ya. Untuk nama maksimal 20 karakter, kalau bio maksimal 30 karakter.");
                replyMultiMsg(replyToken, multimsg3);
                break;
            case 5: //nama tidak boleh kosong
                replyText(replyToken,"Namanya gak boleh kosong ya:)");
                break;
            case 6: //nama tidak boleh > 20 karakter
                replyText(replyToken,"Namanya gak boleh lebih dari 20 karakter ya:)");
                break;
            case 7: //bio tidak boleh kosong
                replyText(replyToken,"Bionya gak boleh kosong ya:)");
                break;
            case 8: //bio tidak boleh > 30 karakter
                replyText(replyToken,"Bionya gak boleh lebih dari 30 karakter ya:)");
                break;
            case 9: //format edit profile
                List<String> multimsg4 = new ArrayList<>();
                multimsg4.add(
                        "Tulis nama & bio yang mau kamu ubah ya.\n" +
                                "Formatnya:");
                multimsg4.add(
                        "+nama Namabaru\n" +
                                "+bio Biobaru");
                multimsg4.add("Untuk bagian yang gak ingin kamu ganti, cukup isi dengan \"##\" aja ya. Misalnya: nama ##\nNama & bio gak boleh kosong ya. Untuk nama maksimal 20 karakter, kalau bio maksimal 30 karakter.");
                replyMultiMsg(replyToken, multimsg4);
                break;
            case 10: //gak bisa edit profile di grup
                replyText(replyToken, "Duh maaf Avo gak bisa bantu edit profile kamu disini:(");
                break;
            case 11: //cara daftar
                List<String> multimsg5 = new ArrayList<>();
                multimsg5.add("Hai! Supaya kamu terdaftar, ketik seperti ini ya:");
                multimsg5.add("+daftar Nama\n+bio Biokamu");
                multimsg5.add("Misalnya: \n+daftar Avo's IDX\n+bio Yuk Nabung Saham!\nNama & bio gak boleh kosong ya. Untuk nama maksimal 20 karakter, kalau bio maksimal 30 karakter.");
                replyMultiMsg(replyToken, multimsg5);
                break;
            case 12: //porto kosong
                replyText(replyToken,"Aduh, portofolio kamu masih kosong:(");
                break;
            case 13: //watchlist kosong
                replyText(replyToken,"Aduh, watchlist kamu masih kosong:(");
                break;
            case 14: //unknown error
                replyText(replyToken,"Yah sepertinya ada yang salah dengan Avo:(");
                break;
            case 15: //porto penuh
                replyText(replyToken,"Wah, portofolio kamu sudah penuh. Maksimal isi portofolio adalah 10.");
                break;
            case 16: //watchlist penuh
                replyText(replyToken,"Wah, watchlist kamu sudah penuh. Maksimal isi watchlist adalah 20.");
                break;
            case 17: //simbol porto sudah ada
                replyText(replyToken,"Kode yang kamu daftarkan sudah ada ya di portofolio kamu.");
                break;
            case 18: //simbol porto sudah ada
                replyText(replyToken,"Kode yang kamu daftarkan sudah ada ya di watchlist kamu.");
                break;
            case 19: //tidak bisa menambahkan indeks ke porto
                replyText(replyToken,"Index tidak bisa ditambahkan ke portofolio ya:)");
                break;
        }
    }
    private String getTwList(String flexTemp){
        String flexTemplate=flexTemp;
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            List<TopWatchlist> twlist = Dbs.getTopWatchlist();
            int dtsize = twlist.size();
            if (dtsize == 0) {
                flexTemplate = flexTemplate.replaceAll(",SeparatorBubble", "");
            } else {
                String[] simbols = new String[Math.min(dtsize, 5)];
                for (int i = 0; i < Math.min(dtsize, 5); i++) {
                    simbols[i] = twlist.get(i).simbol;
                }
                StocksAPI Stocks = new StocksAPI();
                ArrayList<ArrayList<String>> dataaset = Stocks.getQuote(simbols);
                String flextwlist = IOUtils.toString(classLoader.getResourceAsStream("flex_topwlist.json"));
                String twlistbubble = IOUtils.toString(classLoader.getResourceAsStream("flex_topwlist_bubble.json"));
                for (int i = 0; i < Math.min(dtsize, 5); i++) {
                    String ftw = flextwlist;
                    String sim = isIndex(simbols[i], 2);
                    ftw = ftw.replaceAll("SimbolX", sim.equals("NA") ? simbols[i].replaceAll(".JK", "") : sim.toUpperCase());
                    double changex = Double.parseDouble(dataaset.get(i).get(4));
                    String changepx = String.format(changex > 0 ? "+%.2f%%" : "%.2f%%", changex);
                    ftw = ftw.replaceAll("ChangeX", changepx);
                    String color = "#000000";
                    if (changex > 0) {
                        color = "#2E7D32"; //hijau
                    } else if (changex < 0) {
                        color = "#C62828"; //merah
                    }
                    ftw = ftw.replaceAll("ColorCX", color);
                    String shortName = dataaset.get(i).get(0);
                    if (shortName.equals("N/A")) {
                        String simx = dataaset.get(i).get(5);
                        if (simx.contains("^JK")) {
                            shortName = simx.substring(simx.indexOf("^JK") + "^JK".length());
                        } else if (simx.contains(".JK")) {
                            shortName = simx.replaceAll(".JK", "");
                        } else {
                            shortName = simx;
                        }
                    }
                    ftw = ftw.replaceAll("shortNameX", shortName);
                    twlistbubble = twlistbubble.replaceAll("SeparatorSimbol", ftw);
                }
                twlistbubble = twlistbubble.replaceAll("SeparatorSimbol", "");
                twlistbubble = twlistbubble.replaceAll(",SeparatorCarousel", "");
                flexTemplate = flexTemplate.replaceAll("SeparatorBubble", twlistbubble);

            }
        }catch(Exception ignored){}
        return flexTemplate;
    }
    private void replyFlexMessage(String replyToken, int flextype, ArrayList<String> flexText) {
        try {
            //1: Menu awal, 2: Stock, 3: Profile, 4: Portofolio, 5: Watchlist
            ClassLoader classLoader = getClass().getClassLoader();
            String flexTemplate; FlexContainer flexContainer; ReplyMessage replyMessage=null;
            ObjectMapper objectMapper = ModelObjectMapper.createNewObjectMapper();
            if(flextype==1) {
                List<Message> msgArray = new ArrayList<>();
                flexTemplate = IOUtils.toString(classLoader.getResourceAsStream("flex_menu.json"));
                flexTemplate = getTwList(flexTemplate);
                flexContainer = objectMapper.readValue(flexTemplate, FlexContainer.class);
                if(!flexText.get(0).equals("default")) {
                    msgArray.add(new TextMessage(flexText.get(0)));
                }
                msgArray.add(new FlexMessage("Menu", flexContainer));
                msgArray.add(new TextMessage("Tips:\nKamu bisa cari kode saham yang mau kamu cek dengan memilih \"Intip Daftar Saham\". Setelah dapat, kamu bisa cek performa nya dengan keyword:"));
                msgArray.add(new TextMessage("saham kodesaham"));
                msgArray.add(new TextMessage("Kamu juga bisa intip beberapa indeks dengan memilih \"Daftar Kode Indeks\". Setelah itu, kamu bisa cek performa nya dengan keyword:"));
                msgArray.add(new TextMessage("index kodeindex"));
                replyMessage = new ReplyMessage(replyToken, msgArray);
            }else if(flextype==2){
                flexTemplate = IOUtils.toString(classLoader.getResourceAsStream("flex_stock.json"));
                for(int i=1; i<=flexText.size();i++){
                    if(i==7 && !isIndex(flexText.get(0).toLowerCase()).equals("NA")){
                        String tblPorto = "{\"type\":\"button\",\"height\":\"md\",\"action\":{\"type\":\"message\",\"label\":\"Tambah ke Portofolio\",\"text\":\"+portofolio Text7\"},\"color\":\"#2196F3\"},";
                        String s1 =flexTemplate.substring(0,flexTemplate.indexOf(tblPorto));
                        String s2 =flexTemplate.substring(flexTemplate.indexOf(tblPorto)+ tblPorto.length());
                        flexTemplate=s1+s2;
                    }else {
                        flexTemplate = flexTemplate.replaceAll("Text" + i, i==2 ? (flexText.get(1).equals("N/A") ? flexText.get(0) + " Index" : flexText.get(1)) : flexText.get(i-1));
                    }
                }
                flexContainer = objectMapper.readValue(flexTemplate, FlexContainer.class);
                replyMessage = new ReplyMessage(replyToken, new FlexMessage("Performa " + flexText.get(0) + " hari ini", flexContainer));
            }else if(flextype==3){
                flexTemplate = IOUtils.toString(classLoader.getResourceAsStream("flex_profile.json"));
                for(int i=1; i<=flexText.size();i++){
                    flexTemplate=flexTemplate.replaceAll("Text"+i,flexText.get(i-1));
                }
                flexContainer = objectMapper.readValue(flexTemplate, FlexContainer.class);
                replyMessage = new ReplyMessage(replyToken, new FlexMessage("Profileku", flexContainer));
            }
            reply(replyMessage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @RequestMapping(value = "/content/{id}", method = RequestMethod.GET)
    public ResponseEntity content(
            @PathVariable("id") String messageId
    ){
        MessageContentResponse messageContent = getContent(messageId);

        if(messageContent != null) {
            HttpHeaders headers = new HttpHeaders();
            String[] mimeType = messageContent.getMimeType().split("/");
            headers.setContentType(new MediaType(mimeType[0], mimeType[1]));

            InputStream inputStream = messageContent.getStream();
            InputStreamResource inputStreamResource = new InputStreamResource(inputStream);

            return new ResponseEntity<>(inputStreamResource, headers, HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    private MessageContentResponse getContent(String messageId) {
        try {
            return lineMessagingClient.getMessageContent(messageId).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
//    @RequestMapping(value="/pushmessage/{id}/{message}", method=RequestMethod.GET)
//    public ResponseEntity<String> pushmessage(
//            @PathVariable("id") String userId,
//            @PathVariable("message") String textMsg
//    ){
//        TextMessage textMessage = new TextMessage(textMsg);
//        PushMessage pushMessage = new PushMessage(userId, textMessage);
//        push(pushMessage);
//
//        return new ResponseEntity<String>("Push message:"+textMsg+"\nsent to: "+userId, HttpStatus.OK);
//    }
//    private void push(PushMessage pushMessage){
//        try {
//            lineMessagingClient.pushMessage(pushMessage).get();
//        } catch (InterruptedException | ExecutionException e) {
//            throw new RuntimeException(e);
//        }
//    }
//    @RequestMapping(value="/multicast", method=RequestMethod.GET)
//    public ResponseEntity<String> multicast(
//            /*@PathVariable("id")*/ String[] userIdList,
//                                    String textMsg
//    ){
//        /*String[] userIdList = {
//                "U206d25c2ea6bd87c17655609xxxxxxxx",
//                "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
//                "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
//                "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
//                "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"};*/
//        Set<String> listUsers = new HashSet<String>(Arrays.asList(userIdList));
//        if(listUsers.size() > 0){
//            //String textMsg = "Ini pesan multicast";
//            sendMulticast(listUsers, textMsg);
//        }
//        return new ResponseEntity<String>(HttpStatus.OK);
//    }
//    private void sendMulticast(Set<String> sourceUsers, String txtMessage){
//        TextMessage message = new TextMessage(txtMessage);
//        Multicast multicast = new Multicast(sourceUsers, message);
//
//        try {
//            lineMessagingClient.multicast(multicast).get();
//        } catch (InterruptedException | ExecutionException e) {
//            throw new RuntimeException(e);
//        }
//    }
//    @RequestMapping(value = "/profile/{id}", method = RequestMethod.GET)
//    public ResponseEntity<String> profile(
//            @PathVariable("id") String userId
//    ){
//        UserProfileResponse profile = getProfile(userId);
//
//        if (profile != null) {
//            String profileName = profile.getDisplayName();
//            TextMessage textMessage = new TextMessage("Hello, " + profileName);
//            PushMessage pushMessage = new PushMessage(userId, textMessage);
//            push(pushMessage);
//
//            return new ResponseEntity<String>("Hello, "+profileName, HttpStatus.OK);
//        }
//        return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
//    }
    private UserProfileResponse getProfile(String userId){
        try {
            return lineMessagingClient.getProfile(userId).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
    private void reply(ReplyMessage replyMessage) {
        try {
            lineMessagingClient.replyMessage(replyMessage).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private void replySticker(String replyToken, String packageId, String stickerId){
        StickerMessage stickerMessage = new StickerMessage(packageId, stickerId);
        ReplyMessage replyMessage = new ReplyMessage(replyToken, stickerMessage);
        reply(replyMessage);
    }

    private void replyText(String replyToken, String messageToUser){
        TextMessage textMessage = new TextMessage(messageToUser);
        ReplyMessage replyMessage = new ReplyMessage(replyToken, textMessage);
        reply(replyMessage);
    }
    private void replyMultiMsg(String replyToken, List<String> msg) {
        List<Message> msgArray = new ArrayList<>();
        for (String s : msg) {
            msgArray.add(new TextMessage(s));
        }
        ReplyMessage replyMessage = new ReplyMessage(replyToken, msgArray);
        reply(replyMessage);
    }
    private void leaveGroup(String groupId) {
        try {
            lineMessagingClient.leaveGroup(groupId).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private void leaveRoom(String groupId) {
        try {
            lineMessagingClient.leaveGroup(groupId).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }


}