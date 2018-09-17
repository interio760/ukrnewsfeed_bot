package ukrnewsfeed_bot.NewsAPI;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class NewsAPI {

    protected final String access_token, connectionURI;

    public NewsAPI(String access_token) {
        this.access_token = access_token;
        connectionURI = "https://newsapi.org/v2/everything?apiKey=" + access_token +"&pageSize=5";
    }

    public ArrayList<News> getLatest(ArrayList<String> keywords, boolean notor) throws IOException {
        if (keywords.isEmpty()) return new ArrayList<>();
        String first = keywords.get(0);
        StringBuilder arg = new StringBuilder(first);
        for (String s : keywords) {
            if(s.equals(first)) continue;
            if(!notor) arg.append(" OR ");
            else arg.append(", ");
            arg.append(s);
        }

        System.out.println(arg.toString());

        URL address = new URL(connectionURI + "&q=" + URLEncoder.encode(arg.toString(), "UTF-8"));

        HttpsURLConnection conn = (HttpsURLConnection) address.openConnection();
        conn.setConnectTimeout(5000);
        conn.setRequestMethod("GET");


        BufferedReader response = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String r = response.lines().collect(Collectors.joining());
        JsonObject jsonObject = new JsonParser().parse(r).getAsJsonObject();
        response.close();
        conn.disconnect();

        ArrayList<News> news = new ArrayList<>();

        for (JsonElement el : jsonObject.getAsJsonArray("articles")) {
            news.add(newArticle(el));
        }

        return news;
    }

    protected News newArticle(JsonElement art) {
        JsonObject obj = art.getAsJsonObject();
        System.out.println(obj.toString());
        String url = obj.get("url").getAsString();
        String title = obj.get("title").toString().replaceAll("вЂ™", "'");
        String image = obj.has("urlToImage") ? obj.get("urlToImage").toString() : "";
        String desc = obj.has("description") ? obj.get("description").toString() : "";
        desc = desc.replaceAll("вЂ™", "'");
        desc = Jsoup.clean(desc, Whitelist.simpleText());
        title = Jsoup.clean(title, Whitelist.simpleText());
        return new News(title, url, desc.substring(0, desc.length()-16) + "...\"", image);
    }
}
