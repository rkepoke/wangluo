package 模拟NIO的解决方案;

import java.nio.IntBuffer;

public class 学习Buffer操作 {
    public static void main(String[] args) {
        //分配int型缓冲区，参数为缓冲区的容量
        //新缓冲区的当前位置是0，界限是容量，
        IntBuffer buffer=IntBuffer.allocate(8);
    }
}
