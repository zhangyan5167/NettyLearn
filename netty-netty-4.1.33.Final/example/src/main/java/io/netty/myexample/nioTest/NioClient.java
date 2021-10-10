package io.netty.myexample.nioTest;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * nio客户端
 *
 * @author Rock
 * @date 2021/10/10
 */
public class NioClient {
    public static void main(String[] args) throws IOException {
        SocketChannel socketChannel = SocketChannel.open();

        socketChannel.configureBlocking(false);
        Selector selector = Selector.open();
        socketChannel.register(selector, SelectionKey.OP_CONNECT);

        socketChannel.connect(new InetSocketAddress("127.0.0.1", 9000));

        while (true) {
            int select = selector.select();

            if (select > 0) {

                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

                while (iterator.hasNext()) {

                    SelectionKey selectionKey = iterator.next();

                    if (selectionKey.isConnectable()) {
                        System.err.println("Connectable");

                        SocketChannel clientChannel = (SocketChannel) selectionKey.channel();
                        clientChannel.finishConnect();

                        selectionKey.interestOps(SelectionKey.OP_WRITE);
                    } else if (selectionKey.isReadable()) {
                        System.err.println("Readable");

                        SocketChannel channel = (SocketChannel) selectionKey.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(128);
                        channel.read(buffer);
                        selectionKey.interestOps(SelectionKey.OP_WRITE);

                        System.out.println("收到服务器数据 " + new String(buffer.array()));
                    } else if (selectionKey.isWritable()) {
                        SocketChannel clientChannel1 = (SocketChannel) selectionKey.channel();

                        String str = "Hello Server";
                        ByteBuffer buffer = ByteBuffer.wrap(str.getBytes());
                        clientChannel1.write(buffer);
                        selectionKey.interestOps(SelectionKey.OP_READ);

                        System.out.println("向服务区发送数据 " + new String(buffer.array()));
                    }

                    iterator.remove();
                }
            }
        }
    }
}
