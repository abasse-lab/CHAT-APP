package client;

import model.Conversation;
import model.MessageGUI;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.io.FileInputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class ChatFrame extends JFrame {
    private MessagesContainer messagesContainer;
    private ConversationsContainer conversationsContainer;
    private SearchConversationsContainer conversationsSearchContainer;
    private JScrollPane scrollConversations;
    private JTextArea textArea;
    private JTextField search;
    private final Client client;
    private JPanel conversationsPanel;
    private JPanel headerConversationsPanel;
    private JPanel contentConversationsPanel;
    private CardLayout cardLayoutConversations;
    private JPanel leftPanel;
    private JPanel newGroupPanel;
    private CardLayout cardLayoutLeftPanel;
    private ConversationsContainer allMembersForGroups;
    private JPanel groupDescriptionPanel;
    private JLabel groupDescription;
    private JTextField groupName;
    private final JLabel title = new JLabel();
    private final JLabel groupMembers = new JLabel();
    private int WIDTH_LEFT;
    private int WIDTH_RIGHT;
    private JPanel headerChatPanel;
    public static final String ASSETS_FOLDER = "../../assets/";

    public ChatFrame(Client client) {
        this.client = client;
    }

    public void initChatFrame(int width, int height) {
        setTitle("Chat - TP Socket");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);
        setSize(width, height);
        setMainContainer(width, height);
        setResizable(false);
        revalidate();

        addWindowFocusListener(new WindowAdapter() {
            public void windowGainedFocus(WindowEvent e) {
                textArea.requestFocusInWindow();
            }
        });

        setVisible(true);
    }

    public void setMainContainer(int width, int height) {
        JPanel container = new JPanel();
        container.setLayout(null);
        container.setBackground(new Color(47, 49, 54));
        setContentPane(container);

        WIDTH_LEFT = width / 3;
        WIDTH_RIGHT = 2 * width / 3;

        container.add(setLeftTopPanel(WIDTH_LEFT));
        setLeftPanel(WIDTH_LEFT, height - 120);
        container.add(leftPanel);
        setHeaderConversationsPanel();
        container.add(headerChatPanel);
        container.add(setMessagesPanel(WIDTH_LEFT, WIDTH_RIGHT - 55, 65 * height / 100));
        container.add(setNewMessagePanel(WIDTH_LEFT, WIDTH_RIGHT - 55, height, 65 * height / 100));

        container.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getSource() != search && search.hasFocus()) {
                    requestFocusInWindow();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
    }

    public void setHeaderConversationsPanel() {
        headerChatPanel = new JPanel();
        headerChatPanel.setBounds(WIDTH_LEFT + 20,0,WIDTH_RIGHT - 40, 100);
        headerChatPanel.setLayout(null);
        headerChatPanel.setBackground(new Color(47, 49, 54));

        updateHeaderChat();

        headerChatPanel.add(title);
        headerChatPanel.add(groupMembers);
    }

    public void updateHeaderChat() {
        setTitleLabel();
        setGroupMembersLabel();
        revalidate();
    }

    public void setTitleLabel() {
        String conversationName = client.getNameCurrentConversation();
        if (conversationName.equals("")) {
            conversationName = "Chat - TP Socket";
        }
        title.setText(conversationName);
        try {
            Font fontTitle = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(ChatFrame.ASSETS_FOLDER + "fonts/Poppins-Bold.ttf")).deriveFont(Font.PLAIN, 24);
            title.setFont(fontTitle);
        } catch (Exception e) {
            e.printStackTrace();
        }
        title.setForeground(Color.WHITE);
        int widthLabel = title.getPreferredSize().width;
        title.setLocation((WIDTH_RIGHT - 40 - widthLabel) / 2, 30);
        title.setSize(widthLabel, 30);
        revalidate();
    }

    public void setGroupMembersLabel() {
        String users = "";
        if (client.getGroups().containsKey(client.getNameCurrentConversation())) {
            for (String user : client.getGroups().get(client.getNameCurrentConversation())) {
                users += user + ", ";
            }
            users = users.substring(0, users.lastIndexOf(", "));
        }
        groupMembers.setText(users);
        try {
            Font fontTitle = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(ChatFrame.ASSETS_FOLDER + "fonts/Poppins-Regular.ttf")).deriveFont(Font.PLAIN, 14);
            groupMembers.setFont(fontTitle);
        } catch (Exception e) {
            e.printStackTrace();
        }
        groupMembers.setForeground(Color.WHITE);
        int widthLabel = groupMembers.getPreferredSize().width;
        groupMembers.setLocation((WIDTH_RIGHT - 40 - widthLabel) / 2, 60);
        groupMembers.setSize(widthLabel, 30);

        revalidate();
    }


    public JPanel setLeftTopPanel(int width) {
        JPanel leftTopPannel = new JPanel();
        leftTopPannel.setBounds(0, 0, width, 100);
        leftTopPannel.setBackground(new Color(47, 49, 54));
        leftTopPannel.setLayout(null);

        JLabel profil = new JLabel();
        profil.setBounds(15, 15, 40, 40);
        try {
            Image img = ImageIO.read(new FileInputStream(ChatFrame.ASSETS_FOLDER + "img/profil-icon.png")).getScaledInstance(40, 40, Image.SCALE_DEFAULT);
            profil.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            e.printStackTrace();
        }
        leftTopPannel.add(profil);

        JLabel userName = new JLabel(client.getPseudoCurrentClient());
        try {
            Font fontTitle = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(ChatFrame.ASSETS_FOLDER + "fonts/Poppins-Bold.ttf")).deriveFont(Font.PLAIN, 20);
            userName.setFont(fontTitle);
        } catch (Exception e) {
            e.printStackTrace();
        }
        userName.setForeground(Color.WHITE);
        userName.setBounds(70, 20, width - 50, 30);
        leftTopPannel.add(userName);

        return leftTopPannel;
    }

    public void setLeftPanel(int width, int height) {
        leftPanel = new JPanel();
        leftPanel.setBounds(0, 60, width, height);
        cardLayoutLeftPanel = new CardLayout();
        leftPanel.setLayout(cardLayoutLeftPanel);
        setConversationsPanel(width, height);
        setNewGroupPanel(width, height);
        leftPanel.add("a", conversationsPanel);
        leftPanel.add("b", newGroupPanel);
    }

    public void setConversationsPanel(int width, int height) {
        conversationsPanel = new JPanel();
        conversationsPanel.setBounds(0, 0, width, height);
        conversationsPanel.setLayout(null);
        headerConversationsPanel = new JPanel();
        headerConversationsPanel.setBounds(0, 0, width, 40);
        headerConversationsPanel.setLayout(null);
        setSearchField(width);
        contentConversationsPanel = new JPanel();
        contentConversationsPanel.setBounds(0, 40, width, height - 40);
        cardLayoutConversations = new CardLayout();
        contentConversationsPanel.setLayout(cardLayoutConversations);
        setAllConversationsPanel(width, height - 40);
        setConversationsSearchPanel(width, height - 40);
        contentConversationsPanel.add("a", scrollConversations);
        contentConversationsPanel.add("b", conversationsSearchContainer);
        conversationsPanel.add(headerConversationsPanel);
        conversationsPanel.add(contentConversationsPanel);
    }

    public void setNewGroupPanel(int width, int height) {
        newGroupPanel = new JPanel();
        newGroupPanel.setBounds(0, 0, width, height);
        newGroupPanel.setLayout(null);
        newGroupPanel.setBackground(new Color(54, 57, 63));

        groupName = new JTextField("Nom du groupe");
        groupName.setBounds(0, 0, width, 40);
        groupName.setBackground(new Color(64, 68, 75));
        groupName.setForeground(Color.white);
        groupName.setBorder(new EmptyBorder(0, 20, 0, 20));
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(ChatFrame.ASSETS_FOLDER + "fonts/Poppins-Regular.ttf")).deriveFont(Font.PLAIN, 14);
            groupName.setFont(font);
        } catch (Exception e) {
            e.printStackTrace();
        }
        groupName.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                groupName.setText("");
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (groupName.getText().equals("")) {
                    groupName.setText("Nom du groupe");
                }
            }
        });
        newGroupPanel.add(groupName);

        groupDescriptionPanel = new JPanel();
        groupDescriptionPanel.setBounds(0, 40, width, 50);
        groupDescriptionPanel.setLayout(null);
        groupDescriptionPanel.setBackground(new Color(54, 57, 63));

        groupDescription = new JLabel("Sélectionnez les membres :");
        groupDescription.setBounds(20, 5, width - 20, 45);
        groupDescription.setForeground(Color.white);
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(ChatFrame.ASSETS_FOLDER + "fonts/Poppins-Bold.ttf")).deriveFont(Font.PLAIN, 14);
            groupDescription.setFont(font);
        } catch (Exception e) {
            e.printStackTrace();
        }
        groupDescriptionPanel.add(groupDescription);
        newGroupPanel.add(groupDescriptionPanel);

        allMembersForGroups = new ConversationsContainer(client, width, height - 140);
        allMembersForGroups.initContainer();
        JScrollPane scrollPane = new JScrollPane(allMembersForGroups);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBounds(0, 90, width, height - 140);
        scrollPane.setBorder(null);
        newGroupPanel.add(scrollPane);

        JPanel cancelPanel = new JPanel();
        cancelPanel.setBounds(0, height - 50, width / 2, 50);
        cancelPanel.setBackground(new Color(64, 68, 75));
        cancelPanel.setLayout(null);

        JLabel cancel = new JLabel("Annuler");
        cancel.setBounds(50, 5, width / 2 - 20, 45);
        cancel.setForeground(Color.white);
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(ChatFrame.ASSETS_FOLDER + "fonts/Poppins-Bold.ttf")).deriveFont(Font.PLAIN, 14);
            cancel.setFont(font);
        } catch (Exception e) {
            e.printStackTrace();
        }
        cancelPanel.add(cancel);
        cancelPanel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                cardLayoutLeftPanel.previous(leftPanel);
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
        newGroupPanel.add(cancelPanel);

        JPanel validatePanel = new JPanel();
        validatePanel.setBounds(width / 2, height - 50, width / 2, 50);
        validatePanel.setBackground(new Color(29, 29, 30));
        validatePanel.setLayout(null);

        JLabel validate = new JLabel("Créer le groupe");
        validate.setBounds(20, 5, width / 2 - 20, 45);
        validate.setForeground(Color.white);
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(ChatFrame.ASSETS_FOLDER + "fonts/Poppins-Bold.ttf")).deriveFont(Font.PLAIN, 14);
            validate.setFont(font);
        } catch (Exception e) {
            e.printStackTrace();
        }
        validatePanel.add(validate);
        validatePanel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!groupName.getText().equals("") && !groupName.getText().equals("Nom du groupe")) {
                    List<String> selectedUsers = allMembersForGroups.getSelectedUsersForGroup();
                    String lineToSend = "[[[[[NEW GROUP]]]]]{{" + groupName.getText() + "}}";
                    for (String user : selectedUsers) {
                        lineToSend += "{" + user + "}";
                    }
                    lineToSend += "{" + client.getPseudoCurrentClient() + "}";
                    client.getSocOut().println(lineToSend);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
        newGroupPanel.add(validatePanel);
    }

    public void setSearchField(int width) {
        search = new JTextField("Rechercher");
        search.setBounds(0, 0, width, 40);
        search.setBackground(new Color(64, 68, 75));
        search.setForeground(Color.white);
        search.setBorder(new EmptyBorder(0, 20, 0, 20));
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(ChatFrame.ASSETS_FOLDER + "fonts/Poppins-Regular.ttf")).deriveFont(Font.PLAIN, 14);
            search.setFont(font);
        } catch (Exception e) {
            e.printStackTrace();
        }
        search.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                search.setText("");
                client.displayListUsers("");
                client.displayListGroups("");
                cardLayoutConversations.next(contentConversationsPanel);
            }

            @Override
            public void focusLost(FocusEvent e) {
                search.setText("Rechercher");
                cardLayoutConversations.previous(contentConversationsPanel);
            }
        });

        search.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (!search.getText().equals("Recherche")) {
                    client.displayListUsers(search.getText());
                    client.displayListGroups(search.getText());
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (!search.getText().equals("Recherche")) {
                    client.displayListUsers(search.getText());
                    client.displayListGroups(search.getText());
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {

            }
        });

        headerConversationsPanel.add(search);
    }

    private void setAllConversationsPanel(int width, int height) {
        conversationsContainer = new ConversationsContainer(client, width, height);
        conversationsContainer.initContainer();
        scrollConversations = new JScrollPane(conversationsContainer);
        scrollConversations.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollConversations.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollConversations.setBounds(0, 0, width, height);
        scrollConversations.setBorder(null);
    }

    private void setConversationsSearchPanel(int width, int height) {
        conversationsSearchContainer = new SearchConversationsContainer(client, width, height);
        conversationsSearchContainer.initContainer();
    }

    private JScrollPane setMessagesPanel(int originX, int width, int height) {
        messagesContainer = new MessagesContainer(width, height);
        messagesContainer.initContainer();
        JScrollPane scrollMessages = new JScrollPane(messagesContainer);
        scrollMessages.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollMessages.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollMessages.setBounds(originX + 20, 100, width, height);
        scrollMessages.setBorder(null);
        return scrollMessages;
    }

    private JPanel setNewMessagePanel(int originX, int width, int height, int heightMessagePanel) {
        JPanel newMessagePanel = new JPanel();
        newMessagePanel.setBounds(originX + 20, 120 + heightMessagePanel, width, height - heightMessagePanel - 180);
        newMessagePanel.setBackground(new Color(64, 68, 75));
        newMessagePanel.setLayout(null);

        JLabel fakeMsg = new JLabel("Envoyer un message dans le chat");
        fakeMsg.setBounds(35, 12, width - 120, 40);
        fakeMsg.setForeground(Color.white);
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(ChatFrame.ASSETS_FOLDER + "fonts/Poppins-Regular.ttf")).deriveFont(Font.PLAIN, 14);
            fakeMsg.setFont(font);
        } catch (Exception e) {
            e.printStackTrace();
        }
        newMessagePanel.add(fakeMsg);

        textArea = new JTextArea("");
        textArea.setBounds(30, 25, width - 120, 40);
        textArea.setForeground(Color.white);
        textArea.setBackground(new Color(64, 68, 75));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.requestFocus();
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(ChatFrame.ASSETS_FOLDER + "fonts/Poppins-Regular.ttf")).deriveFont(Font.PLAIN, 14);
            textArea.setFont(font);
        } catch (Exception e) {
            e.printStackTrace();
        }

        textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                textArea.setPreferredSize(new Dimension(width - 200, (e.getOffset() / 20 + 1) * 12));
                if (e.getOffset() == 0) fakeMsg.setVisible(false);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                textArea.setPreferredSize(new Dimension(width - 200, (e.getOffset() / 20 + 1) * 12));
                if (e.getOffset() == 0) fakeMsg.setVisible(true);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBounds(30, 20, width - 120, 40);
        scrollPane.setBorder(null);

        newMessagePanel.add(scrollPane);

        JButton envoi = new JButton();
        try {
            Image img = ImageIO.read(new FileInputStream(ChatFrame.ASSETS_FOLDER + "img/send-icon.png")).getScaledInstance(40, 40, Image.SCALE_DEFAULT);
            envoi.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            e.printStackTrace();
        }
        envoi.setBorder(null);
        envoi.setBackground(new Color(64, 68, 75));
        envoi.setBounds(width - 60, 20, 40, 40);
        newMessagePanel.add(envoi);


        envoi.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!Objects.equals(client.getNameCurrentConversation(), "") && textArea.getText().length() > 0) {
                    LocalDateTime msgTime = LocalDateTime.now();
                    String day = msgTime.getDayOfMonth() + "/" + msgTime.getMonthValue() + "/" + msgTime.getYear();
                    String hour = msgTime.toString().substring(11,16);
                    MessageGUI m = new MessageGUI("Moi", textArea.getText(), day, hour);
                    messagesContainer.addMessage(m);
                    textArea.setText("");
                    client.getSocOut().println("[" + msgTime + "]" + m.getContent());
                    client.addMessageInCurrentConversation(m);
                    conversationsContainer.updateConversation(client.getNameCurrentConversation(), false);
                    client.getConversations().replace(client.getNameCurrentConversation(), LocalDateTime.now());
                }
            }
        });

        return newMessagePanel;
    }


    public MessagesContainer getMessagesContainer() {
        return messagesContainer;
    }

    public ConversationsContainer getConversationsContainer() {
        return conversationsContainer;
    }

    public SearchConversationsContainer getConversationsSearchContainer() {
        return conversationsSearchContainer;
    }

    public CardLayout getCardLayoutLeftPanel() {
        return cardLayoutLeftPanel;
    }

    public JPanel getLeftPanel() {
        return leftPanel;
    }

    public ConversationsContainer getAllMembersForGroups() {
        return allMembersForGroups;
    }

    public JPanel getGroupDescriptionPanel() {
        return groupDescriptionPanel;
    }

    public JLabel getGroupDescription() {
        return groupDescription;
    }

    public JTextField getGroupName() {
        return groupName;
    }
}
