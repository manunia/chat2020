package server;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientHandler {
    Socket socket = null;
    DataInputStream in;
    DataOutputStream out;
    Server server;
    private String nick;
    private String login;

    public ClientHandler(Socket socket, Server server) {
        try {
            this.socket = socket;
            this.server = server;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            //создаем пулл потоков
            ExecutorService executorService = server.getExecutorService();
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        socket.setSoTimeout(120000);
                        //цикл аутентификации
                        while (true) {
                            String str = in.readUTF();
                            if (str.startsWith("/reg ")) {
                                String[] token = str.split(" ");
                                boolean b = server
                                        .getAuthService()
                                        .registration(token[1], token[2], token[3]);
                                if (b) {
                                    sendMsg("Регистрация прошла успешно");
                                } else {
                                    sendMsg("Пользователь не может быть зарегистрирован");
                                }
                            }

                            if (str.equals("/end")) {
                                throw new RuntimeException("сами ");
                            }
                            if (str.startsWith("/auth ")) {
                                String[] token = str.split(" ");
                                if (token.length < 3) {
                                    continue;
                                }
                                String newNick = server
                                        .getAuthService()
                                        .getNicknameByLoginAndPassword(token[1], token[2]);
                                if (newNick != null) {
                                    login = token[1];
                                    if (!server.isLoginAuthorized(login)) {
                                        sendMsg("/authok " + newNick);
                                        nick = newNick;
                                        server.subscribe(ClientHandler.this);
                                        System.out.println("Клиент " + nick + " подключился");
                                        socket.setSoTimeout(0);
                                        break;
                                    } else {
                                        sendMsg("С этим логином уже авторизовались");
                                    }
                                } else {
                                    sendMsg("Неверный логин / пароль");
                                }
                            }
                        }

                        //цикл работы
                        while (true) {
                            String str = in.readUTF();

                            if (str.startsWith("/")) {
                                if (str.equals("/end")) {
                                    out.writeUTF("/end");
                                    break;
                                }
                                if (str.startsWith("/w ")) {
                                    String[] token = str.split(" ", 3);
                                    if (token.length == 3) {
                                        server.privateMsg(ClientHandler.this, token[1], token[2]);
                                    }
                                }
                                if (str.startsWith("/changenick ")) {
                                    String[] token = str.split(" ");
                                    boolean b = server
                                            .getAuthService()
                                            .changeNickname(token[1], token[2], token[3]);
                                    if (!server.isLoginAuthorized(login)) b = false;
                                    if (b) {
                                        sendMsg("Nickname changed");
                                    } else {
                                        sendMsg("Can't change");
                                    }
                                }
                            } else {
                                server.broadcastMsg(nick, str);
                            }


                        }
                    }catch (SocketTimeoutException e){
                        System.out.println("Клиент отключился по таймауту");
                    } catch (RuntimeException e) {
                        System.out.println("сами вызвали исключение.");
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        server.unsubscribe(ClientHandler.this);
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        System.out.println("Клиент отключился");
                    }
                }
            });
            executorService.shutdown();


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNick() {
        return nick;
    }

    public String getLogin() {
        return login;
    }

}
