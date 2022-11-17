import javax.sound.sampled.Port;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class Server extends JFrame {
    private ServerSocket socket;
    private Socket client_socket;
    private Vector UserVec = new Vector();
    private JTextField PortNumber;
    private JTextArea textArea;
    private JPanel contentPane;
    private static final int BUF_LEN = 128;

    public static void main(String[] args) {

        EventQueue.invokeLater(() -> {
            Server server = new Server();
            server.setVisible(true);
        });
    }


    public Server() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 338, 440);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        JScrollPane scrollPane = new JScrollPane(); // MSG 표기 창
        scrollPane.setBounds(12, 10, 300, 298);
        contentPane.add(scrollPane);

        textArea = new JTextArea();
        textArea.setEditable(false);
        scrollPane.setViewportView(textArea);

        JLabel lblNewLabel = new JLabel("Port Number");
        lblNewLabel.setBounds(13, 318, 87, 26);
        contentPane.add(lblNewLabel);

        PortNumber = new JTextField();
        PortNumber.setHorizontalAlignment(SwingConstants.CENTER);
        PortNumber.setText("30000");
        PortNumber.setBounds(112, 318, 199, 26);
        contentPane.add(PortNumber);
        PortNumber.setColumns(10);

        JButton ServerStart = new JButton("Server Start");
        ServerStart.setBounds(12, 356, 300, 35);
        contentPane.add(ServerStart);
        //서버 연결 버튼
        ServerStart.addActionListener(e -> {
            try {
                socket = new ServerSocket(Integer.parseInt(PortNumber.getText()));//PortNumber 텍스트 Socket 담기
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            ServerStart.setText("Chat Server Running..");
            ServerStart.setEnabled(false);
            PortNumber.setEnabled(false);
            Accept accept = new Accept();
            accept.start();

        });
    }

    class UserActivity extends Thread {
        private ObjectInputStream ois;
        private ObjectOutputStream oos;
        private DataInputStream dis;
        private DataOutputStream dos;
        private Socket client_socket;
        private Vector user_vc;
        public String UserName = "";

        //생성자
        public UserActivity(Socket client_socket) {
            this.client_socket = client_socket; //client_socket 초기화
            this.user_vc = UserVec;
            try {
                oos = new ObjectOutputStream(client_socket.getOutputStream());
                oos.flush();
                ois = new ObjectInputStream(client_socket.getInputStream());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        @Override
        public void run() {

            while(true) {
                try {
                    Object obcm = null;
                    String msg = null;
                    ChatMsg cm = null;
                    if (socket == null)
                        break;
                    try {
                        obcm = ois.readObject(); //Input 올때까지 대기
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                        return;
                    }
                    if (obcm == null)
                        break;
                    if (obcm instanceof ChatMsg) {
                        cm = (ChatMsg) obcm;
                        AppendObject(cm);
                    } else
                        continue;

                    if (cm.code.matches("100")) {
                        UserName = cm.UserName;
                        Login();
                    } else if (cm.code.matches("200")) {
                        msg = String.format("[%s] %s", cm.UserName, cm.data);
                        AppendText(msg); //서버 TextField에 표기
                        WriteAllObject(cm);
                    } else if (cm.code.matches("300")) {
                        UserName = cm.UserName;
                        Logout();
                        break;
                    }

                } catch (IOException e) {
                    AppendText("ois.readObject() error");
                    try {
//						dos.close();
//						dis.close();
                        ois.close();
                        oos.close();
                        client_socket.close();
                        Logout(); // 에러가난 현재 객체를 벡터에서 지운다
                        break;
                    } catch (IOException ee) {
                        break;
                    } // catch문 끝
                }
            }
        }

        public void Login(){
            AppendText(UserName + "님이 입장 하였습니다.");
        }
        public void Logout(){
            String msg = "[" + UserName + "]님이 퇴장 하였습니다.\n";
            UserVec.removeElement(this);
            WriteAll(msg); // 나를 제외한 다른 User들에게 전송
            AppendText("사용자 " + "[" + UserName + "] 퇴장. 현재 참가자 수 " + UserVec.size());
        }

        public void WriteAll(String msg){
            for(int i=0;i<user_vc.size();i++){
                UserActivity user = (UserActivity) user_vc.elementAt(i);
                user.WriteOne(msg);
            }
        }

        public void WriteOne(String msg){
            try {
                ChatMsg obcm = new ChatMsg("SERVER", "200", msg);
                oos.writeObject(obcm);

            } catch (IOException e) {
                AppendText("dos.writeObject() error");
                try {
//					dos.close();
//					dis.close();
                    ois.close();
                    oos.close();
                    client_socket.close();
                    client_socket = null;
                    ois = null;
                    oos = null;
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                Logout();
            }
        }

        public void WriteAllObject(Object ob){ //전체 메시지 전송
            for(int i=0;i<user_vc.size();i++){
                UserActivity user = (UserActivity) user_vc.elementAt(i);
                user.WriteOneObject(ob);
            }
        }

        public void WriteOneObject(Object ob){
            try{
                oos.writeObject(ob);
            } catch (IOException e) {
                AppendText("oos.writeObject(ob) error");
                try {
                    ois.close();
                    oos.close();
                    client_socket.close();
                    client_socket = null;
                    ois = null;
                    oos = null;
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                Logout();
            }
        }
    }

    class Accept extends Thread{
        @Override
        public void run() {
            while(true){
                try {
                    AppendText("사용자를 기다리고 있습니다.");
                    client_socket = socket.accept();
                    AppendText("새로운 참가자 : " + client_socket);
                    UserActivity user = new UserActivity(client_socket);
                    UserVec.add(user); // 연결된 User 배열
                    user.start();
                    AppendText("현재 참가자 수 " + UserVec.size());
                } catch (IOException e) {
                    AppendText("Accept() error");
                }
            }
        }

    }
    public void AppendText(String str){
        textArea.append(str + "\n");
        textArea.setCaretPosition(textArea.getText().length());//스크롤을 맨 아래로 내린다.
    }

    public void AppendObject(ChatMsg msg) {
        textArea.append("code = " + msg.code + "\n");
        textArea.append("id = " + msg.UserName + "\n");
        textArea.append("data = " + msg.data + "\n");
        textArea.setCaretPosition(textArea.getText().length());
    }

}