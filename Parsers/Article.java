package ukrnewsfeed_bot.Parsers;

public class Article {

    private final long id;
    private final String text;
    private final String photo;

    protected Article(long id, String text, String photo){
        this.id = id;
        this.text = text;
        this.photo = photo;
    }

    @Override
    public String toString() {
        return text;
    }

    public String getPhoto(){
        return photo;
    }

    public long getId(){
        return id;
    }
}
