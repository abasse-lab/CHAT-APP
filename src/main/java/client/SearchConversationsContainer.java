package client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.FileInputStream;

public class SearchConversationsContainer extends JPanel {
    private final int CONTAINER_HEIGHT;
    private final int CONTAINER_WIDTH;
    private final int HEADER_HEIGHT = 40;
    private final Client client;
    private final JPanel headerPersons = new JPanel();
    private final JPanel headerGroups = new JPanel();
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private ConversationsContainer contentPersons;
    private ConversationsContainer contentGroups;

    public SearchConversationsContainer(Client client, int width, int height) {
        this.client = client;
        CONTAINER_WIDTH = width;
        CONTAINER_HEIGHT = height;
    }

    public ConversationsContainer getContentPersons() {
        return contentPersons;
    }

    public ConversationsContainer getContentGroups() {
        return contentGroups;
    }

    public void initContainer() {
        removeAll();
        setBounds(0, 0, CONTAINER_WIDTH, CONTAINER_HEIGHT);
        setPreferredSize(new Dimension(CONTAINER_WIDTH, CONTAINER_HEIGHT));
        setLayout(null);
        setBackground(new Color(54, 57, 63));
        setHeader(headerPersons, 0, "Personnes");
        setHeader(headerGroups, CONTAINER_WIDTH / 2, "Groupes");
        setContentPanel();
        this.add(headerPersons);
        this.add(headerGroups);
        revalidate();
    }

    public void setHeader(JPanel panel, int originX, String text) {
        panel.setBounds(originX, 0, CONTAINER_WIDTH / 2, HEADER_HEIGHT);
        panel.setBackground(new Color(54, 57, 63));
        JLabel label = new JLabel(text);
        label.setForeground(Color.white);
        label.setBorder(new EmptyBorder(5, 0, 0, 0));
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(ChatFrame.ASSETS_FOLDER + "fonts/Poppins-Bold.ttf")).deriveFont(Font.PLAIN, 14);
            label.setFont(font);
        } catch (Exception e) {
            e.printStackTrace();
        }
        panel.add(label);
    }

    public void setContentPanel() {
        contentPersons = new ConversationsContainer(client, CONTAINER_WIDTH, CONTAINER_HEIGHT - HEADER_HEIGHT);
        contentPersons.initContainer();
        JScrollPane scrollPersons = new JScrollPane(contentPersons);
        scrollPersons.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPersons.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPersons.setBounds(0, 0, CONTAINER_WIDTH, CONTAINER_HEIGHT - HEADER_HEIGHT);
        scrollPersons.setBorder(null);

        JPanel groupsContainer = new JPanel();
        groupsContainer.setBounds(0,0,CONTAINER_WIDTH,CONTAINER_HEIGHT - HEADER_HEIGHT);
        groupsContainer.setLayout(null);
        contentGroups = new ConversationsContainer(client, CONTAINER_WIDTH, CONTAINER_HEIGHT - HEADER_HEIGHT - 50);
        contentGroups.initContainer();
        JScrollPane scrollGroups = new JScrollPane(contentGroups);
        scrollGroups.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollGroups.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollGroups.setBounds(0, 0, CONTAINER_WIDTH, CONTAINER_HEIGHT - HEADER_HEIGHT - 50);
        scrollGroups.setBorder(null);
        groupsContainer.add(addButtonNewGroup());
        groupsContainer.add(scrollGroups);

        contentPanel = new JPanel();
        contentPanel.setBounds(0, HEADER_HEIGHT, CONTAINER_WIDTH, CONTAINER_HEIGHT - HEADER_HEIGHT);
        cardLayout = new CardLayout();
        contentPanel.setLayout(cardLayout);
        contentPanel.add("a", scrollPersons);
        contentPanel.add("b", groupsContainer);
        this.add(contentPanel);
        headerGroups.setBackground(new Color(64, 68, 75));

        headerPersons.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (headerPersons.getBackground().equals(new Color(64, 68, 75))) {
                    headerPersons.setBackground(new Color(54, 57, 63));
                    headerGroups.setBackground(new Color(64, 68, 75));
                    cardLayout.previous(contentPanel);
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
        headerGroups.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (headerGroups.getBackground().equals(new Color(64, 68, 75))) {
                    headerGroups.setBackground(new Color(54, 57, 63));
                    headerPersons.setBackground(new Color(64, 68, 75));
                    cardLayout.next(contentPanel);
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

    public JPanel addButtonNewGroup() {
        JPanel newGroup = new JPanel();
        newGroup.setBounds(0,CONTAINER_HEIGHT - HEADER_HEIGHT - 50,CONTAINER_WIDTH, 50);
        newGroup.setBackground(new Color(64, 68, 75));
        newGroup.setLayout(null);
        JLabel label = new JLabel("Nouveau groupe");
        label.setBounds(90,0,CONTAINER_WIDTH,50);
        label.setForeground(Color.white);
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(ChatFrame.ASSETS_FOLDER + "fonts/Poppins-Bold.ttf")).deriveFont(Font.PLAIN, 14);
            label.setFont(font);
        } catch (Exception e) {
            e.printStackTrace();
        }
        newGroup.add(label);
        newGroup.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                client.getChatFrame().getCardLayoutLeftPanel().next(client.getChatFrame().getLeftPanel());
                client.getChatFrame().getAllMembersForGroups().initContainer();
                client.sortAllUsers("").forEach(x -> client.getChatFrame().getAllMembersForGroups().addConversationInGroupMembers(x.getKey(), x.getValue()));
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
        return newGroup;
    }
}


