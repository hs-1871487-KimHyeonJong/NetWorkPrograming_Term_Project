import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Lobby extends JFrame {
    private JPanel contentPane;
    private JTextField txtInput;
    private String UserName;
    private JButton btnSend;
    private JButton game_room_1,game_room_2,game_room_3,game_room_4;
    private JButton game_room_5,game_room_6,game_room_7,game_room_8;
    private JLabel lblUserName;
    private JTextPane textArea;
    private Frame frame;
    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    public Lobby(String userName,String ip_addr,String port_no) {


        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(10, 10, 580, 500);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        contentPane.setLayout(null);
        setContentPane(contentPane);

        JScrollPane scrollPane = new JScrollPane(); // 스크롤 기능을 넣는 코드
        scrollPane.setBounds(250, 10, 280, 330);
        contentPane.add(scrollPane);

        textArea = new JTextPane();
        textArea.setEditable(true);
        textArea.setFont(new Font("굴림체",Font.PLAIN,14));
        scrollPane.setViewportView(textArea);

        txtInput = new JTextField();
        txtInput.setBounds(250, 350, 270, 40);
        contentPane.add(txtInput);
        txtInput.setColumns(10);

        btnSend = new JButton("전송");
        btnSend.setFont(new Font("굴림", Font.PLAIN, 14));
        btnSend.setBounds(290, 400, 69, 40);
        contentPane.add(btnSend);

        AppendText("사용자 " + userName + " connecting " + ip_addr + " " + port_no);
        UserName = userName;

        JButton btnexit = new JButton("종 료");
        btnexit.setFont(new Font("굴림",Font.PLAIN,14));
        btnexit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ChatMsg msg = new ChatMsg(UserName, "300", "Bye");
                SendObject(msg);
                System.exit(0);
            }
        });
        btnexit.setBounds(400, 400, 69, 40);
        contentPane.add(btnexit);

        game_room_1 = new JButton("1번방");
        game_room_1.setFont(new Font("굴림",Font.PLAIN,14));
        game_room_1.setBounds(10,20,100,80);
        contentPane.add(game_room_1);

        game_room_2 = new JButton("2번방");
        game_room_2.setFont(new Font("굴림",Font.PLAIN,14));
        game_room_2.setBounds(120,20,100,80);
        contentPane.add(game_room_2);

        game_room_3 = new JButton("3번방");
        game_room_3.setFont(new Font("굴림",Font.PLAIN,14));
        game_room_3.setBounds(10,120,100,80);
        contentPane.add(game_room_3);

        game_room_4 = new JButton("4번방");
        game_room_4.setFont(new Font("굴림",Font.PLAIN,14));
        game_room_4.setBounds(120,120,100,80);
        contentPane.add(game_room_4);

        game_room_5 = new JButton("5번방");
        game_room_5.setFont(new Font("굴림",Font.PLAIN,14));
        game_room_5.setBounds(10,220,100,80);
        contentPane.add(game_room_5);

        game_room_6 = new JButton("6번방");
        game_room_6.setFont(new Font("굴림",Font.PLAIN,14));
        game_room_6.setBounds(120,220,100,80);
        contentPane.add(game_room_6);

        game_room_7 = new JButton("7번방");
        game_room_7.setFont(new Font("굴림",Font.PLAIN,14));
        game_room_7.setBounds(10,320,100,80);
        contentPane.add(game_room_7);

        game_room_8 = new JButton("8번방");
        game_room_8.setFont(new Font("굴림",Font.PLAIN,14));
        game_room_8.setBounds(120,320,100,80);
        contentPane.add(game_room_8);

        GameRoomAction roomAction = new GameRoomAction();
        game_room_1.addActionListener(roomAction);
        game_room_2.addActionListener(roomAction);
        game_room_3.addActionListener(roomAction);
        game_room_4.addActionListener(roomAction);

        try {
            socket = new Socket(ip_addr, Integer.parseInt(port_no));
            oos = new ObjectOutputStream(socket.getOutputStream());
            oos.flush();
            ois = new ObjectInputStream(socket.getInputStream());

            ListenNetwork net = new ListenNetwork();
            net.start();
            TextSendAction action = new TextSendAction();
            btnSend.addActionListener(action);
            txtInput.addActionListener(action);
            txtInput.requestFocus();
        } catch (NumberFormatException | IOException e) {
            e.printStackTrace();
            AppendText("connet error");
        }


        setVisible(true);
    }

        class ListenNetwork extends Thread{
            @Override
            public void run() {
                while(true){
                    try{
                        Object obcm = null;
                        String msg = null;
                        ChatMsg cm;
                        try{
                            obcm = ois.readObject();
                        }catch(ClassNotFoundException e){
                            e.printStackTrace();
                            break;
                        }

                        if(obcm == null){
                            break;
                        }
                        if(obcm instanceof  ChatMsg){
                            cm = (ChatMsg) obcm;
                            msg = String.format("[%s]\n%s",cm.UserName,cm.data);
                        }else
                            continue;
                        if(cm.code.matches("200")){
                            if(cm.UserName.equals(UserName)){
                                AppendTextR(msg);
                            }else
                                AppendText(msg);
                        }

                    }catch(Exception e){
                        e.printStackTrace();
                        AppendText("ois.readObject() error");
                        try{
                            ois.close();
                            oos.close();
                            socket.close();
                            break;
                        } catch (Exception ex) {
                            break;
                        }
                    }
                }
            }
        }

    public void AppendText(String msg){
        msg = msg.trim();
        int len = textArea.getDocument().getLength();

        StyledDocument doc = textArea.getStyledDocument();
        SimpleAttributeSet left = new SimpleAttributeSet();
        StyleConstants.setAlignment(left,StyleConstants.ALIGN_LEFT);
        StyleConstants.setForeground(left,Color.BLACK);
        doc.setParagraphAttributes(doc.getLength(), 1, left, false);
        try {
            doc.insertString(doc.getLength(), msg+"\n", left );
        } catch (BadLocationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        len = textArea.getDocument().getLength();
        textArea.setCaretPosition(len);
    }

    public void AppendTextR(String msg) {
        msg = msg.trim(); // 앞뒤 blank와 \n을 제거한다.
        StyledDocument doc = textArea.getStyledDocument();
        SimpleAttributeSet right = new SimpleAttributeSet();
        StyleConstants.setAlignment(right, StyleConstants.ALIGN_RIGHT);
        StyleConstants.setForeground(right, Color.BLUE);
        doc.setParagraphAttributes(doc.getLength(), 1, right, false);
        try {
            doc.insertString(doc.getLength(),msg+"\n", right );
        } catch (BadLocationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        int len = textArea.getDocument().getLength();
        textArea.setCaretPosition(len);

    }

    public void SendMessage(String msg){
        try{
            ChatMsg obcm = new ChatMsg(UserName,"200",msg);
            oos.writeObject(obcm);
        } catch (IOException e) {
            AppendText("oos.writeObject() error");
            try{
                ois.close();
                oos.close();
                socket.close();
            } catch(IOException e1){
                e1.printStackTrace();
                System.exit(0);
            }
        }
    }

    class GameRoomAction implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            GameRoom room = new GameRoom(socket,UserName);
            setVisible(false);
        }
    }

    class TextSendAction implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == btnSend || e.getSource() == txtInput){
                String msg = null;
                msg = txtInput.getText();
                SendMessage(msg);
                txtInput.setText("");
                txtInput.requestFocus();
                if(msg.contains("/exit"))
                    System.exit(0);
            }
        }
    }

    public void SendObject(Object ob){
        try{
            oos.writeObject(ob);
        }catch(IOException e){
            AppendText("SendObject Error");
        }
    }
}
