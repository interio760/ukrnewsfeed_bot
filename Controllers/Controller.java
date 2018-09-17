package ukrnewsfeed_bot.Controllers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import ukrnewsfeed_bot.Database.Database;
import ukrnewsfeed_bot.Database.KeywordAlreadyExistsException;
import ukrnewsfeed_bot.Database.KeywordDoesntExistsException;
import ukrnewsfeed_bot.Database.UserNotFoundException;
import ukrnewsfeed_bot.NewsAPI.News;
import ukrnewsfeed_bot.NewsAPI.NewsAPI;
import ukrnewsfeed_bot.NewsAPI.NewsFormatter;
import ukrnewsfeed_bot.Telegram.TelegramAPI;
import ukrnewsfeed_bot.Telegram.User;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

public class Controller implements Runnable{

    protected boolean stop = false;
    protected final TelegramAPI telegram;
    protected final Database db;
    protected final NewsAPI napi;
    protected int offset;

    public Controller(TelegramAPI telegram, Database db, NewsAPI napi){
        this.telegram = telegram;
        this.db = db;
        this.napi = napi;
        this.offset = 0;
    }

    @Override
    public void run() {
        int gg = 0;
        while(!stop){
            System.out.println(gg++);
            try{
                JsonObject data = telegram.getUpdates(offset);
                JsonArray arr = data.getAsJsonArray("result");
                int max = 0, update_id;
                for(JsonElement el : arr){

                    update_id = el.getAsJsonObject().get("update_id").getAsInt();
                    if( update_id > max ) max = update_id;
                    if(!el.getAsJsonObject().has("message")) continue;
                    JsonObject msg = el.getAsJsonObject().get("message").getAsJsonObject();
                    if(!msg.getAsJsonObject().has("text")) continue;
                    long chat_id = msg.get("from").getAsJsonObject().get("id").getAsLong();
                    String text = msg.get("text").toString();

                    route(text, chat_id);

                }
                offset = max+1;

            }catch (Exception e){
                e.printStackTrace();
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop(){
        this.stop = true;
    }

    protected void route(String cmd, long chat_id){
        cmd = cmd.substring(1, cmd.length()-1);
        System.out.println(cmd);
        if(!cmd.startsWith("/")) return;
        String[] bcmd = cmd.split(" ");
        System.out.println(bcmd[0]);

        if(bcmd[0].equalsIgnoreCase("/start")){
            handleStartBot(chat_id);
        }

        if(bcmd[0].equalsIgnoreCase("/stop")){
            handleStopBot(chat_id);
        }

        try {
            if(!db.userExists(chat_id)) db.newUser(chat_id);
        }catch (SQLException e){
            e.printStackTrace();
        }

        if(bcmd[0].equalsIgnoreCase("/add")){
            handleAddBot(chat_id, bcmd);
        }

        if(bcmd[0].equalsIgnoreCase("/remove")){
            handleRemoveBot(chat_id, bcmd);
        }

        if(bcmd[0].equalsIgnoreCase("/list")){
            handleListBot(chat_id);
        }

        if(bcmd[0].equalsIgnoreCase("/pick")){
            handlePickBot(chat_id, bcmd);
        }

    }

    protected void handleStartBot(long chat_id){
        try {
            if (db.userExists(chat_id)) {
                if(!db.isActive(chat_id)) {
                    db.setActive(chat_id, true);
                    telegram.sendMessage(chat_id, "Вы вновь подписались на новости.");
                } else{
                    telegram.sendMessage(chat_id, "Вы уже подписаны.");
                }
            } else {
                db.newUser(chat_id);
                telegram.sendMessage(chat_id, "Добро пожаловать! Вы подписались на новости.");
            }
        }catch (SQLException | IOException e){
            e.printStackTrace();
        }
    }

    protected void handleStopBot(long chat_id){
        try {
            if(db.isActive(chat_id)) {
                db.setActive(chat_id, false);
                telegram.sendMessage(chat_id, "Вы отписались от новостей.");
            } else{
                telegram.sendMessage(chat_id, "Вы не подписаны.");
            }
        }catch (SQLException | IOException e){
            e.printStackTrace();
        }
    }

    protected void handleAddBot(long chat_id, String[] keyword){
        try {
            for(String kw : keyword) {
                if (kw.equalsIgnoreCase("/add")) continue;
                try {
                    db.userAddKeyword(chat_id, kw);
                    telegram.sendMessage(chat_id, "Ключевое слово " + kw + " добавлено.");
                } catch (KeywordAlreadyExistsException e) {
                    telegram.sendMessage(chat_id, "Ключевое слово " + kw + " уже существует.");
                }
            }
        }catch (IOException | SQLException e){
            e.printStackTrace();
        }
    }

    protected void handleRemoveBot(long chat_id, String[] keyword){
        try {
            for(String kw : keyword) {
                if(kw.equalsIgnoreCase("/remove")) continue;
                try {
                    db.userRemoveKeyword(chat_id, kw);
                    telegram.sendMessage(chat_id, "Ключевое слово " + kw + " удалено.");
                } catch (KeywordDoesntExistsException e) {
                    telegram.sendMessage(chat_id, "Ключевое слово " + kw + " не существует.");
                }
            }
        }catch (IOException | SQLException e){
            e.printStackTrace();
        }
    }

    protected void handleListBot(long chat_id){
        try {
            User usr = db.getUser(chat_id);
            StringBuilder msg = new StringBuilder();
            msg.append("Список ключевых слов для новостей:\n\n");

            for(String kw : usr.getKeyword()){
                msg.append(kw);
                msg.append("\n");
            }

            telegram.sendMessage(chat_id, msg.toString());
        }catch (IOException | SQLException | UserNotFoundException e){
            e.printStackTrace();
        }
    }

    protected void handlePickBot(long chat_id, String[] keyword) {
        try {
            String[] args = Arrays.copyOfRange(keyword, 1, keyword.length);
            ArrayList<News> nw = napi.getLatest(new ArrayList<>(Arrays.asList(args)), true);
            NewsFormatter nf = new NewsFormatter();
            for(News n : nw){
                telegram.sendMessage(chat_id, nf.format(n));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
