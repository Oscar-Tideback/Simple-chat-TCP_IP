import Clients.Client;
import Server.Server;

import javax.swing.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Objects;

public class Chat extends JFrame implements Runnable{
    //lazySodium
    private JPanel contentPane;
    private JButton btnSend;
    private JButton btnConnect;
    private JButton btnDisconnect;
    private JTextArea txtChatArea;
    private JTextArea txtMessage;
    private JTextArea txtInputName;
    private JTextArea txtName;
    private JTextField txtFieldIP;
    private JTextField txtFieldPort;
    private JCheckBox isServer;
    private final int maxNameChar =  10;
    private boolean firsttime = true;
    private ObjectInputStream reader;
    private ObjectOutputStream writer;
    private Client client;
    private static Server server;

    public String getIpAddress() { return txtFieldIP.getText(); }
    public int getPort() { return Integer.parseInt(txtFieldPort.getText()); }

    public static void main(String[] args) throws IOException {
        Chat chat = new Chat();
        Thread thread = new Thread(chat);
        thread.start();
    }
    @Override
    public void run() {
        try {
            while (true){
                client = (Client) reader.readObject();
                txtChatArea.append(client.getAlias() + " says: " + client.getMessage() + "\r\n");
                if(!checkIfOnline(client.getAlias()))                                               // BUG! This check should be on serverside
                    txtName.append(client.getAlias()  + "\n");
            }
        }  catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }



    private Chat() throws IOException {
        setContentPane(contentPane);
        pack();
        setTitle("Chat 0.3 TCP/IP version");
        setVisible(true);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);


        Socket connectionSocket = new Socket(getIpAddress(), getPort());
        writer = new ObjectOutputStream(connectionSocket.getOutputStream());
        reader = new ObjectInputStream(connectionSocket.getInputStream());
        client = new Client();

        btnDisconnect.setEnabled(false);
        btnSend.setEnabled(false);
        txtMessage.setEditable(false);
        isServer.setEnabled(false);

        btnConnect.addActionListener(e -> {
            if(checkIfOnline(txtInputName.getText())){
                txtChatArea.append("Try again, alias already online. \r\n");
            }
            else if(Objects.equals(txtInputName.getText(), "Enter your name") || !checkIfChar()) {
                txtChatArea.append("Try again with a name.\r\n");
            }
            else{
                txtName.append(txtInputName.getText() + "\n");
                btnSend.setEnabled(true);
                txtMessage.setEditable(true);
                btnConnect.setEnabled(false);
                btnDisconnect.setText("Off kommer senare");
                btnDisconnect.setEnabled(false);
                txtInputName.setEditable(false);
                txtInputName.setEnabled(false);
                txtFieldIP.setEnabled(false);
                txtFieldPort.setEnabled(false);
                try {
                    sendMessage(client);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        btnSend.addActionListener(e -> {
            try {
                if (checkForMessages()){
                    sendMessage(client);
                    txtMessage.setEditable(true);
                }
            } catch (IOException ex) {
                System.out.println(txtMessage);
                ex.printStackTrace();
            }
        });
        txtInputName.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
                txtInputName.setText("");
            }
        });

        txtInputName.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if(txtInputName.getText().length() > maxNameChar + 1){
                    e.consume();
                    String shorterName = txtInputName.getText().substring(0, maxNameChar);
                    txtInputName.setText(shorterName);
                }
                else if (txtInputName.getText().length() > maxNameChar){
                    txtMessage.setText("Please only 10 letter name");
                    e.consume();
                }
                super.keyTyped(e);
            }
        });

        txtMessage.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                try {
                    if(e.getKeyCode() == KeyEvent.VK_ENTER && (e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0){
                        txtMessage.append("\n");
                        e.consume();
                    }
                    else if (e.getKeyCode() == KeyEvent.VK_ENTER && checkForMessages()) {
                        sendMessage(client);
                        e.consume();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private boolean checkForMessages() throws IOException {
        if(        Objects.equals(txtMessage.getText(), "Please enter something to say. \nUse SHIFT-ENTER for new row.")
                || Objects.equals(txtMessage.getText(), "Please enter something to say. \nUse SHIFT-ENTER for new row.\n")
                || Objects.equals(txtMessage.getText(), "Try again with a name.")
                || Objects.equals(txtMessage.getText(), "\n")
                || Objects.equals(txtMessage.getText(), "")){
            txtMessage.setText("Please enter something to say. \nUse SHIFT-ENTER for new row.");
            txtMessage.setEditable(true);
            return false;
        }
        return true;
    }
    private boolean checkIfChar(){
        for (char letter : txtInputName.getText().toCharArray()) {
            if(Character.isLetterOrDigit(letter)){
                return true;
            }
        }
        return false;
    }
    private boolean checkIfOnline(String alias){
        for (String line : txtName.getText().split("\\n")) {
            if(Objects.equals(alias, line))
                return true;
        }
        return false;
    }
    private void sendMessage(Client client) throws IOException {
        if(firsttime) {
            client.setMessage("hi!");
            firsttime = false;
        }
        else
            client.setMessage(txtMessage.getText());
        client.setAlias(txtInputName.getText());
        writer.writeObject(client);
        writer.reset();
        txtMessage.setText("");
    }
    public void someFunction(String data) {
        //smallOperation();
        new Thread(() -> {
            // doSomething long running
        }).start();
    }
}