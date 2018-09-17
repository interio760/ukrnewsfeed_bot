package ukrnewsfeed_bot.Feed;

import ukrnewsfeed_bot.Database.Database;
import ukrnewsfeed_bot.NewsAPI.News;
import ukrnewsfeed_bot.NewsAPI.NewsAPI;
import ukrnewsfeed_bot.NewsAPI.NewsFormatter;
import ukrnewsfeed_bot.Telegram.TelegramAPI;
import ukrnewsfeed_bot.Telegram.User;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

public class GoogleNewsUpdater extends FeedUpdater {

    protected final NewsAPI gnews;

    public GoogleNewsUpdater(TelegramAPI telegram, Database db, NewsAPI api){
        super(telegram, db);
        gnews = api;
    }

    @Override
    public void run() {
        while(!stop){
            try {
                ArrayList<User> users = db.getAllUsers();
                NewsFormatter nf = new NewsFormatter();
                for(User u : users){
                    ArrayList<String> keywords = u.getKeyword();
                    ArrayList<News> news = gnews.getLatest(keywords, false);
                    for(News inf : news){
                        try {
                            if (!db.articleRead(u.getChatId(), inf.getLink())) {
                                telegram.sendMessage(u.getChatId(), nf.format(inf));
                                db.articlePut(u.getChatId(), inf.getLink());
                            }
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                }
            } catch (SQLException | IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    Thread.sleep(800000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
