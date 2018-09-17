package ukrnewsfeed_bot.Parsers;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class UNNParser extends NewsParser {

    protected static int sourceId = 1;

    public ArrayList<Article> getLatest(long offset) throws IOException {
        ArrayList<Article> list = new ArrayList<>();

        Document unn = Jsoup.connect("https://unn.com.ua/uk/news").userAgent("Mozilla").get();
        Elements els = unn.select("#news_public_holder a");

        int limit = 0;

        for(Element el : els){
            int end = el.toString().substring(10).indexOf("\"");
            String link = el.toString().substring(10, end+10);
            long id = Integer.parseInt(link.substring(8, 16).replaceAll("\\D+", ""));
            if(id <= offset) continue;
            list.add(loadArticle(link, id));
            limit++;
            if(limit >= 10) return list;
        }

        return list;
    }

    public long getSourceId(){
        return sourceId;
    }

    protected Article loadArticle(String address, long id) throws IOException{
        Document unn = Jsoup.connect("https://unn.com.ua/" + address).userAgent("Mozilla").get();
        Elements els = unn.select(".b-news-text p");

        String text = els.toString();
        text = text.replaceAll("<p>", "##rep##").
                replaceAll("</p>", "").replaceAll("&nbsp;", " ").
                replaceAll("<br>", "#rep#").replaceAll("<br/>", "#rep#");
        text = Jsoup.clean(text, getWhitelist());
        text = text.replaceAll("##rep##", "\n\n");
        text = text.replaceAll("#rep#", "\n");
        els = unn.select(".b-news-full-img img");
        String photo_link = els.toString();

        try {
            photo_link = "https://unn.com.ua" + photo_link.substring(photo_link.indexOf("src=\"") + 5, photo_link.length() - 4);
        }catch (StringIndexOutOfBoundsException e){
            photo_link = null;
        }

        System.out.println(id);
        return new Article(id, text, photo_link);
    }

}
