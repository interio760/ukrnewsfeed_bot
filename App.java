package ukrnewsfeed_bot;

import ukrnewsfeed_bot.Controllers.Controller;
import ukrnewsfeed_bot.Database.Database;
import ukrnewsfeed_bot.Feed.GoogleNewsUpdater;
import ukrnewsfeed_bot.Feed.UnnUpdater;
import ukrnewsfeed_bot.NewsAPI.NewsAPI;
import ukrnewsfeed_bot.Telegram.IncorrectTokenException;
import ukrnewsfeed_bot.Telegram.TelegramAPI;

import java.sql.SQLException;

public class App 
{
    public static void main( String[] args )
    {
        TelegramAPI telegram;
        Database db;
        NewsAPI napi;

        try {

            telegram = new TelegramAPI(0, "");
            db = new Database("", "", "", "", "");
            napi = new NewsAPI("");

        }catch (IncorrectTokenException e){
            System.err.println("Incorrect Token or Bot ID");
            return;
        }catch (ClassNotFoundException e){
            System.err.println("Missing jdbc mysql driver");
            return;
        }catch (SQLException e){
            e.printStackTrace();
            return;
        }

        try {
            UnnUpdater upd = new UnnUpdater(telegram, db);
            Thread up = new Thread(upd);
            up.start();

            Controller ct = new Controller(telegram, db, napi);
            Thread t = new Thread(ct);
            t.start();

            GoogleNewsUpdater gnews = new GoogleNewsUpdater(telegram, db, napi);
            Thread gt = new Thread(gnews);
            gt.start();

            while(true){
                if(!up.isAlive()){
                    upd = new UnnUpdater(telegram, db);
                    up = new Thread(upd);
                    up.start();
                }

                if(!t.isAlive()){
                    ct = new Controller(telegram, db, napi);
                    t = new Thread(ct);
                    t.start();
                }

                if(!gt.isAlive()){
                    gnews = new GoogleNewsUpdater(telegram, db, napi);
                    gt = new Thread(gnews);
                    gt.start();
                }

                Thread.sleep(1000);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
