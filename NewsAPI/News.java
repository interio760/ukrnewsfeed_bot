package ukrnewsfeed_bot.NewsAPI;

public class News {

    protected final String text;
    protected final String link;
    protected final String image;
    protected final String description;

    public News(String text, String link, String description, String image){
        this.text = text;
        this.link = link;
        this.description = description;
        this.image = image;
    }

    public String getText() {
        return text;
    }

    public String getDescription() {
        return description;
    }

    public String getLink() {
        return link;
    }

    public String getImage() {
        return image;
    }
}
