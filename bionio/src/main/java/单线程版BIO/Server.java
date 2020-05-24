package 单线程版BIO;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) {
        byte[] buffer = new byte[1024];
        try {
            ServerSocket serverSocket=new ServerSocket(12345);
            while(true) {
                Socket socket = serverSocket.accept();//我自己手动模拟了一下client2,3同时访问服务器，发现只能处理一个，因为现在还是单线程，没办法同时处理两个
                //对于多个客户端，都会为其创建连接，但因为是单线程，所以只接受其中一个线程来处理请求，其他的创建了连接，但是也不会处理的
                socket.getInputStream().read(buffer);
                String content = new String(buffer);
                System.out.println("接受到的内容是：" + content);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
