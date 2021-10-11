package io.netty.myexample.nioTest;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;


/**
 * nio服务器
 *
 * @author Rock
 */
public class NioServer {

    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        serverSocketChannel.socket().bind(new InetSocketAddress(9000));
        serverSocketChannel.configureBlocking(false);

        Selector selector = Selector.open();

        // 1，configureBlocking 如果不设置非阻塞，register的时候会报异常 java.nio.channels.IllegalBlockingModeException
        // 2，如果一个 Channel 要注册到 Selector 中, 那么这个 Channel 必须是非阻塞的。
        // 因为 Channel 必须要是非阻塞的, 因此 FileChannel 是不能够使用选择器的, 因为 FileChannel 都是阻塞的.
        // 3，一个 Channel 仅仅可以被注册到一个 Selector 一次, 如果将 Channel 注册到 Selector 多次, 那么其实就是相当于更新 SelectionKey 的 interest set
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {

            // 这里会一直阻塞，直到注册在Selector中的Channel发送可读写事件。
            // 当selector()返回时，当前这个线程就可以处理Channel了。
            int selected = selector.select();

            // select() 返回值表示有多少个Channel可读。
            if (selected > 0) {

                // 获取 I/O 操作就绪的 SelectionKey, 通过 SelectionKey 可以知道哪些 Channel 的哪类 I/O 操作已经就绪.
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();
                    // 这里必须删除
                    iterator.remove();

                    if (selectionKey.isAcceptable()) {
                        // 当 OP_ACCEPT 事件到来时, 我们就有从 ServerSocketChannel 中获取一个 SocketChannel,
                        // 代表客户端的连接
                        // 注意, 在 OP_ACCEPT 事件中, 从 key.channel() 返回的 Channel 是 ServerSocketChannel.
                        // 而在 OP_WRITE 和 OP_READ 中, 从 key.channel() 返回的是 SocketChannel.
                        System.err.println("Acceptable");

                        SocketChannel socketChannel = serverSocketChannel.accept();
                        socketChannel.configureBlocking(false);
                        socketChannel.register(selector, SelectionKey.OP_READ);
                    } else if (selectionKey.isReadable()) {
                        System.err.println("Readable");

                        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();

                        ByteBuffer buffer = ByteBuffer.allocate(128);
                        socketChannel.read(buffer);

                        System.out.println("接收来自客户端的数据： " + new String(buffer.array()));

                        // 重新设置关注事件，即可以动态的修改selectionKey的interest set
                        selectionKey.interestOps(SelectionKey.OP_WRITE);
                    } else if (selectionKey.isWritable()) {
                        System.err.println("Readable");

                        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                        String content = "向客户端发送数据： " + System.currentTimeMillis();
                        ByteBuffer buffer = ByteBuffer.wrap(content.getBytes());
                        socketChannel.write(buffer);

                        selectionKey.interestOps(SelectionKey.OP_READ);
                    }
                }
            }
        }
    }
}
