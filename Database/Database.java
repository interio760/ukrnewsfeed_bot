package ukrnewsfeed_bot.Database;

import ukrnewsfeed_bot.Telegram.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import static java.sql.DriverManager.getConnection;

public class Database {
    protected final String user, password, host, port, dbname;
    protected Connection conn;

    public Database(String user, String password, String host, String port, String dbname) throws ClassNotFoundException, SQLException {
        this.user = user;
        this.password = password;
        this.host = host;
        this.port = port;
        this.dbname = dbname;

        Class.forName("com.mysql.cj.jdbc.Driver");
        this.conn = getConnection("jdbc:mysql://" + host + ":" + port + "/" + dbname +
                "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC"
                , user, password);
    }

    public void userAddKeyword(long chat_id, String keyword) throws SQLException, KeywordAlreadyExistsException{
        if(!userExists(chat_id)) return;

        if(keywordExists(chat_id, keyword)) throw new KeywordAlreadyExistsException();

        keywordOperation("INSERT INTO user_keyword (user_id, keyword) VALUES ((SELECT id FROM user WHERE chat_id = ?), ?)", chat_id, keyword)
                .execute();
    }

    public void userRemoveKeyword(long chat_id, String keyword) throws SQLException, KeywordDoesntExistsException{
        if(!userExists(chat_id)) return;

        if(!keywordExists(chat_id, keyword)) throw new KeywordDoesntExistsException();

        keywordOperation("DELETE FROM user_keyword WHERE user_id = (SELECT id FROM user WHERE chat_id = ?) AND keyword = ?", chat_id, keyword)
                .execute();
    }

    public boolean keywordExists(long chat_id, String keyword) throws SQLException{
        PreparedStatement st = conn.prepareStatement("SELECT id FROM user_keyword WHERE user_id = (SELECT id FROM `user` WHERE user.chat_id = ?) AND keyword = ?");
        st.setLong(1, chat_id);
        st.setString(2, keyword);
        ResultSet rs = st.executeQuery();
        return rs.next();
    }

    protected PreparedStatement keywordOperation(String sql, long chat_id, String keyword) throws SQLException{
        PreparedStatement st = conn.prepareStatement(sql);
        st.setLong(1, chat_id);
        st.setString(2, keyword);
        return st;
    }

    public boolean articleRead(long chat_id, String url) throws SQLException{
        PreparedStatement st = conn.prepareStatement("SELECT id FROM user_news WHERE user_id = (SELECT id FROM `user` WHERE user.chat_id = ?) AND url = ?");
        st.setLong(1, chat_id);
        st.setString(2, url);
        ResultSet rs = st.executeQuery();
        return rs.next();
    }

    public void articlePut(long chat_id, String url) throws SQLException{
        keywordOperation("INSERT INTO user_news (user_id, url) VALUES ((SELECT id FROM `user` WHERE user.chat_id = ?), ?)", chat_id, url)
                .execute();
    }

    public User getUser(long chat_id) throws SQLException{
        PreparedStatement st = conn.prepareStatement("SELECT user.id, user.chat_id, user_keyword.keyword FROM user INNER JOIN user_keyword ON `user`.id = user_keyword.user_id WHERE user.chat_id = ?");
        st.setLong(1, chat_id);
        ResultSet rs = st.executeQuery();
        if(rs.next()){
            User usr = new User(rs.getLong("chat_id"));
            ArrayList<String> kw = usr.getKeyword();
            kw.add(rs.getString("keyword"));
            while(rs.next()){
                kw.add(rs.getString("keyword"));
            }
            return usr;
        }
        throw new UserNotFoundException();
    }

    public ArrayList<User> getAllUsers() throws SQLException{
        ArrayList<User> users = new ArrayList<>();
        PreparedStatement st = conn.prepareStatement("SELECT user.id, user.chat_id, user_keyword.keyword FROM user INNER JOIN user_keyword ON `user`.id = user_keyword.user_id WHERE user.active = 1 ORDER BY user.id");
        ResultSet rs = st.executeQuery();

        while(rs.next()){
            User usr = new User(rs.getLong("chat_id"));
            if(users.contains(usr)){
                users.get(users.indexOf(usr)).getKeyword().add(rs.getString("keyword"));
            }
            else{
                usr.getKeyword().add(rs.getString("keyword"));
                users.add(usr);
            }
        }
        return users;
    }

    public void newUser(long chat_id) throws SQLException{
        if(userExists(chat_id)) return;
        PreparedStatement st = conn.prepareStatement("INSERT INTO `user` (chat_id) VALUES (?)");
        st.setLong(1, chat_id);
        st.execute();
    }

    public boolean userExists(long chat_id) throws SQLException{
        PreparedStatement st = conn.prepareStatement("SELECT id FROM `user` WHERE chat_id = ?");
        st.setLong(1, chat_id);
        ResultSet rs = st.executeQuery();
        return rs.next();
    }

    public long getOffset(long source) throws SQLException{
        PreparedStatement st = conn.prepareStatement("SELECT MAX(int_id) AS mx FROM article WHERE source = ?");
        st.setLong(1, source);
        ResultSet rs = st.executeQuery();
        rs.next();
        return rs.getLong("mx");
    }

    public void putArticle(long source, long int_id) throws SQLException{
        PreparedStatement st = conn.prepareStatement("INSERT INTO article (source, int_id) VALUES (?, ?)");
        st.setLong(1, source);
        st.setLong(2, int_id);
        st.execute();
    }

    public boolean isActive(long chat_id) throws SQLException{
        PreparedStatement st = conn.prepareStatement("SELECT active FROM `user` WHERE chat_id = ?");
        st.setLong(1, chat_id);
        ResultSet rs = st.executeQuery();
        rs.next();
        return rs.getInt("active") == 1;
    }

    public void setActive(long chat_id, boolean active) throws SQLException {
        if (userExists(chat_id)) {
            PreparedStatement st = conn.prepareStatement("UPDATE `user` SET active = ? WHERE chat_id = ?");
            st.setLong(1, active ? 1 : 0);
            st.setLong(2, chat_id);
            st.execute();
        }
    }
}
