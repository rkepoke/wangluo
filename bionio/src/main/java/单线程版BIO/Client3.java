package 单线程版BIO;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client3 {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("127.0.0.1", 12345);
            String message="";
            Scanner sc=new Scanner(System.in);
            message= sc.next();
            socket.getOutputStream().write(message.getBytes());
            socket.close();
            sc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
