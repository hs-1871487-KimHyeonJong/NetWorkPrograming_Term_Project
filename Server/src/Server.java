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
            while(true){
                Object obcm = null;
                String msg = null;

                if(socket == null)
                    break;
                try{
                    obcm = ois.readObject();
                }  catch (ClassNotFoundException | IOException e) {
                    e.printStackTrace();
                    return;
                }

                if(obcm == null)
                    break;
                if(obcm instanceof )
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

                }
            }
        }

    }
    public void AppendText(String str){
        textArea.append(str + "\n");
        textArea.setCaretPosition(textArea.getText().length());//스크롤을 맨 아래로 내린다.
    }

}