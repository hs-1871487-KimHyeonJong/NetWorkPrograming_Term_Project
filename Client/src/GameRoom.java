import javax.swing.*;
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

public class GameRoom extends JFrame {
    private JPanel contentpane;
    private JTextField textField;
    private JTextPane textArea;
    private JButton btn_ready,btn_exit;

    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    private int ready_status = 0;

    public GameRoom(Socket socket, String username){
        setResizable(false);
        setLocationRelativeTo(null); //창 가운데
        setBounds(10,10,1000,1000);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        contentpane = new JPanel();
        setContentPane(contentpane);
        contentpane.setLayout(null);

        textArea = new JTextPane();

        textField = new JTextField();

        //준비완료
        Ready_Action ready = new Ready_Action();
        btn_ready = new JButton("준비");
        btn_ready.setFont(new Font("굴림",Font.PLAIN,14));
        btn_ready.addActionListener(ready);

        //종료
        btn_exit = new JButton();
        btn_exit.setFont(new Font("굴림", Font.PLAIN,14));

        try{
            oos = new ObjectOutputStream(socket.getOutputStream());
            oos.flush();
            ois = new ObjectInputStream(socket.getInputStream());


        } catch(NumberFormatException | IOException e){
            e.printStackTrace();
            AppendText("Connect error");
        }



        setVisible(true);
    }

    class ListenNetwork extends Thread{
        public void run(){
            while(true){
                try{
                    Object obcm = null;
                    String msg = null;
                    ChatMsg cm;
                    try{
                        obcm = ois.readObject(); //데이터 read
                    }catch(ClassNotFoundException e){
                        e.printStackTrace();
                        break;
                    }

                    if(obcm == null){
                        break; //데이터 X -> 중지
                    }

                }catch(Exception e){
                    e.printStackTrace();
                    break;
                }
            }
        }
    }

    public void AppendText(String msg){
        msg = msg.trim();
        StyledDocument doc = textArea.getStyledDocument();
        SimpleAttributeSet left = new SimpleAttributeSet();
        StyleConstants.setAlignment(left,StyleConstants.ALIGN_LEFT);
        StyleConstants.setForeground(left,Color.BLACK);
        doc.setParagraphAttributes(doc.getLength(),1,left,false);
        try{
            doc.insertString(doc.getLength(),msg+"\n",left);
        } catch (BadLocationException e){
            e.printStackTrace();
        }
        int len = textArea.getDocument().getLength();
        textArea.setCaretPosition(len);
    }

    public void AppendTextR(String msg){
        msg = msg.trim(); // 앞뒤 blank와 \n을 제거한다.
        StyledDocument doc = textArea.getStyledDocument();
        SimpleAttributeSet right = new SimpleAttributeSet();
        StyleConstants.setAlignment(right, StyleConstants.ALIGN_RIGHT);
        StyleConstants.setForeground(right, Color.BLUE);
        doc.setParagraphAttributes(doc.getLength(), 1, right, false);
        try{
            doc.insertString(doc.getLength(),msg+"\n",right);
        } catch(BadLocationException e){
            e.printStackTrace();
        }
        int len = textArea.getDocument().getLength();
        textArea.setCaretPosition(len);
    }

    class Ready_Action implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            if(ready_status == 0){
                btn_ready.setText("준비 완료");
                ready_status = 1;
            }else if(ready_status == 1){
                btn_ready.setText("준비");
                ready_status = 0;
            }
        }
    }
}
