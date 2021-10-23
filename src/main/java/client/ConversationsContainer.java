package client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.List;

public class ConversationsContainer extends JPanel {
    private final int CONTAINER_HEIGHT;
    private final int CONTAINER_WIDTH;
    private final int CONVERSATION_HEIGHT = 50;
    private final Map<String, JPanel> allConversationPanels = new HashMap<>();
    private final Map<String, JLabel> stateLabelsPerPanel = new HashMap<>();
    private int totalHeight;
    private Client client;
    private final int STATE_WIDTH = 110;

    public ConversationsContainer(Client client, int width, int height) {
        this.client = client;
        CONTAINER_WIDTH = width;
        CONTAINER_HEIGHT = height;
    }

    public void initContainer() {
        removeAll();
        totalHeight = 0;
        allConversationPanels.clear();
        setBounds(0, 0, CONTAINER_WIDTH, CONTAINER_HEIGHT);
        setPreferredSize(new Dimension(CONTAINER_WIDTH, CONTAINER_HEIGHT));
        setLayout(null);
        setBackground(new Color(54, 57, 63));
        revalidate();
    }

    public void addConversation(String conversationName, boolean state, boolean received) {
        JPanel conversationPanel = new JPanel();
        conversationPanel.setBounds(0, allConversationPanels.size() * CONVERSATION_HEIGHT, CONTAINER_WIDTH, CONVERSATION_HEIGHT);
        conversationPanel.setBackground(new Color(54, 57, 63));
        conversationPanel.setLayout(null);

        JLabel conversationLabel = new JLabel(conversationName);
        conversationLabel.setBounds(20, 5, CONTAINER_WIDTH - STATE_WIDTH - 20, CONVERSATION_HEIGHT);
        conversationLabel.setForeground(Color.white);
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(ChatFrame.ASSETS_FOLDER + "fonts/Poppins-Bold.ttf")).deriveFont(Font.PLAIN, 14);
            conversationLabel.setFont(font);
        } catch (Exception e) {
            e.printStackTrace();
        }
        conversationPanel.add(conversationLabel);

        JLabel stateLabel = new JLabel();
        stateLabel.setBounds(CONTAINER_WIDTH - STATE_WIDTH, 5, STATE_WIDTH, CONVERSATION_HEIGHT);
        if (state) {
            stateLabel.setText("Connecté");
            stateLabel.setForeground(new Color(31, 140, 48));
        } else {
            stateLabel.setText("Déconnecté");
            stateLabel.setForeground(Color.red);
        }
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(ChatFrame.ASSETS_FOLDER + "fonts/Poppins-Regular.ttf")).deriveFont(Font.PLAIN, 14);
            stateLabel.setFont(font);
        } catch (Exception e) {
            e.printStackTrace();
        }
        stateLabelsPerPanel.put(conversationName, stateLabel);
        conversationPanel.add(stateLabel);

        this.add(conversationPanel);
        allConversationPanels.put(conversationName, conversationPanel);
        totalHeight += CONVERSATION_HEIGHT;
        setPreferredSize(new Dimension(CONTAINER_WIDTH, Math.max(CONTAINER_HEIGHT, totalHeight)));
        updateConversation(conversationName, received);

        conversationPanel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setActiveConversation(conversationName);
                client.displayMessagesWhenConversationChanges(conversationName);
                client.getSocOut().println("[[[[[CONVERSATION]]]]]{" + conversationName + "}");
                revalidate();
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

        revalidate();
    }

    public void updateConversation(String conversationName, boolean received) {
        JPanel conversationPanel = allConversationPanels.get(conversationName);
        for (JPanel panel : allConversationPanels.values()) {
            if (panel.getLocation().y < conversationPanel.getLocation().y) {
                panel.setLocation(0, panel.getLocation().y + CONVERSATION_HEIGHT);
            }
        }
        conversationPanel.setLocation(0, 0);
        if (received) conversationPanel.setBackground(new Color(29, 29, 30));
        revalidate();
    }

    public void setActiveConversation(String conversationName) {
        JPanel conversationPanel = allConversationPanels.get(conversationName);
        conversationPanel.setBackground(new Color(47, 49, 54));
        for (JPanel panel : allConversationPanels.values().stream().filter(x -> !x.equals(conversationPanel)).collect(Collectors.toList())) {
            if (panel.getBackground().equals(new Color(47, 49, 54))) {
                panel.setBackground(new Color(54, 57, 63));
            }
        }
        revalidate();
    }

    public void setMissedConservation(String conversationName) {
        JPanel conversationPanel = allConversationPanels.get(conversationName);
        conversationPanel.setBackground(new Color(29, 29, 30));
    }

    public void changeState(String conversationName, boolean state) {
        if (stateLabelsPerPanel.containsKey(conversationName)) {
            if (state) {
                stateLabelsPerPanel.get(conversationName).setText("Connecté");
                stateLabelsPerPanel.get(conversationName).setForeground(new Color(31, 140, 48));
            } else {
                stateLabelsPerPanel.get(conversationName).setText("Déconnecté");
                stateLabelsPerPanel.get(conversationName).setForeground(Color.red);
            }
        }
    }

    public void addConversationInSearch(String conversationName, boolean state) {
        JPanel conversationPanel = new JPanel();
        conversationPanel.setBounds(0, allConversationPanels.size() * CONVERSATION_HEIGHT, CONTAINER_WIDTH, CONVERSATION_HEIGHT);
        conversationPanel.setBackground(new Color(54, 57, 63));
        conversationPanel.setLayout(null);

        JLabel conversationLabel = new JLabel(conversationName);
        conversationLabel.setBounds(20, 5, CONTAINER_WIDTH - STATE_WIDTH - 20, CONVERSATION_HEIGHT);
        conversationLabel.setForeground(Color.white);
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(ChatFrame.ASSETS_FOLDER + "fonts/Poppins-Bold.ttf")).deriveFont(Font.PLAIN, 14);
            conversationLabel.setFont(font);
        } catch (Exception e) {
            e.printStackTrace();
        }
        conversationPanel.add(conversationLabel);

        JLabel stateLabel = new JLabel();
        stateLabel.setBounds(CONTAINER_WIDTH - STATE_WIDTH, 5, STATE_WIDTH, CONVERSATION_HEIGHT);
        if (state) {
            stateLabel.setText("Connecté");
            stateLabel.setForeground(new Color(31, 140, 48));
        } else {
            stateLabel.setText("Déconnecté");
            stateLabel.setForeground(Color.red);
        }
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(ChatFrame.ASSETS_FOLDER + "fonts/Poppins-Regular.ttf")).deriveFont(Font.PLAIN, 14);
            stateLabel.setFont(font);
        } catch (Exception e) {
            e.printStackTrace();
        }
        conversationPanel.add(stateLabel);

        conversationPanel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!client.getNameCurrentConversation().equals(conversationName)) {
                    if (!client.getMessagesInConversation().containsKey(conversationName)) {
                        client.getChatFrame().getConversationsContainer().addConversation(conversationName, client.getState(conversationName), false);
                        client.getMessagesInConversation().put(conversationName, new ArrayList<>());
                    }
                    client.getChatFrame().getConversationsContainer().setActiveConversation(conversationName);
                    client.displayMessagesWhenConversationChanges(conversationName);
                    client.getSocOut().println("[[[[[CONVERSATION]]]]]{" + conversationName + "}");
                }

                client.getChatFrame().requestFocusInWindow();
                revalidate();
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

        this.add(conversationPanel);
        allConversationPanels.put(conversationName, conversationPanel);
        totalHeight += CONVERSATION_HEIGHT;
        setPreferredSize(new Dimension(CONTAINER_WIDTH, Math.max(CONTAINER_HEIGHT, totalHeight)));
        revalidate();
    }

    public void addConversationInGroupMembers(String conversationName, boolean state) {
        JPanel conversationPanel = new JPanel();
        conversationPanel.setBounds(0, allConversationPanels.size() * CONVERSATION_HEIGHT, CONTAINER_WIDTH, CONVERSATION_HEIGHT);
        conversationPanel.setBackground(new Color(54, 57, 63));
        conversationPanel.setLayout(null);

        JLabel conversationLabel = new JLabel(conversationName);
        conversationLabel.setBounds(20, 5, CONTAINER_WIDTH - STATE_WIDTH - 20, CONVERSATION_HEIGHT);
        conversationLabel.setForeground(Color.white);
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(ChatFrame.ASSETS_FOLDER + "fonts/Poppins-Bold.ttf")).deriveFont(Font.PLAIN, 14);
            conversationLabel.setFont(font);
        } catch (Exception e) {
            e.printStackTrace();
        }
        conversationPanel.add(conversationLabel);

        JLabel stateLabel = new JLabel();
        stateLabel.setBounds(CONTAINER_WIDTH - STATE_WIDTH, 5, STATE_WIDTH, CONVERSATION_HEIGHT);
        if (state) {
            stateLabel.setText("Connecté");
            stateLabel.setForeground(new Color(31, 140, 48));
        } else {
            stateLabel.setText("Déconnecté");
            stateLabel.setForeground(Color.red);
        }
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(ChatFrame.ASSETS_FOLDER + "fonts/Poppins-Regular.ttf")).deriveFont(Font.PLAIN, 14);
            stateLabel.setFont(font);
        } catch (Exception e) {
            e.printStackTrace();
        }
        conversationPanel.add(stateLabel);

        conversationPanel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (conversationPanel.getBackground().equals(new Color(54, 57, 63))) {
                    conversationPanel.setBackground(new Color(29, 29, 30));
                } else {
                    conversationPanel.setBackground(new Color(54, 57, 63));
                }
                revalidate();
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

        this.add(conversationPanel);
        allConversationPanels.put(conversationName, conversationPanel);
        totalHeight += CONVERSATION_HEIGHT;
        setPreferredSize(new Dimension(CONTAINER_WIDTH, Math.max(CONTAINER_HEIGHT, totalHeight)));
        revalidate();
    }

    public List<String> getSelectedUsersForGroup() {
        List<String> selectedUsers = new ArrayList<>();
        allConversationPanels.entrySet().stream().filter(x -> x.getValue().getBackground().equals(new Color(29, 29, 30))).forEach(x -> selectedUsers.add(x.getKey()));
        return selectedUsers;
    }
}


