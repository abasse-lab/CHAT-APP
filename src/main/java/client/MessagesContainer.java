package client;

import model.MessageGUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.FileInputStream;
import java.util.*;
import java.util.List;

public class MessagesContainer extends JPanel {
    private final int MESSAGE_HEIGHT = 25;
    private final int MESSAGE_WIDTH;
    private final int MARGIN = 20;
    private final int GAP = 10;
    private final int CONTAINER_HEIGHT;
    private final int CONTAINER_WIDTH;
    private int totalHeight;

    public MessagesContainer(int width, int height) {
        CONTAINER_HEIGHT = height;
        CONTAINER_WIDTH = width;
        MESSAGE_WIDTH = CONTAINER_WIDTH - 45;
    }

    public void initContainer() {
        totalHeight = GAP;
        setBounds(MARGIN, 100, CONTAINER_WIDTH, CONTAINER_HEIGHT);
        setPreferredSize(new Dimension(CONTAINER_WIDTH, CONTAINER_HEIGHT));
        setLayout(null);
        setBackground(new Color(54, 57, 63));
        revalidate();
    }

    public void addPartOfMessagePanel(JPanel messageContainer, int yLocation, int width, String text, String fontLocation) {
        JPanel panel = new JPanel();
        panel.setBounds(0, yLocation, width, MESSAGE_HEIGHT);
        panel.setBackground(new Color(54, 57, 63));
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel label = new JLabel(text);
        label.setForeground(Color.white);
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(fontLocation)).deriveFont(Font.PLAIN, 14);
            label.setFont(font);
        } catch (Exception e) {
            e.printStackTrace();
        }
        panel.add(label);

        messageContainer.add(panel);
    }

    public Map<MessageGUI, List<String>> getMsgAccordingToWidth(List<MessageGUI> messages, int maxWidth) {
        Map<MessageGUI, List<String>> mapMsg = new LinkedHashMap<>();
        for (MessageGUI message : messages) {
            JLabel testContentSize = new JLabel(message.getContent());
            if (testContentSize.getPreferredSize().width > maxWidth) {
                mapMsg.put(message, transformMsgToList(message, testContentSize, maxWidth));
            } else {
                mapMsg.put(message, List.of(message.getContent()));
            }
        }
        return mapMsg;
    }

    public List<String> transformMsgToList(MessageGUI message, JLabel testContentSize, int maxWidth) {
        List<String> msgList = new ArrayList<>();
        String msg = message.getContent();
        int nbChars = msg.length();
        int nbCharsLine = nbChars * maxWidth / testContentSize.getPreferredSize().width;
        while (msg.length() > nbCharsLine) {
            String firstLine = msg.substring(0, nbCharsLine);
            String after = msg.substring(nbCharsLine);
            if (!after.startsWith(" ")) {
                int endFirstLine = firstLine.lastIndexOf(" ");
                firstLine = msg.substring(0, endFirstLine);
                after = msg.substring(endFirstLine).trim();
            } else {
                after = after.trim();
            }
            msg = after;
            msgList.add(firstLine);
        }
        msgList.add(msg);
        return msgList;
    }

    public void addMessage(MessageGUI message) {
        List<String> contentMessage = getMsgAccordingToWidth(List.of(message), CONTAINER_WIDTH - 160).values().stream().findFirst().get();
        int panelHeight = MESSAGE_HEIGHT*(1 + contentMessage.size());

        JPanel messageContainer = new JPanel();
        messageContainer.setBounds(MARGIN, totalHeight, MESSAGE_WIDTH, panelHeight);
        messageContainer.setLayout(null);

        addPartOfMessagePanel(messageContainer, 0, MESSAGE_WIDTH - 130, message.getUserName(), ChatFrame.ASSETS_FOLDER + "fonts/Poppins-Bold.ttf");

        for (int i = 0; i < contentMessage.size(); i++) {
            addPartOfMessagePanel(messageContainer, 25*(i+1), MESSAGE_WIDTH, contentMessage.get(i), ChatFrame.ASSETS_FOLDER + "fonts/Poppins-Regular.ttf");
        }

        JPanel timePanel = new JPanel();
        timePanel.setBounds(MESSAGE_WIDTH - 130, 0, 130, MESSAGE_HEIGHT);
        timePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        timePanel.setBackground(new Color(54, 57, 63));
        JLabel timeLabel = new JLabel(message.getDay() + " - " + message.getTime());
        timeLabel.setForeground(Color.white);
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(ChatFrame.ASSETS_FOLDER + "fonts/Poppins-Regular.ttf")).deriveFont(Font.PLAIN, 14);
            timeLabel.setFont(font);
        } catch (Exception e) {
            e.printStackTrace();
        }
        timeLabel.setForeground(new Color(94, 95, 100));
        timePanel.add(timeLabel);
        messageContainer.add(timePanel);

        this.add(messageContainer);

        totalHeight += panelHeight + GAP;
        setBounds(0, Math.min(100, CONTAINER_HEIGHT - totalHeight), CONTAINER_WIDTH, CONTAINER_HEIGHT);
        setPreferredSize(new Dimension(CONTAINER_WIDTH, Math.max(CONTAINER_HEIGHT, totalHeight)));

        revalidate();
        Rectangle rect = new Rectangle(0,getPreferredSize().height,10,10);
        scrollRectToVisible(rect);
    }

}


