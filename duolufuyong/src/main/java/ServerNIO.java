/*
ServerSocket(int port)
创建绑定到指定端口的服务器套接字。
int getLocalPort()
返回此套接字正在侦听的端口号。


static InetAddress getLocalHost()
返回本地主机的地址。

PrintWriter(OutputStream out)
从现有的OutputStream创建一个新的PrintWriter，而不需要自动线路刷新。

BufferReader
Stream<String> lines()
返回一个 Stream ，其元素是从这个 BufferedReader读取的行。

Stream
void forEach(Consumer<? super T> action)
对此流的每个元素执行操作。
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class ServerNIO extends Thread{
    private ServerSocket serverSocket;
    public int getPort() {//这样书写就是动态的，而不是硬编码了   ===返回此套接字正在侦听的端口号。
        return serverSocket.getLocalPort();
    }

    public void run() {
        try {
            serverSocket = new ServerSocket(0);
            while (true) {
                Socket socket = serverSocket.accept();
                RequestHandler requestHandler = new RequestHandler(socket);
                requestHandler.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        ServerNIO serverNIO = new ServerNIO();
        serverNIO.start();
        for (int i = 0; i < 10; i++) {//多个客户端去访问请求
            try(Socket client = new Socket(InetAddress.getLocalHost(), serverNIO.getPort())){
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream()));//建立一个输入流
                bufferedReader.lines().forEach(s -> System.out.println(s));//返回一个 Stream ，其元素是从这个 BufferedReader读取的行。
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

//简化实现
class RequestHandler extends Thread {//这就是开启一个线程去处理一个请求     这个就是长连接，因为执行玩run并没有关闭socket连接
    private Socket socket;

    RequestHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (PrintWriter out = new PrintWriter(socket.getOutputStream())) {//从现有的OutputStream创建一个新的PrintWriter，而不需要自动线路刷新。
            out.print("Hello world");//输出流，向socket里面输出Hello world
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

