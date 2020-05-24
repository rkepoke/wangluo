package 模拟NIO的解决方案;



import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

public class TomcatServer {
    //分配缓冲区
    static ByteBuffer buffer=ByteBuffer.allocate(1024);
    //list里面也能装连接，学到了   list里装的全是连接了该服务端的socket
    static List<SocketChannel>  channelList=new ArrayList<>();
    public static void main(String[] args) {

        try {
            //创建一个非阻塞的ServerSocket  这样在监听的时候就不会阻塞   解决第一种阻塞
            ServerSocketChannel serverSocket = ServerSocketChannel.open();
            //指定IP+Port  并绑定到serverSoket中
            InetSocketAddress socketAddress = new InetSocketAddress("127.0.0.1", 12345);
            serverSocket.bind(socketAddress);
            //把服务端设置为非阻塞
            serverSocket.configureBlocking(false);//就是之前分析的，把阻塞变为非阻塞，才能用单线程实现并发

            while (true){
                //轮询，看数据有咩有准备好，要是准备好了，就直接处理
                for(SocketChannel socketChannel:channelList){
                    //看往缓冲区里读的数据有咩有，有（行数>0）就处理
                    int read =  socketChannel.read(buffer);//这里是不会阻塞的，因为每个连接都设为非阻塞的了
                    if(read>0){
                        System.out.println("read---------111----"+ read);
                        //转变缓冲区的模式，从read到write
                        buffer.flip();
                        byte[] bytes = new byte[read];
                        buffer.get(bytes);//读缓冲区中额数据到当前连接自己的数组中
                        String content = new String(bytes);
                        System.out.println(content);
                        buffer.flip();//处理完数据在转变回来
                    }
                }

                //判断有没有人来连接我，有，我就创建一个新的socket与之对接
                SocketChannel accept = serverSocket.accept();
                //因为是非阻塞，所以要判断有没有人来连接，如果有，才加入，因为不是时时刻刻都有连接到来的
                if(accept!=null){
                    System.out.println("conn success");
                    accept.configureBlocking(false);//把当前这个连接设置为非阻塞连接，否则在读取数据的时候也会发生阻塞 解决了第二种阻塞
                    channelList.add(accept);
                    System.out.println(channelList.size()+"list-------size");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
