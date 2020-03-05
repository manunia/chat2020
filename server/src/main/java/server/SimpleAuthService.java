package server;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SimpleAuthService implements AuthService {

    private class UserData {
        String login;
        String password;
        String nickname;

        public UserData(String login, String password, String nickname) {
            this.login = login;
            this.password = password;
            this.nickname = nickname;
        }
    }

    private static Connection connection;
    private static Statement statement;
    private static PreparedStatement preparedStatement;
    private List<UserData> users;

    public SimpleAuthService() throws SQLException {
        users = new ArrayList<>();

        try {
            connect();//соединяемся
            statement.executeUpdate("DELETE FROM userdata");//предварительно очистим таблицу
            //заполним таблицу в базе тестовыми данными
            for (int i = 1; i <= 10; i++) {
                statement.executeUpdate("INSERT INTO userdata (login, pass, nick) VALUES ('login" + i + "','pass" + i + "','nick" + i + "')");
            }
            connection.setAutoCommit(true);
            //достанем из базы данные и пометстим их в наш списох юзеров
            ResultSet rs = statement.executeQuery("SELECT * FROM userdata");
            while (rs.next()) {
                users.add(new UserData(rs.getString("login"), rs.getString("pass"), rs.getString("nick")));
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void iaddUserInDb(String login, String pass, String nic) throws SQLException {
        statement.executeUpdate("INSERT INTO userdata (login, pass, nick) VALUES ('" + login + "','" + pass + "','" + nic + "')");
    }

    @Override
    public String getNicknameByLoginAndPassword(String login, String password) {
        for (UserData o : users) {
            if (o.login.equals(login) && o.password.equals(password)) {
                return o.nickname;
            }
        }
        return null;
    }

    @Override
    public boolean registration(String login, String password, String nickname) {
        for (UserData o : users) {
            if (o.login.equals(login)) {
                return false;
            }
        }

        if (password.trim().equals("")) {
            return false;
        }

        users.add(new UserData(login, password, nickname));
        try {
            iaddUserInDb(login,password,nickname);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }


    public void connect() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:main.db");
        statement = connection.createStatement();
    }

    @Override
    public void disconnect() {
        try {
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
