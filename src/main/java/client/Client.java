package client;

import model.*;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

public class Client {
    private Socket echoSocket = null;
    private PrintStream socOut = null;
    private BufferedReader stdIn = null;
    private BufferedReader socIn = null;
    private ChatFrame chatFrame;
    private ConnectionFrame connectionFrame;
    private String pseudoCurrentClient;
    private LocalDateTime lastConnection;
    private String nameCurrentConversation = "";
    private final Map<String, List<MessageGUI>> messagesInConversation = new HashMap<>();
    private final Map<String, LocalDateTime> conversations = new HashMap<>();
    private final Map<String, List<String>> groups = new HashMap<>();
    private final Map<String, Boolean> allUsers = new HashMap<>();
    private boolean historyDone = false;

    // if our arg != 2 then it will throw exception.
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Usage: java client.Client <server.Server host> <server.Server port>");
            System.exit(1);
        }

        Client client = new Client();
        client.connectClient(args[0], Integer.parseInt(args[1]));

    }

    public Client() {
    }

    public void connectClient(String host, int port) throws IOException {

        try {
            // creation socket ==> connexion
            // this we are creating a stream socket and connecting it to the specified port number
            echoSocket = new Socket(host, port);

            socIn = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
            //creates new input stream without flushing automatically
            socOut= new PrintStream(echoSocket.getOutputStream());
            stdIn = new BufferedReader(new InputStreamReader(System.in));

        //exception handling
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host:" + host);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for "
                    + "the connection to:"+ host);
            System.exit(1);
        }

        connectionFrame = new ConnectionFrame(this, 500, 300);
        connectionFrame.initChoice();

    }

    public boolean checkPseudo(String pseudo) throws IOException {
        System.out.println("Check if pseudo " + pseudo + " exists or not...");
        socOut.println(pseudo);
        boolean res = Boolean.parseBoolean(socIn.readLine());
        if (res) {
            System.out.println("Pseudo " + pseudo + " exists.");
        } else {
            System.out.println("Pseudo " + pseudo + " does not exist.");
        }
        return res;
    }
    //when the connection is established it will prompt a new java frame for us
    public void sendStartSignal(boolean newUser) throws IOException {
        System.out.println("Ready to start !");
        socOut.println("START");
        socOut.println(newUser);
        chatFrame = new ChatFrame(this);
        chatFrame.initChatFrame(900, 750);
        getHistoryAndListUsersAnsGroups();
        historyDone = true;;
        startListening();
    }
    //here you are putting the users and group into a list.
    public void getHistoryAndListUsersAnsGroups() throws IOException {
        String line = socIn.readLine();

        line = line.substring(line.indexOf("{"));
        while (!line.startsWith("[GROUPS]")) {
            String pseudo = line.substring(line.indexOf("{") + 1, line.indexOf("["));
            boolean connected = Boolean.parseBoolean(line.substring(line.indexOf("[")+1, line.indexOf("]")));
            line = line.substring(line.indexOf("}")+1);
            allUsers.put(pseudo, connected);
            conversations.put(pseudo, null);
        }
        if (!line.equals("[GROUPS]")) {
            line = line.substring(line.indexOf("{"));
            while (line.contains("{")) {
                String groupName = line.substring(line.indexOf("{") + 1, line.indexOf("["));
                String users = line.substring(line.indexOf("[") + 1, line.indexOf("]"));
                List<String> groupUsers = new ArrayList<>();
                line = line.substring(line.indexOf("]}") + 2);
                while (users.contains("{")) {
                    String oneUser = users.substring(users.indexOf("{") + 1, users.indexOf("}"));
                    if(!oneUser.equals(pseudoCurrentClient)) groupUsers.add(oneUser);
                    users = users.substring(users.indexOf("}") + 1);
                }
                groups.put(groupName, groupUsers);
                conversations.put(groupName, null);
            }
        }

        String lineLastInfo = socIn.readLine();
        lastConnection = LocalDateTime.parse(lineLastInfo.substring(0, lineLastInfo.indexOf(",")));
        String lastConversation = lineLastInfo.substring(lineLastInfo.indexOf(",")+1);

        line = socIn.readLine();
        String conversationName = "";
        while (line.startsWith("[[[[[MESSAGE]]]]]")) {
            conversationName = line.substring(line.indexOf("{") + 1, line.indexOf("}"));
            getAndDisplayMessage(line);
            line = socIn.readLine();
        }

        nameCurrentConversation = lastConversation;
        if (!conversationName.equals("")) {
            chatFrame.getConversationsContainer().setActiveConversation(nameCurrentConversation);
            messagesInConversation.get(nameCurrentConversation).forEach(x -> chatFrame.getMessagesContainer().addMessage(x));
            socOut.println("[[[[[CONVERSATION]]]]]{" + nameCurrentConversation + "}");
            chatFrame.updateHeaderChat();
        }
    }

    public void displayListUsers(String filter) {
        chatFrame.getConversationsSearchContainer().getContentPersons().initContainer();

        sortAllUsers(filter).forEach(x -> chatFrame.getConversationsSearchContainer().getContentPersons().addConversationInSearch(x.getKey(), x.getValue()));
    }

    public Stream<Map.Entry<String, Boolean>> sortAllUsers(String filter) {
        return allUsers.entrySet().stream().filter(x -> x.getKey().toLowerCase().contains(filter.toLowerCase())).sorted(new Comparator<Map.Entry<String, Boolean>>() {
            @Override
            public int compare(Map.Entry<String, Boolean> o1, Map.Entry<String, Boolean> o2) {
                if (o1.getValue() == o2.getValue()) {
                    if (conversations.get(o1.getKey()) != null && conversations.get(o2.getKey()) != null) {
                        return -conversations.get(o1.getKey()).compareTo(conversations.get(o2.getKey()));
                    } else if (conversations.get(o1.getKey()) != null && conversations.get(o2.getKey()) == null) {
                        return -1;
                    } else if (conversations.get(o1.getKey()) == null && conversations.get(o2.getKey()) != null) {
                        return 1;
                    } else {
                        return o1.getKey().compareTo(o2.getKey());
                    }
                } else {
                    if (o1.getValue()) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
            }
        });
    }

    public void displayListGroups(String filter) {
        chatFrame.getConversationsSearchContainer().getContentGroups().initContainer();
        groups.entrySet().stream().filter(x -> x.getKey().toLowerCase().contains(filter.toLowerCase())).sorted(new Comparator<Map.Entry<String, List<String>>>() {
            @Override
            public int compare(Map.Entry<String, List<String>> o1, Map.Entry<String, List<String>> o2) {
                if ((o1.getValue().stream().anyMatch(allUsers::get) && o2.getValue().stream().anyMatch(allUsers::get)) ||
                    (o1.getValue().stream().noneMatch(allUsers::get) && o2.getValue().stream().noneMatch(allUsers::get))) {
                    if (conversations.get(o1.getKey()) != null && conversations.get(o2.getKey()) != null) {
                        return  -conversations.get(o1.getKey()).compareTo(conversations.get(o2.getKey()));
                    } else if (conversations.get(o1.getKey()) != null && conversations.get(o2.getKey()) == null) {
                        return -1;
                    } else if (conversations.get(o1.getKey()) == null && conversations.get(o2.getKey()) != null) {
                        return 1;
                    } else {
                        return o1.getKey().compareTo(o2.getKey());
                    }
                } else {
                    if (o1.getValue().stream().anyMatch(allUsers::get)) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
            }
        }).forEach(x -> chatFrame.getConversationsSearchContainer().getContentGroups().addConversationInSearch(x.getKey(), x.getValue().stream().anyMatch(allUsers::get)));
    }

    public void getAndDisplayMessage(String line) {
        String conversationName = line.substring(line.indexOf("{") + 1, line.indexOf("}"));
        line = line.substring(line.indexOf("}") + 1);
        String pseudo = line.substring(line.indexOf("{") + 1, line.indexOf("}"));
        line = line.substring(line.indexOf("}") + 1);
        String content = line.substring(line.indexOf("{") + 1, line.indexOf("}"));
        line = line.substring(line.indexOf("}") + 1);
        String time = line.substring(line.indexOf("{") + 1, line.indexOf("}"));
        line = line.substring(line.indexOf("}") + 1);

        String realPseudo = pseudo;
        if (pseudo.equals(pseudoCurrentClient)) {
            pseudo = "Moi";
        }

        LocalDateTime msgTime = LocalDateTime.parse(time);
        String day = msgTime.getDayOfMonth() + "/" + msgTime.getMonthValue() + "/" + msgTime.getYear();
        String hour = time.substring(11,16);
        MessageGUI message = new MessageGUI(pseudo, content, day, hour);

        if (conversations.containsKey(conversationName)) {
            conversations.replace(conversationName, msgTime);
        } else {
            conversations.put(conversationName, msgTime);
            if (!allUsers.containsKey(conversationName)) {
                List<String> users = new ArrayList<>();
                while (line.contains("{")) {
                    String oneUser = line.substring(line.indexOf("{") + 1, line.indexOf("}"));
                    if (!oneUser.equals(pseudoCurrentClient)) users.add(oneUser);
                    line = line.substring(line.indexOf("}") + 1);
                }
                groups.put(conversationName, users);
                if (realPseudo.equals(pseudoCurrentClient) && historyDone) {
                    chatFrame.getCardLayoutLeftPanel().previous(chatFrame.getLeftPanel());
                    chatFrame.getGroupName().setText("Nom du groupe");
                }
            }
        }

        if (!nameCurrentConversation.equals(conversationName)) {
            if (messagesInConversation.containsKey(conversationName)) {
                chatFrame.getConversationsContainer().updateConversation(conversationName, historyDone);
            } else {
                boolean state = getState(conversationName);
                chatFrame.getConversationsContainer().addConversation(conversationName, state, historyDone);
                messagesInConversation.put(conversationName, new ArrayList<>());
            }
            if (LocalDateTime.parse(time).compareTo(lastConnection) > 0) {
                chatFrame.getConversationsContainer().setMissedConservation(conversationName);
            }
        } else {
            chatFrame.getMessagesContainer().addMessage(message);
        }
        messagesInConversation.get(conversationName).add(message);

    }

    public boolean getState(String conversationName) {
        boolean state = false;
        if (allUsers.containsKey(conversationName)) {
            state = allUsers.get(conversationName);
        } else if (groups.containsKey(conversationName)) {
            state = groups.get(conversationName).stream().anyMatch(allUsers::get);
        }
        return state;
    }

    public void displayMessagesWhenConversationChanges(String conversationName) {
        nameCurrentConversation = conversationName;
        chatFrame.updateHeaderChat();
        chatFrame.getMessagesContainer().removeAll();
        chatFrame.getMessagesContainer().initContainer();
        if (messagesInConversation.get(conversationName).isEmpty()) {
            chatFrame.getMessagesContainer().setLocation(0,0);
        }
        messagesInConversation.get(conversationName).forEach(x -> chatFrame.getMessagesContainer().addMessage(x));
    }

    private void startListening() {
        Thread listeningThread = new Thread(() -> {
            while (!echoSocket.isClosed()) {
                try {
                    String received = socIn.readLine();
                    if (received.startsWith("[[[[[STATE]]]]]")) {
                        getAndUpdateStateUsers(received);
                    } else if (received.equals("[[[[[NEW GROUP]]]]]{false}")) {
                        generateErrorMessageInGroupCreation();
                    } else {
                        getAndDisplayMessage(received);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        listeningThread.start();
    }

    public void generateErrorMessageInGroupCreation() {
        chatFrame.getGroupDescriptionPanel().setBackground(Color.red);
        chatFrame.getGroupDescription().setText("Nom déjà utilisé !");
        chatFrame.revalidate();
    }

    public void getAndUpdateStateUsers(String line) {
        String name = line.substring(line.indexOf("{")+1, line.indexOf("}"));
        line = line.substring(line.indexOf("}")+1);
        boolean state = Boolean.parseBoolean(line.substring(line.indexOf("{")+1, line.indexOf("}")));
        System.out.println("Connection of " + name + " : " + state);
        if (state) {
            if (allUsers.containsKey(name)) {
                allUsers.replace(name, state);
            } else {
                allUsers.put(name, state);
            }
        } else {
            allUsers.replace(name, state);
        }

        chatFrame.getConversationsContainer().changeState(name, state);
        groups.entrySet().stream().filter(x -> x.getValue().contains(name)).forEach(x -> {
            if (x.getValue().stream().anyMatch(allUsers::get)) {
                chatFrame.getConversationsContainer().changeState(x.getKey(), true);
            } else {
                chatFrame.getConversationsContainer().changeState(x.getKey(), false);
            }
        });

    }

    public void addMessageInCurrentConversation(MessageGUI message) {
        messagesInConversation.get(nameCurrentConversation).add(message);
    }

    public PrintStream getSocOut() {
        return socOut;
    }

    public String getPseudoCurrentClient() {
        return pseudoCurrentClient;
    }

    public void setPseudoCurrentClient(String pseudoCurrentClient) {
        this.pseudoCurrentClient = pseudoCurrentClient;
    }

    public String getNameCurrentConversation() {
        return nameCurrentConversation;
    }

    public Map<String, LocalDateTime> getConversations() {
        return conversations;
    }

    public ChatFrame getChatFrame() {
        return chatFrame;
    }

    public Map<String, List<MessageGUI>> getMessagesInConversation() {
        return messagesInConversation;
    }

    public Map<String, List<String>> getGroups() {
        return groups;
    }
}