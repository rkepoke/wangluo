package 单线程版BIO;

import java.io.IOException;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("127.0.0.1", 12345);
            socket.getOutputStream().write("Hello World!".getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
