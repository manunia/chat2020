package server.service;

import server.entity.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBAuthService implements AuthService {

    private static Connection connection;
    private static Statement statement;
    private static PreparedStatement preparedStatement;
    private List<User> users;

    public DBAuthService() throws SQLException {
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
                users.add(new User(rs.getString("login"), rs.getString("pass"), rs.getString("nick")));
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void iaddUserInDb(String login, String pass, String nic) throws SQLException {
        statement.executeUpdate("INSERT INTO userdata (login, pass, nick) VALUES ('" + login + "','" + pass + "','" + nic + "')");
    }

    public void updateUserInDb(String login, String pass, String nic) throws SQLException {
        statement.executeUpdate("UPDATE userdata SET nick='" + nic + "'WHERE login='" + login + "' AND pass='" + pass +"'");
    }

    @Override
    public String getNicknameByLoginAndPassword(String login, String password) {
        for (User o : users) {
            if (o.getLogin().equals(login) && o.getPassword().equals(password)) {
                return o.getNickname();
            }
        }
        return null;
    }

    @Override
    public boolean registration(String login, String password, String nickname) {
        for (User o : users) {
            if (o.getLogin().equals(login)) {
                return false;
            }
        }

        if (password.trim().equals("")) {
            return false;
        }

        users.add(new User(login, password, nickname));
        try {
            iaddUserInDb(login,password,nickname);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean changeNickname(String login, String password, String nickname) {
        for (User o : users) {
            if (o.getLogin().equals(login) && o.getPassword().equals(password)) {
                users.remove(o);
                try {
                    updateUserInDb(login,password,nickname);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                users.add(new User(login, password, nickname));
                return true;
            }
        }

        return false;
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
