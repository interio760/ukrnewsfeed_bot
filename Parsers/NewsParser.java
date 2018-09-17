package ukrnewsfeed_bot.Parsers;

import org.jsoup.safety.Whitelist;

import java.io.IOException;
import java.util.ArrayList;

public abstract class NewsParser {

    public abstract ArrayList<Article> getLatest(long offset) throws IOException;

    public Whitelist getWhitelist(){
        Whitelist whitelist = Whitelist.none();
        whitelist.addTags("a");
        whitelist.addAttributes("a","href");
        return whitelist;
    }

}
