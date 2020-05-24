# wangluo
nio的逐步学习
1：BIO会有两个阻塞，一个是连接时阻塞，一个是等待数据时会阻塞。而且等待的时候是放弃CPU的。
2：为什么BIO不支持多线程啊：因为当有两个客户端同时连接一个BIOServer时，Server会等待其中一个客户端数据，就处于放弃CPU状态，所以，另一个客户端不能连接，因为main线程阻塞在等待数据哪里了呀
3：解决方法：为每一个客户端开一个线程。有了一个新连接后，就开辟一个新的线程去处理，这样阻塞的就是新的线程，整个main线程是不会阻塞的。

总结：因为BIO会有两个地方阻塞，所以BIO情形下的并发需要多线程去支持。
而多线程的缺点：为每一个请求开一个线程，会造成大量的资源浪费。因为一个连接并不会时时都发请求，做交互。  所以引入NIO

NIO：
NIO的设计初衷：使用单线程就能处理并发
(redis也是单线程处理并发，它的核心思想也是NIO的思路，调用的是epoll函数)
就是要想办法，使阻塞变为非阻塞

为什么sun公司开发一个新的类ServerSocketChannel，里面实现非阻塞，而不是在原来的ServerSocket里面添加一个新的类，因为ServerSocket已经被应用了，改变一个应用了的类会带来很多工程上的麻烦，所以直接新创建了一个类ServerSocketChannel实现非阻塞。你可以把ServerSocketChannel理解为传统的ServerSocket只是改了一个名字。

NIO：就是使两处阻塞变为非阻塞。这就是它的设计初衷
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

以上代码的性能瓶颈在，轮询，比如，现在有10000个连接，但是只有1000个是活跃的，9000个是不活跃的，那么每次轮询就会浪费大蓝的时间

解决：
1：把for循环交给---->os内核kernel去执行
因为java程序是在jvm（估计一分钟的运行时间）上运行的，jvm是在os（在os上只要20秒）上运行的，所以，如果我们直接把代码交给os，就能跑的更快
2：for循环会有很多无意义的循环，还没解决。
Selector就是把轮询交给了os内核去做了。

在linux中通过
man 2 select  就能看到select源码 了

Nio的底层就是：select：
从最开始去理解：ServerSocket不会与客户端去通信，但是当监听到一个连接之后，就会创建一个新的socket的专门与监听到的连接对接。
从java级别到c级别，要结果JNI；
int select(int n,fd_set * readfds,fd_set * writefds,fd_set * exceptfds,struct timeval * timeout);
第一个参数的解释：就是select要轮询多少次，而且轮询的是所有的读写和异常事件
通过Java追踪bind发现，发现最终调用的是bind0方法，而bind0方法就是一个native本地方法，是c语言的，不是存在SDK中的，而是存储在JVM中的
原来Java编译后的很多底层代码实现，都是调用对应的c去实现的。
ServerSocket serverSocket=new ServerSocket(12345);
这行代码的本质就是调用了JNI中的源码。Bind0

Window中通过poll0方法把list给os了 ，为什么该方法可以给呢？

可以把c中的“文件描述符”理解为Java中的“对象”。
因为linux中一切皆文件，你创建的任何对象都会存在fd[1,2,3，...]这个文件数组中，创建一个对象就存进去，创建一个对象就存进去。而文件描述符就是这个数组的下标，所以linux并不是把创建的文件返回给你，而是返回这个文件在fd文件数组里的下标，这个下标可以理解为是一个指针。
Linux中每启动一个进程，就会在fd中创建三个对象，fd[0,1,2]，代表stdio，就是标准输入输出和出错。原来stdio是这个意思。

总之就是，Java的native就是c写的，c又根据不同的系统调自带函数，实现把本该Java实现的代码交给os去实现，提高了速度
在windows平台底层掉的是select
在linux底层调的是epoll
所以同样的代码在linux上和在windows上的性能是不一样的。

就比如synchronized的底层调用的是jvm，jvm的底层又是调用os内核，调用内核就会引起内核态和用户态之间的切换
所以就开发了ReentrntLock从jdk级别解决高并发问题









