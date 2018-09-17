package ukrnewsfeed_bot.Telegram;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

public class TelegramAPI {

    protected final int bot_id;
    protected final String access_token, connectionURI;

    public TelegramAPI(int bot_id, String access_token) throws IncorrectTokenException{
        this.bot_id = bot_id;
        this.access_token = access_token;
        this.connectionURI = "https://api.telegram.org/bot" + bot_id + ":" + access_token + "/";
        testConnection();
    }

    public JsonObject sendMessage(long chat_id, String msg) throws IOException{
        JsonObject query = new JsonObject();
        System.out.println(msg);
        query.addProperty("chat_id", chat_id);
        query.addProperty("text", msg);
        query.addProperty("parse_mode", "HTML");
        return request("sendMessage", query);
    }

    public ArrayList<JsonObject> sendMultiMessage(ArrayList<User> list, String msg){
        ArrayList<JsonObject> responses = new ArrayList<>();
        for(User usr : list){
            long id = usr.getChatId();
            try {
                JsonObject resp = sendMessage(id, msg);
                responses.add(resp);
                Thread.sleep(100);
            }catch (IOException | InterruptedException e){
                e.printStackTrace();
            }
        }
        return responses;
    }

    public JsonObject sendPhoto(long chat_id, String photo) throws IOException{
        JsonObject query = new JsonObject();
        query.addProperty("chat_id", chat_id);
        query.addProperty("photo", photo);
        query.addProperty("allowed_updates", "message");
        return request("sendPhoto", query);
    }

    public ArrayList<JsonObject> sendMultiPhoto(ArrayList<Long> list, String photo){
        ArrayList<JsonObject> responses = new ArrayList<>();
        for(Long id : list){
            try {
                JsonObject resp = sendPhoto(id, photo);
                responses.add(resp);
                Thread.sleep(100);
            }catch (IOException | InterruptedException e){
                e.printStackTrace();
            }
        }
        return responses;
    }

    public JsonObject getUpdates(int offset) throws IOException{
        JsonObject query = new JsonObject();
        query.addProperty("offset", offset);
        return request("getUpdates", query);
    }

    protected void testConnection() throws IncorrectTokenException{
        try {
            request("getMe", new JsonObject());
        }catch (IOException e){
            throw new IncorrectTokenException();
        }
    }

    protected JsonObject request(String method, JsonObject data) throws IOException{
        URL address = new URL(connectionURI + method);
        HttpsURLConnection conn = (HttpsURLConnection) address.openConnection();
        conn.setDoOutput(true);
        conn.setConnectTimeout(5000);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type","application/json");
        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
        byte[] ptext = data.toString().getBytes(UTF_8);
        wr.write(ptext);
        wr.flush();
        wr.close();

        if(conn.getResponseCode() >= 400){
            BufferedReader response = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            String r = response.lines().collect(Collectors.joining());
            System.err.println(r);
            response.close();
            conn.disconnect();
        }
        else {
            BufferedReader response = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String r = response.lines().collect(Collectors.joining());
            JsonObject jsonObject = new JsonParser().parse(r).getAsJsonObject();
            if (!jsonObject.get("ok").getAsBoolean()) throw new RequestFailedException();
            response.close();
            conn.disconnect();
            return jsonObject;
        }

        throw new IOException();
    }

}
