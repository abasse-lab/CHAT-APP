package server;

import model.Conversation;
import model.Message;
import model.User;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

public class ClientThread extends Thread {
    private final User client;
    private final Socket clientSocket;
    private final Map<User, Socket> clients;
    private final List<Conversation> conversations;
    private final JSONArray conversationsJson;
    private final JSONArray usersJSON;
    private Conversation conversationToSend;
    //we are using semaphore because it controls access to a shared resource through counters.
    //If the counter is > 0 then access is allowed. if the opposite then it is denied
    private final Semaphore mutexConversationsJson;
    private final Semaphore mutexUsersJson;
    private final Semaphore mutexClients;
    private final Semaphore mutexConversations;
//due to the fact that it is a multithreaded application usually the resources have to share their resources and usually this is not good because
    //it might go into a race condition and to avoid this we use mutex to give it more to the critical section
    ClientThread(User client, Socket s, Map<User, Socket> clients, List<Conversation> conversations, JSONArray conversationsJSON, JSONArray usersJSON, Semaphore mutexConversationsJson, Semaphore mutexUsersJson, Semaphore mutexClients, Semaphore mutexConversations) {
        this.client = client;
        this.clientSocket = s;
        this.clients = clients;
        this.conversations = conversations;
        this.conversationsJson = conversationsJSON;
        this.usersJSON = usersJSON;
        this.mutexConversationsJson = mutexConversationsJson;
        this.mutexUsersJson = mutexUsersJson;
        this.mutexClients = mutexClients;
        this.mutexConversations = mutexConversations;
    }

    public void run() {
        try {
            BufferedReader socIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintStream socOut = new PrintStream(clientSocket.getOutputStream());
            try {
                mutexClients.acquire();
                for (Map.Entry<User, Socket> oneClient : clients.entrySet().stream().filter(x -> x.getValue() != null).filter(x -> !x.getKey().equals(client)).collect(Collectors.toSet())) {
                    if (oneClient.getValue() != null) {
                        PrintStream sOut = new PrintStream(oneClient.getValue().getOutputStream());
                        sOut.println("[[[[[STATE]]]]]{" + client.getPseudo() + "}{" + client.isConnected() + "}");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mutexClients.release();
            }
            while (true) {
                String line = socIn.readLine();
                try {
                    mutexClients.acquire();

                    if (line.equals("EXIT")) {
                        break;
                    } else if (line.equals("[[[[[LIST]]]]]")) {
                        String list = "[[[[[LIST]]]]]";
                        for (User user : clients.keySet().stream().filter(x -> !x.equals(client)).collect(Collectors.toList())) {
                            list += "{" + user.getPseudo() + "}";
                        }
                        socOut.println(list);

                    } else if (line.startsWith("[[[[[CONVERSATION]]]]]")) {
                        try {
                            mutexConversations.acquire();
                            String nameConversation = line.substring(line.indexOf("{") + 1, line.indexOf("}"));
                            Optional<Conversation> optionalConversation = conversations.stream().filter(x -> x.getName().equals(nameConversation)).findFirst();
                            if (optionalConversation.isPresent()) {
                                conversationToSend = optionalConversation.get();
                            } else {
                                Optional<Conversation> optionalPrivateConversation = conversations.stream()
                                        .filter(x -> x.getName().equals("PRIVATE"))
                                        .filter(x -> x.getUsers().contains(client))
                                        .filter(x -> x.getUsers().stream().map(User::getPseudo).collect(Collectors.toList()).contains(nameConversation))
                                        .findFirst();
                                if (optionalPrivateConversation.isPresent()) {
                                    conversationToSend = optionalPrivateConversation.get();
                                } else {
                                    User user = clients.keySet().stream().filter(x -> x.getPseudo().equals(nameConversation)).findFirst().get();
                                    Conversation conversation = new Conversation("PRIVATE", List.of(user, client), LocalDateTime.now());
                                    conversations.add(conversation);
                                    conversationToSend = conversation;
                                    addConversationToJson(conversation);
                                }
                            }
                            String lastConversation = conversationToSend.getName();
                            if (lastConversation.equals("PRIVATE")) {
                                lastConversation = conversationToSend.getUsers().stream().filter(x -> !x.equals(client)).findFirst().get().getPseudo();
                            }
                            client.setLastConversation(lastConversation);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } finally {
                            mutexConversations.release();
                        }

                    } else if (line.startsWith("[[[[[NEW GROUP]]]]]")) {
                        try {
                            mutexConversations.acquire();
                            List<String> userNames = new ArrayList<>();
                            String groupName = line.substring(line.indexOf("{{") + 2, line.indexOf("}}"));
                            if (conversations.stream().noneMatch(x -> x.getName().equals(groupName)) && clients.keySet().stream().noneMatch(x -> x.getPseudo().equals(groupName))) {
                                line = line.substring(line.indexOf("}}") + 2);
                                while (line.contains("{")) {
                                    String name = line.substring(line.indexOf("{") + 1, line.indexOf("}"));
                                    line = line.substring(line.indexOf("}") + 1);
                                    userNames.add(name);
                                }
                                List<User> users = new ArrayList<>();
                                for (String pseudo : userNames) {
                                    users.add(clients.keySet().stream().filter(x -> x.getPseudo().equals(pseudo)).findFirst().get());
                                }

                                Conversation conversation = new Conversation(groupName, users, LocalDateTime.now());
                                conversations.add(conversation);
                                conversationToSend = conversation;
                                addConversationToJson(conversation);

                                Message initMsg = new Message("Création du groupe " + groupName, client, LocalDateTime.now());
                                conversationToSend.addMessage(initMsg);
                                conversationToSend.setTimeLastMessage(initMsg.getTime());
                                addMessageToJson(conversationToSend, initMsg);
                                for (User user : conversationToSend.getUsers()) {
                                    Socket s = clients.entrySet()
                                            .stream()
                                            .filter(x -> x.getKey().equals(user))
                                            .findFirst()
                                            .get()
                                            .getValue();
                                    if (s != null) {
                                        PrintStream sOut = new PrintStream(s.getOutputStream());
                                        String lineToSend = "[[[[[MESSAGE]]]]]{" + conversationToSend.getName() + "}{" + client.getPseudo() + "}{" + initMsg.getContent() + "}{" + initMsg.getTime() + "}";
                                        for (Map.Entry<User, Socket> x : clients.entrySet().stream().filter(x -> conversationToSend.getUsers().contains(x.getKey())).collect(Collectors.toSet())) {
                                            lineToSend += "{" + x.getKey().getPseudo() + "}";
                                        }
                                        sOut.println(lineToSend);
                                    }
                                }
                            } else {
                                socOut.println("[[[[[NEW GROUP]]]]]{false}");
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } finally {
                            mutexConversations.release();
                        }

                    } else {
                        try {
                            mutexConversations.acquire();
                            Message message = new Message(line.substring(line.indexOf("]") + 1), client, LocalDateTime.parse(line.substring(line.indexOf("[") + 1, line.indexOf("]"))));
                            conversationToSend.addMessage(message);
                            conversationToSend.setTimeLastMessage(message.getTime());
                            addMessageToJson(conversationToSend, message);
                            for (User user : conversationToSend.getUsers()) {
                                Socket s = clients.entrySet()
                                        .stream()
                                        .filter(x -> x.getKey().equals(user))
                                        .findFirst()
                                        .get()
                                        .getValue();
                                if (!user.equals(client) && s != null) {
                                    PrintStream sOut = new PrintStream(s.getOutputStream());
                                    String conversationName = conversationToSend.getName();
                                    if (conversationName.equals("PRIVATE")) {
                                        conversationName = client.getPseudo();
                                    }
                                    String lineToSend = "[[[[[MESSAGE]]]]]{" + conversationName + "}{" + client.getPseudo() + "}{" + message.getContent() + "}{" + message.getTime() + "}";
                                    if (!conversationName.equals("PRIVATE")) {
                                        for (Map.Entry<User, Socket> x : clients.entrySet().stream().filter(x -> conversationToSend.getUsers().contains(x.getKey())).collect(Collectors.toSet())) {
                                            lineToSend += "{" + x.getKey().getPseudo() + "}";
                                        }
                                    }
                                    sOut.println(lineToSend);
                                }
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } finally {
                            mutexConversations.release();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    mutexClients.release();
                }
            }
        } catch (Exception e) {
            System.err.println("Error in server.Server:" + e);
            try {
                mutexClients.acquire();
                client.setLastConnection(LocalDateTime.now());
                client.setConnected(false);
                clients.replace(client, null);
                for (Map.Entry<User, Socket> oneClient : clients.entrySet().stream().filter(x -> !x.getKey().equals(client)).collect(Collectors.toSet())) {
                    if (oneClient.getValue() != null) {
                        PrintStream sOut = new PrintStream(oneClient.getValue().getOutputStream());
                        sOut.println("[[[[[STATE]]]]]{" + client.getPseudo() + "}{" + client.isConnected() + "}");
                    }
                }

                updateUserInJson(client.getPseudo(), client.getLastConnection(), client.getLastConversation());

            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                mutexClients.release();
            }
        }
    }

    private void updateUserInJson(String pseudo, LocalDateTime lastConnection, String lastConversation) {
        try {
            mutexUsersJson.acquire();

            Iterator<JSONObject> iterator = usersJSON.iterator();
            while (iterator.hasNext()) {
                JSONObject current = iterator.next();
                if (current.get("pseudo").equals(pseudo)) {
                    current.replace("lastConnection", lastConnection.toString());
                    current.replace("lastConversation", lastConversation);
                    break;
                }
            }

            try (FileWriter file = new FileWriter(Server.PERSIST_DATA_FOLDER + "users.json")) {
                //We can write any JSONArray or JSONObject instance to the file
                file.write(usersJSON.toJSONString());
                file.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            mutexUsersJson.release();
        }
    }

    public void addConversationToJson(Conversation conversation) {
        try {
            mutexConversationsJson.acquire();
            JSONObject conversationJson = new JSONObject();
            conversationJson.put("name", conversation.getName());

            JSONArray usersArray = new JSONArray();
            for (User user : conversation.getUsers()) {
                usersArray.add(user.getPseudo());
            }
            conversationJson.put("users", usersArray);

            conversationJson.put("timeLastMessage", conversation.getTimeLastMessage().toString());

            JSONArray messagesArray = new JSONArray();
            conversationJson.put("messages", messagesArray);

            conversationsJson.add(conversationJson);

            try (FileWriter file = new FileWriter(Server.PERSIST_DATA_FOLDER + "conversations.json")) {
                //We can write any JSONArray or JSONObject instance to the file
                file.write(conversationsJson.toJSONString());
                file.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mutexConversationsJson.release();
        }

    }

    public void addMessageToJson(Conversation conversation, Message message) {
        try {
            mutexConversationsJson.acquire();
            Iterator<JSONObject> iterator = conversationsJson.iterator();
            while (iterator.hasNext()) {
                JSONObject current = iterator.next();
                if (current.get("name").toString().equals(conversation.getName())) {
                    JSONArray usersArray = (JSONArray) current.get("users");
                    List<User> currentUsers = new ArrayList<>();
                    for (Object pseudo : usersArray) {
                        currentUsers.add(clients.keySet().stream().filter(x -> x.getPseudo().equals(pseudo.toString())).findFirst().get());
                    }
                    if (!conversation.getName().equals("PRIVATE") || currentUsers.equals(conversation.getUsers())) {
                        current.replace("timeLastMessage", conversation.getTimeLastMessage().toString());

                        JSONObject messageJson = new JSONObject();
                        messageJson.put("pseudo", message.getUser().getPseudo());
                        messageJson.put("content", message.getContent());
                        messageJson.put("time", message.getTime().toString());

                        JSONArray messagesArray = (JSONArray) current.get("messages");
                        messagesArray.add(messageJson);
                        break;
                    }
                }
            }

            try (FileWriter file = new FileWriter(Server.PERSIST_DATA_FOLDER + "conversations.json")) {
                //We can write any JSONArray or JSONObject instance to the file
                file.write(conversationsJson.toJSONString());
                file.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mutexConversationsJson.release();
        }
    }

}


