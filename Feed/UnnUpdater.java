package ukrnewsfeed_bot.Feed;

import ukrnewsfeed_bot.Database.Database;
import ukrnewsfeed_bot.Parsers.Article;
import ukrnewsfeed_bot.Parsers.UNNParser;
import ukrnewsfeed_bot.Telegram.TelegramAPI;
import ukrnewsfeed_bot.Telegram.User;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;

public class UnnUpdater extends FeedUpdater {

    protected final UNNParser parser;

    public UnnUpdater(TelegramAPI telegram, Database db){
        super(telegram, db);
        parser = new UNNParser();
    }

    @Override
    public void run() {
        while(!stop){
            try {
                long sourceId = parser.getSourceId();
                long offset = db.getOffset(sourceId);
                ArrayList<Article> list;
                try {
                    list = parser.getLatest(offset);
                }catch (SocketTimeoutException e){
                    System.err.println("Socket timeout");
                    continue;
                }catch (UnknownHostException e){
                    System.err.println("DNS Lookup error");
                    continue;
                }
                ArrayList<User> users = db.getAllUsers();
                for(Article ar : list){
                    try {
                        String msg = ar.toString() + (ar.getPhoto() != null
                                ? ("<a href=\"" + ar.getPhoto() + "\">&#8205;</a>")
                                : "");
                        ArrayList<User> fit_users = new ArrayList<>();
                        for(User user : users){
                            if(user.fits(msg)) fit_users.add(user);
                        }
                        telegram.sendMultiMessage(fit_users, msg);
                        db.putArticle(sourceId, ar.getId());
                    }catch(SQLException e){
                        e.printStackTrace();
                    }
                }
                Thread.sleep(2000);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
