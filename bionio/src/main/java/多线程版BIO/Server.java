package 多线程版BIO;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(12345);
            while (true){
                final Socket socket = serverSocket.accept();
                new Thread(new Runnable() {//此时等待数据时，阻塞的就是这个线程，而不是main线程在阻塞了
                    public void run() {
                        try {
                            byte[] buffer=new byte[1024];
                            socket.getInputStream().read(buffer);
                            String content=new String(buffer);
                            System.out.println(content);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();//这次不一样 了，是来来一个连接，就创建一个新的线程去处理了，等待读写请求
                //这次就实现了多线程下的BIO对请求的处理
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
