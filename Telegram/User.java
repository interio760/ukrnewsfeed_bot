package ukrnewsfeed_bot.Telegram;

import java.util.ArrayList;
import java.util.Objects;

public class User {
    protected final long chat_id;
    protected final ArrayList<String> keyword;

    public User(long chat_id){
        this.chat_id = chat_id;
        keyword = new ArrayList<>();
    }

    public User(long chat_id, ArrayList<String> keyword){
        this.chat_id = chat_id;
        this.keyword = keyword;
    }

    public long getChatId() {
        return chat_id;
    }

    public ArrayList<String> getKeyword() {
        return keyword;
    }

    public boolean fits(String text){
        for(String kw : keyword){
            if(text.contains(kw)) return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return chat_id == user.chat_id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(chat_id);
    }
}
