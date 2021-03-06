package server;

import server.service.AuthService;
import server.service.DBAuthService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.*;

public class Server {
    private Vector<ClientHandler> clients;
    private AuthService authService;
    private ExecutorService executorService;

    private static final Logger logger = Logger.getLogger(Server.class.getName());
    Handler handler = new ConsoleHandler();

    public ExecutorService getExecutorService() {
        return Executors.newCachedThreadPool();
    }

    public AuthService getAuthService() {
        return authService;
    }

    public Server() {
        handler.setFormatter(new SimpleFormatter());
        handler.setLevel(Level.ALL);
        logger.addHandler(handler);
        logger.setLevel(Level.ALL);

        clients = new Vector<>();
        try {
            authService = new DBAuthService();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        ServerSocket server = null;
        Socket socket = null;

        try {
            server = new ServerSocket(8189);
            //System.out.println("Сервер запустился");
            logger.log(Level.INFO, "Сервер запустился");
            while (true) {
                socket = server.accept();
                //System.out.println("Клиент подключился");
                logger.log(Level.INFO, "Клиент подключился");
                new ClientHandler(socket, this);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                server.close();
                logger.log(Level.INFO, "Соединение разорвано");
                authService.disconnect();//вот здесь разрываем соединение с бд, когда закрывается сервер
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void broadcastMsg(String nick, String msg) {
        for (ClientHandler c : clients) {
            c.sendMsg(nick + " : " + msg);
        }
    }

    public void privateMsg(ClientHandler sender, String receiver, String msg) {
        String message = String.format("[ %s ] private [ %s ] : %s", sender.getNick(), receiver, msg);

        for (ClientHandler c : clients) {
            if (c.getNick().equals(receiver)) {
                c.sendMsg(message);
                if (!sender.getNick().equals(receiver)) {
                    sender.sendMsg(message);
                }
                return;
            }
        }

        sender.sendMsg("not found user :" + receiver);
    }


    public void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
        broadcastClientList();
    }

    public void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        broadcastClientList();
    }


    public boolean isLoginAuthorized(String login) {
        for (ClientHandler c : clients) {
            if (c.getLogin().equals(login)) {
                return true;
            }
        }
        return false;
    }


    public void broadcastClientList() {
        StringBuilder sb = new StringBuilder("/clientlist ");

        for (ClientHandler c : clients) {
            sb.append(c.getNick()).append(" ");
        }

        String msg = sb.toString();
        for (ClientHandler c : clients) {
            c.sendMsg(msg);
        }
    }
}
