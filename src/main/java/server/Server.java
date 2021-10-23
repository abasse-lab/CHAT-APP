package server;

import model.Conversation;
import model.Message;
import model.User;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

public class Server {

    private final Map<User, Socket> clients = new HashMap<>();
    private final List<Conversation> conversations = new ArrayList<>();
    private JSONArray usersJSON;
    private JSONArray conversationsJSON;
    private final Semaphore mutexConversationsJson = new Semaphore(1);
    private final Semaphore mutexUsersJson = new Semaphore(1);
    private final Semaphore mutexClients = new Semaphore(1);
    private final Semaphore mutexConversations = new Semaphore(1);
    public static final String PERSIST_DATA_FOLDER = "data/";

    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Usage: java server.Server <server.Server port>");
            System.exit(1);
        }
        Server server = new Server();
        server.initServer(args[0]);
    }

    public Server() {
    }

    public void initServer(String port) {
        retrieveData();
        ServerSocket listenSocket;
        try {
            listenSocket = new ServerSocket(Integer.parseInt(port)); //port
            System.out.println("server.Server ready...");
            while (true) {
                Socket clientSocket = listenSocket.accept();
                User user = manageConnection(clientSocket);
                System.out.println("Connexion from:" + clientSocket.getInetAddress());
                ClientThread ct = new ClientThread(user, clientSocket, clients, conversations, conversationsJSON, usersJSON, mutexConversationsJson, mutexUsersJson, mutexClients, mutexConversations);
                ct.start();
            }
        } catch (Exception e) {
            System.err.println("Error in server.Server:" + e);
        }
    }

    public void retrieveData() {
        retrieveUsers();
        retrieveConversations();
    }

    public void retrieveUsers() {
        //JSON parser object to parse read file
        JSONParser jsonParser = new JSONParser();

        try (FileReader reader = new FileReader(PERSIST_DATA_FOLDER + "users.json")) {
            //Read JSON file
            Object obj = jsonParser.parse(reader);

            usersJSON = (JSONArray) obj;
            for (Object userObject : usersJSON) {
                JSONObject userJson = (JSONObject) userObject;
                String pseudo = (String) userJson.get("pseudo");
                String lastConnection = (String) userJson.get("lastConnection");
                String lastConversation = (String) userJson.get("lastConversation");
                User user = new User(pseudo, LocalDateTime.parse(lastConnection), lastConversation);
                clients.put(user, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void retrieveConversations() {
        try {
            mutexConversationsJson.acquire();
            JSONParser jsonParser = new JSONParser();

            try (FileReader reader = new FileReader(PERSIST_DATA_FOLDER + "conversations.json")) {
                //Read JSON file
                Object obj = jsonParser.parse(reader);

                conversationsJSON = (JSONArray) obj;
                for (Object conversationObject : conversationsJSON) {
                    JSONObject conversationJson = (JSONObject) conversationObject;
                    String name = (String) conversationJson.get("name");

                    JSONArray usersArray = (JSONArray) conversationJson.get("users");
                    List<User> users = new ArrayList<>();
                    for (Object pseudo : usersArray) {
                        users.add(clients.keySet().stream().filter(x -> x.getPseudo().equals(pseudo.toString())).findFirst().get());
                    }

                    LocalDateTime timeLastMessage = LocalDateTime.parse(conversationJson.get("timeLastMessage").toString());

                    JSONArray messagesArray = (JSONArray) conversationJson.get("messages");
                    List<Message> messages = new ArrayList<>();
                    for (Object messageObject : messagesArray) {
                        JSONObject messageJson = (JSONObject) messageObject;

                        String pseudo = messageJson.get("pseudo").toString();
                        User user = users.stream().filter(x -> x.getPseudo().equals(pseudo)).findFirst().get();
                        String content = messageJson.get("content").toString();
                        LocalDateTime time = LocalDateTime.parse(messageJson.get("time").toString());

                        messages.add(new Message(content, user, time));
                    }
                    Conversation conversation = new Conversation(name, users, timeLastMessage);
                    messages.forEach(conversation::addMessage);
                    conversations.add(conversation);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mutexConversationsJson.release();
        }
    }

    public User manageConnection(Socket clientSocket) throws IOException {

        BufferedReader socIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintStream socOut = new PrintStream(clientSocket.getOutputStream());

        String msgReceived = socIn.readLine();
        String pseudo = null;

        while (!msgReceived.equals("START")) {
            pseudo = msgReceived;
            String finalPseudoName = pseudo;
            try {
                mutexClients.acquire();
                boolean pseudoUsed = clients.keySet().stream().anyMatch(x -> finalPseudoName.equals(x.getPseudo()));
                socOut.println(pseudoUsed);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                mutexClients.release();
            }
            msgReceived = socIn.readLine();
        }

        boolean newUser = Boolean.parseBoolean(socIn.readLine());

        User user = null;
        try {
            mutexClients.acquire();
            if (newUser) {
                user = new User(pseudo);
                user.setConnected(true);
                addUserToData(pseudo, user.getLastConnection(), user.getLastConversation());
                sendUsersAndGroups(socOut, user);
                socOut.println(user.getLastConnection().toString() + "," + user.getLastConversation());
                socOut.println("[[[[[FIN]]]]]");
                clients.put(user, clientSocket);
            } else {
                String finalPseudo = pseudo;
                user = clients.keySet().stream().filter(x -> x.getPseudo().equals(finalPseudo)).findFirst().get();
                user.setConnected(true);
                sendUsersAndGroups(socOut, user);
                socOut.println(user.getLastConnection().toString() + "," + user.getLastConversation());
                sendConversationsToUser(socOut, user);
                clients.replace(user, clientSocket);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            mutexClients.release();
        }

        return user;
    }

    public void sendConversationsToUser(PrintStream socOut, User user) {
        try {
            mutexConversations.acquire();
            conversations.sort(new Comparator<Conversation>() {
                @Override
                public int compare(Conversation o1, Conversation o2) {
                    return o1.getTimeLastMessage().compareTo(o2.getTimeLastMessage());
                }
            });
            for (Conversation conversation : conversations.stream().filter(x -> x.getUsers().contains(user)).collect(Collectors.toList())) {
                for (Message message : conversation.getMessages()) {
                    String conversationName = conversation.getName();
                    if (conversationName.equals("PRIVATE")) {
                        conversationName = conversation.getUsers().stream().filter(x -> !x.equals(user)).findFirst().get().getPseudo();
                    }
                    socOut.println("[[[[[MESSAGE]]]]]{" + conversationName + "}{" + message.getUser().getPseudo() + "}{" + message.getContent() + "}{" + message.getTime() + "}");
                }
            }
            socOut.println("[[[[[FIN]]]]]");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mutexConversations.release();
        }
    }

    public void sendUsersAndGroups(PrintStream socOut, User user) {
        try {
            mutexConversations.acquire();
            String lineToSend = "[[[[[INIT LIST]]]]]";
            for (User oneUser : clients.keySet().stream().filter(x -> !x.equals(user)).collect(Collectors.toList())) {
                lineToSend += "{" + oneUser.getPseudo() + "[" + oneUser.isConnected() + "]}";
            }
            lineToSend += "[GROUPS]";
            for (Conversation conversation : conversations.stream().filter(x -> !x.getName().equals("PRIVATE")).filter(x -> x.getUsers().contains(user)).collect(Collectors.toList())) {
                lineToSend += "{" + conversation.getName() + "[";
                for (User groupUser : conversation.getUsers().stream().filter(x -> !x.equals(user)).collect(Collectors.toList())) {
                    lineToSend += "{" + groupUser.getPseudo() + "}";
                }
                lineToSend += "]}";
            }
            socOut.println(lineToSend);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mutexConversations.release();
        }
    }

    public void addUserToData(String pseudo, LocalDateTime lastConnection, String lastConversation) {
        try {
            mutexUsersJson.acquire();

            JSONObject userJson = new JSONObject();
            userJson.put("pseudo", pseudo);
            userJson.put("lastConnection", lastConnection.toString());
            userJson.put("lastConversation", lastConversation);
            usersJSON.add(userJson);

            try (FileWriter file = new FileWriter(PERSIST_DATA_FOLDER + "users.json")) {
                //We can write any JSONArray or JSONObject instance to the file
                file.write(usersJSON.toJSONString());
                file.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mutexUsersJson.release();
        }
    }

}


