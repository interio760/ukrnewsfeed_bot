package ukrnewsfeed_bot.NewsAPI;

public class NewsFormatter {

    public String format(News n){
        StringBuilder text = new StringBuilder();
        text.append(n.getText());
        if(!n.getImage().equals("")) {
            text.append("<a href=");
            text.append(n.getImage());
            text.append(">&#8205;</a>");
        }
        if(!n.getDescription().equals("")) {
            text.append("\n\n");
            text.append(n.getDescription());
        }
        text.append("\n\n<a href=\"");
        text.append(n.getLink());
        text.append("\">Читать статью</a>");
        return text.toString().replaceAll("&raquo;", "»").replaceAll("&laquo;", "«");
    }

}
