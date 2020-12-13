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
import com.linecorp.bot.model.event.FollowEvent;
import com.linecorp.bot.model.event.JoinEvent;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.ReplyEvent;
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
            "jadwal uts","saham",
            "indeks","index",
            "profile","edit profile",
            "+nama","+bio",
            "+daftar","-leave"};

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
                        List<Message> msgArray = new ArrayList<>();
                        msgArray.add(new TextMessage(
                                "Hai!\n"+
                                "Terimakasih telah menambahkan Avo kesini"));
                        msgArray.add(new StickerMessage("1", "2"));
                        msgArray.add(new TextMessage(
                                "Ketik \"profile\" kalau kamu mau liat profile kamu ya.\n"+
                                        "Tapi pastikan kamu udah jadiin aku teman kamu dulu okey?:)"));
                        ReplyMessage replyMessage = new ReplyMessage(((ReplyEvent) event).getReplyToken(), msgArray);
                        reply(replyMessage);
                    }
                }
            });

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
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
        String baseURL     = "https://avoidxline.herokuapp.com";
        String contentURL  = baseURL+"/content/"+ event.getMessage().getId();
        String contentType = event.getMessage().getClass().getSimpleName();
        String textMsg     = contentType.substring(0, contentType.length() -14)
                + " yang kamu kirim bisa diakses dari link:\n "
                + contentURL;

        replyText(event.getReplyToken(), textMsg);
    }
    private void handleTextMessage(MessageEvent event) {
        TextMessageContent textMessageContent = (TextMessageContent) event.getMessage();
        String msg = textMessageContent.getText();
//        if(event.getSource() instanceof GroupSource || event.getSource() instanceof RoomSource){
//            msg = textMessageContent.getText().substring(4); //"avo "=4
//        }else {
//            msg = textMessageContent.getText();
//        }
        for (String keyword : keywords) {
            if (msg.length() >= keyword.length()) {
                if (msg.toLowerCase().substring(0, keyword.length()).equals(keyword)) {
                    String symbol = "";
                    String userid = event.getSource().getUserId();
                    StocksAPI Stocks;
                    Dbs=new DbSvc();
                    switch (keyword) {
                        case "jadwal uts":
                            replyFlexMessage(event.getReplyToken());
                            return;
                        case "saham":
                            symbol = msg.toUpperCase().substring(6); //misal teks "saham BBCA", berarti memisahkan teks "saham " dengan "BBCA"
                            Stocks = new StocksAPI(symbol + ".JK");
                            Stocks.join();
                            String[] dataaset = Stocks.getSingleQuote();

                            if (dataaset != null) {
                                ArrayList<String> dataset = new ArrayList<>();
                                dataset.add(symbol);
                                dataset.addAll(Arrays.asList(dataaset).subList(0, 4));
                                if (dataaset[3].contains("+")) {
                                    dataset.add("#2E7D32");
                                } else if (dataaset[3].contains("-")) {
                                    dataset.add("#C62828");
                                } else {
                                    dataset.add("#000000");
                                }
                                replyFlexMessage(event.getReplyToken(), 2, dataset);
                            } else {
                                replyText(event.getReplyToken(), symbol + " tidak ditemukan.");
                            }
                            return;
                        case "indeks":
                            symbol = msg.toLowerCase().substring(7);
                        case "index":
                            try {
                                if (symbol.equals("")) {
                                    symbol = msg.toLowerCase().substring(6);
                                }
                                ClassLoader classLoader = getClass().getClassLoader();
                                String idx_keys = IOUtils.toString(classLoader.getResourceAsStream("index_keywords.json"));
                                String idkey = "NA";
                                JSONObject idxkeys = new JSONObject(idx_keys);
                                try {
                                    idkey = idxkeys.getString(symbol);
                                } catch (Exception ignored) {
                                }
                                if (!idkey.equals("NA")) {
                                    Stocks = new StocksAPI(idkey);
                                    Stocks.join();
                                    String[] dset = Stocks.getSingleQuote();
                                    ArrayList<String> dataset = new ArrayList<>();
                                    dataset.add(symbol.toUpperCase());
                                    dataset.addAll(Arrays.asList(dset).subList(0, 4));
                                    if (dset[3].contains("+")) {
                                        dataset.add("#2E7D32");
                                    } else if (dset[3].contains("-")) {
                                        dataset.add("#C62828");
                                    } else {
                                        dataset.add("#000000");
                                    }
                                    replyFlexMessage(event.getReplyToken(), 2, dataset);
                                } else {
                                    replyText(event.getReplyToken(), symbol.toUpperCase() + " tidak ditemukan.");
                                }
                            } catch (Exception ignored) {
                            }
                            return;
                        case "profile":
                                Dbs.init(userid);
                                if(Dbs.isUserExist()){
                                    Dbs.initUser();
                                    ArrayList<String> user = new ArrayList<>();
                                    user.add(Dbs.getUserProfile().uname);
                                    user.add(Dbs.getUserProfile().ustatus);
                                    user.add(getProfile(userid).getPictureUrl());
                                    replyFlexMessage(event.getReplyToken(), 3, user);
                                }else{
                                    List<String> multimsg = new ArrayList<>();
                                    multimsg.add("Akun kamu belum terdaftar:( Silahkan daftar dulu ya dengan ketik");
                                    multimsg.add("+daftar Nama\n+bio Biokamu");
                                    multimsg.add("Misalnya: \n+daftar Avo's IDX\n+bio Yuk Nabung Saham!\nNama & bio gak boleh kosong ya.");
                                    replyMultiMsg(event.getReplyToken(), multimsg);
                                }
                                return;
                        case "+daftar":
                            Dbs.init(userid);
                            if(Dbs.isUserExist()){
                                List<Message> msgArray = new ArrayList<>();
                                msgArray.add(new TextMessage(String.format("Hai %s! Kamu udah terdaftar kok, tenang aja!",getProfile(userid).getDisplayName())));
                                msgArray.add(new StickerMessage("1", "13"));
                                ReplyMessage replyMessage = new ReplyMessage(((ReplyEvent) event).getReplyToken(), msgArray);
                                reply(replyMessage);
                            }else{
                                if(msg.contains("\n+bio")){
                                    String nama=msg.substring(8,msg.indexOf("\n+bio")); //8 didapat dari length("+daftar ")
                                    if(nama.equals("")){
                                        replyText(event.getReplyToken(),"Namanya gak boleh kosong ya:)");
                                        return;
                                    }
                                    String bio = msg.substring(msg.indexOf("\n+bio") + 6); //6 didapat dari length("\n+bio ")
                                    if(bio.equals("")){
                                        replyText(event.getReplyToken(),"Bionya gak boleh kosong ya:)");
                                        return;
                                    }
                                    Dbs.createUser(nama,bio);
                                    replyText(event.getReplyToken(),"Yeay! Pendaftaran kamu sukses!");
                                }else{
                                    List<String> multimsg = new ArrayList<>();
                                    multimsg.add("Wah formatnya salah nih. Untuk daftar ketiknya");
                                    multimsg.add("+daftar Nama\n+bio Biokamu");
                                    multimsg.add("Misalnya: \n+daftar Avo's IDX\n+bio Yuk Nabung Saham!\nNama & bio gak boleh kosong ya.");
                                    replyMultiMsg(event.getReplyToken(), multimsg);
                                }
                            }
                            return;
                        case "edit profile":
                            if (event.getSource() instanceof GroupSource || event.getSource() instanceof RoomSource) {
                                replyText(event.getReplyToken(), "Duh maaf Avo gak bisa bantu edit profile kamu disini:(");
                            } else {
                                Dbs.init(userid);
                                if(Dbs.isUserExist()) {
                                    List<String> multimsg = new ArrayList<>();
                                    multimsg.add(
                                            "Tulis nama & bio yang mau kamu ubah ya.\n" +
                                                    "Formatnya:");
                                    multimsg.add(
                                            "+nama Namabaru\n" +
                                                    "+bio Biobaru");
                                    multimsg.add("Untuk bagian yang gak ingin kamu ganti, cukup isi dengan \"##\" aja ya. Misalnya: nama ##");
                                    replyMultiMsg(event.getReplyToken(), multimsg);
                                }else {
                                    List<String> multimsg = new ArrayList<>();
                                    multimsg.add("Akun kamu belum terdaftar:( Silahkan daftar dulu ya dengan ketik");
                                    multimsg.add("+daftar Nama\n+bio Biokamu");
                                    multimsg.add("Misalnya: \n+daftar Avo's IDX\n+bio Yuk Nabung Saham!\nNama & bio gak boleh kosong ya.");
                                    replyMultiMsg(event.getReplyToken(), multimsg);
                                }
                            }
                            return;
                        case "+nama":
                            if (event.getSource() instanceof GroupSource || event.getSource() instanceof RoomSource) {
                                replyText(event.getReplyToken(), "Duh maaf Avo gak bisa bantu edit profile kamu disini:(");
                            } else {
                                Dbs.init(userid);
                                if(!Dbs.isUserExist()) {
                                    List<String> multimsg = new ArrayList<>();
                                    multimsg.add("Akun kamu belum terdaftar:( Silahkan daftar dulu ya dengan ketik");
                                    multimsg.add("+daftar Nama\n+bio Biokamu");
                                    multimsg.add("Misalnya: \n+daftar Avo's IDX\n+bio Yuk Nabung Saham!\nNama & bio gak boleh kosong ya.");
                                    replyMultiMsg(event.getReplyToken(), multimsg);
                                    return;
                                }
                                if (msg.contains("\n+bio")) {
                                    String nama = msg.substring(6, msg.indexOf("\n+bio")); //6 didapat dari length("+nama ")
                                    if(nama.equals("")){
                                        replyText(event.getReplyToken(),"Namanya gak boleh kosong ya:)");
                                        return;
                                    }
                                    String bio = msg.substring(msg.indexOf("\n+bio") + 6); //6 didapat dari length("\n+bio ")
                                    if(bio.equals("")){
                                        replyText(event.getReplyToken(),"Bionya gak boleh kosong ya:)");
                                        return;
                                    }
                                    if (!nama.equals("##") && !bio.equals("##")) {
                                        Dbs.updateUser(nama,bio);
                                        replyText(event.getReplyToken(),"Sukses ganti nama menjadi " + nama + "\ndan bio menjadi " + bio);
                                    } else if (!nama.equals("##")) {
                                        Dbs.updateUsername(nama);
                                        replyText(event.getReplyToken(),"Sukses ganti nama menjadi " + nama);
                                    } else if (!bio.equals("##")) {
                                        Dbs.updateUserbio(bio);
                                        replyText(event.getReplyToken(),"Sukses ganti bio menjadi " + bio);
                                    } else if (nama.equals("##") && bio.equals("##")) {
                                        replyText(event.getReplyToken(),"Sukses gak ganti apa-apa:)");
                                    } else {
                                        replyText(event.getReplyToken(),"Yah sepertinya ada yang salah dengan Avo:(");
                                    }
                                } else {
                                    List<String> multimsg = new ArrayList<>();
                                    multimsg.add(
                                            "Aduh formatnya salah nih:(\n" +
                                                    "Formatnya:");
                                    multimsg.add(
                                            "+nama Namabaru\n" +
                                                    "+bio Biobaru");
                                    multimsg.add("Untuk bagian yang gak ingin kamu ganti, cukup isi dengan \"##\" aja ya. Misalnya: nama ##");
                                    replyMultiMsg(event.getReplyToken(), multimsg);
                                }
                            }
                            return;
                        case "+bio":
                            if (event.getSource() instanceof GroupSource || event.getSource() instanceof RoomSource) {
                                replyText(event.getReplyToken(), "Duh maaf Avo gak bisa bantu edit profile kamu disini:(");
                            } else {
                                List<String> multimsg = new ArrayList<>();
                                multimsg.add(
                                        "Aduh formatnya salah nih:(\n" +
                                                "Formatnya:");
                                multimsg.add(
                                        "+nama Namabaru\n" +
                                                "+bio Biobaru");
                                multimsg.add("Untuk bagian yang gak ingin kamu ganti, cukup isi dengan \"##\" aja ya. Misalnya: nama ##");
                                replyMultiMsg(event.getReplyToken(), multimsg);
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
            replyText(event.getReplyToken(), "Keyword salah:(");
        }
    }
    private void replyFlexMessage(String replyToken){
        replyFlexMessage(replyToken, 1, null);
    }
    private void replyFlexMessage(String replyToken, int flextype, ArrayList<String> flexText) {
        try {
            //1: Jadwal UTS, 2: Stock, 3: Profile
            ClassLoader classLoader = getClass().getClassLoader();
            String flexTemplate; FlexContainer flexContainer; ReplyMessage replyMessage=null;
            ObjectMapper objectMapper = ModelObjectMapper.createNewObjectMapper();
            if(flextype==1) {
                flexTemplate = IOUtils.toString(classLoader.getResourceAsStream("flex_jadwaluts.json"));
                flexContainer = objectMapper.readValue(flexTemplate, FlexContainer.class);
                replyMessage = new ReplyMessage(replyToken, new FlexMessage("Jadwal UTS", flexContainer));
            }else if(flextype==2){
                flexTemplate = IOUtils.toString(classLoader.getResourceAsStream("flex_stock.json"));
                for(int i=1; i<=6;i++){
                    flexTemplate=flexTemplate.replaceAll("Text"+i,flexText.get(i-1));
                }
                flexContainer = objectMapper.readValue(flexTemplate, FlexContainer.class);
                replyMessage = new ReplyMessage(replyToken, new FlexMessage("Performa " + flexText.get(0) + " hari ini", flexContainer));
            }else if(flextype==3){
                flexTemplate = IOUtils.toString(classLoader.getResourceAsStream("flex_profile.json"));
                for(int i=1; i<=3;i++){
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