package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.IOException;

public class ConnectionFrame extends JFrame implements ActionListener {
    private final int BUTTON_HEIGHT = 40;
    private final int GAP = 60;
    private final int WIDTH;
    private final int HEIGHT;
    private JPanel initContainer;
    private JButton connect;
    private JButton newUser;
    private JButton goToChat;
    private JButton goBack;
    private boolean isNewUser;
    private Client client;
    private JTextField pseudo;
    private JLabel pseudoLabel;
    private JPanel askPseudo;

    public ConnectionFrame(Client client, int width, int height) {
        this.client = client;
        WIDTH = width;
        HEIGHT = height;
    }

    public void initChoice() {
        setTitle("Connexion");
        setLayout(null);
        setBounds(200, 100, WIDTH, HEIGHT);
        setResizable(false);
        setVisible(true);

        initContainer = new JPanel();
        initContainer.setBackground(new Color(47, 49, 54));
        setContentPane(initContainer);
        initContainer.setLayout(null);

        connect = new JButton("Connexion");
        connect.setBounds(WIDTH/4, GAP, WIDTH/2, BUTTON_HEIGHT);
        connect.setBackground(new Color(54, 57, 63));
        connect.setForeground(Color.white);
        connect.setBorder(null);
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(ChatFrame.ASSETS_FOLDER + "fonts/Poppins-Regular.ttf")).deriveFont(Font.PLAIN, 14);
            connect.setFont(font);
        } catch (Exception e) {
            e.printStackTrace();
        }
        connect.addActionListener(this);
        initContainer.add(connect);

        newUser = new JButton("Nouvel utilisateur");
        newUser.setBounds(WIDTH/4, GAP + BUTTON_HEIGHT + GAP/2, WIDTH/2, BUTTON_HEIGHT);
        newUser.setBackground(new Color(54, 57, 63));
        newUser.setForeground(Color.white);
        newUser.setBorder(null);
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(ChatFrame.ASSETS_FOLDER + "fonts/Poppins-Regular.ttf")).deriveFont(Font.PLAIN, 14);
            newUser.setFont(font);
        } catch (Exception e) {
            e.printStackTrace();
        }
        newUser.addActionListener(this);
        initContainer.add(newUser);
        validate();
    }

    public void askPseudo() {
        askPseudo = new JPanel();
        askPseudo.setBackground(new Color(47, 49, 54));
        setContentPane(askPseudo);
        askPseudo.setLayout(null);
        
        goBack = new JButton("Retour");
        goBack.setBounds(10, 15, 70, 30);
        goBack.setBackground(new Color(29, 29, 30));
        goBack.setForeground(Color.white);
        goBack.setBorder(null);
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(ChatFrame.ASSETS_FOLDER + "fonts/Poppins-Regular.ttf")).deriveFont(Font.PLAIN, 14);
            goBack.setFont(font);
        } catch (Exception e) {
            e.printStackTrace();
        }
        goBack.addActionListener(this);
        askPseudo.add(goBack);

        String textLabel = "Quel est votre pseudo ?";
        if (isNewUser) textLabel = "Choisir un pseudo :";

        pseudoLabel = new JLabel(textLabel);
        int labelWidth = pseudoLabel.getPreferredSize().width + 40;
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(ChatFrame.ASSETS_FOLDER + "fonts/Poppins-Regular.ttf")).deriveFont(Font.PLAIN, 14);
            pseudoLabel.setFont(font);
        } catch (Exception e) {
            e.printStackTrace();
        }
        pseudoLabel.setBounds((WIDTH - labelWidth)/2, GAP/2, labelWidth, BUTTON_HEIGHT);
        pseudoLabel.setForeground(Color.white);
        askPseudo.add(pseudoLabel);

        pseudo = new JTextField();
        pseudo.setBounds(WIDTH/4, GAP/2 + BUTTON_HEIGHT + GAP/2, WIDTH/2, BUTTON_HEIGHT);
        pseudo.setBackground(new Color(47, 49, 54));
        pseudo.setForeground(Color.white);
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(ChatFrame.ASSETS_FOLDER + "fonts/Poppins-Regular.ttf")).deriveFont(Font.PLAIN, 14);
            pseudo.setFont(font);
        } catch (Exception e) {
            e.printStackTrace();
        }
        askPseudo.add(pseudo);

        goToChat = new JButton("Continuer");
        goToChat.setBounds(WIDTH/4, GAP/2 + BUTTON_HEIGHT + GAP/2 + BUTTON_HEIGHT + GAP/2, WIDTH/2, BUTTON_HEIGHT);
        goToChat.setBackground(new Color(54, 57, 63));
        goToChat.setForeground(Color.white);
        goToChat.setBorder(null);
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(ChatFrame.ASSETS_FOLDER + "fonts/Poppins-Regular.ttf")).deriveFont(Font.PLAIN, 14);
            goToChat.setFont(font);
        } catch (Exception e) {
            e.printStackTrace();
        }
        goToChat.addActionListener(this);
        askPseudo.add(goToChat);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == connect) {
            initContainer.setVisible(false);
            isNewUser = false;
            askPseudo();
        } else if (e.getSource() == newUser) {
            initContainer.setVisible(false);
            isNewUser = true;
            askPseudo();
        } else if (e.getSource() == goBack) {
            ConnectionFrame connectionFrame = new ConnectionFrame(client, WIDTH, HEIGHT);
            connectionFrame.initChoice();
            dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        } else if (e.getSource() == goToChat) {
            try {
                boolean exists = client.checkPseudo(pseudo.getText());
                if ((exists && !isNewUser) || (!exists && isNewUser)) {
                    client.setPseudoCurrentClient(pseudo.getText());
                    goToChat.removeActionListener(this);
                    dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
                    client.sendStartSignal(isNewUser);
                } else if (exists) {
                    pseudoLabel.setText("Ce pseudo est déjà pris. Choisissez un autre pseudo :");
                    int labelWidth = pseudoLabel.getPreferredSize().width;
                    pseudoLabel.setBounds((WIDTH - labelWidth)/2, GAP/2, labelWidth, BUTTON_HEIGHT);
                    revalidate();
                } else {
                    pseudoLabel.setText("Pseudo incorrect. Réessayez :");
                    int labelWidth = pseudoLabel.getPreferredSize().width;
                    pseudoLabel.setBounds((WIDTH - labelWidth)/2, GAP/2, labelWidth, BUTTON_HEIGHT);
                    revalidate();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
