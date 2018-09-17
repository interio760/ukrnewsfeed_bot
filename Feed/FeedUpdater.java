package ukrnewsfeed_bot.Feed;

import ukrnewsfeed_bot.Database.Database;
import ukrnewsfeed_bot.Telegram.TelegramAPI;

public abstract class FeedUpdater implements Runnable {

    protected final TelegramAPI telegram;
    protected final Database db;
    protected boolean stop = false;

    public FeedUpdater(TelegramAPI telegram, Database db){
        this.telegram = telegram;
        this.db = db;
    }

    public void stop(){
        this.stop = true;
    }

}
