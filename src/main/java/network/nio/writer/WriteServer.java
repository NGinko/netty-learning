package network.nio.writer;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

/**
 * @author NGinko
 * @date 2022-05-22 21:52
 */
@Slf4j
public class WriteServer {

    public static void main(String[] args) throws IOException {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        ssc.bind(new InetSocketAddress(8080));

        Selector selector = Selector.open();
        ssc.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            selector.select();

            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                if (key.isAcceptable()) {
                    SocketChannel sc = ssc.accept();
                    sc.configureBlocking(false);
                    SelectionKey scKey = sc.register(selector, SelectionKey.OP_READ);

                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < 900000000; i++) {
                        sb.append("a");
                    }
                    ByteBuffer encode = Charset.defaultCharset().encode(sb.toString());
                    //write 返回值表示的是实际写入了多少字节
                    int write = sc.write(encode);
                    log.info("写入的字节数：{}", write);
                    //如果还没写完应该继续写 ， 此步骤的目的是为了防止判断过程中无意义的循环，浪费性能
                    if (encode.hasRemaining()) {
                        //在原有的事件基础上，再加上写事件  read 1 | write 4
                        scKey.interestOps(scKey.interestOps() | SelectionKey.OP_WRITE);
                        //没使用完的buffer继续放到附件使用
                        scKey.attach(encode);
                    }
                } else if (key.isWritable()) {
                    ByteBuffer buffer = (ByteBuffer) key.attachment();
                    SocketChannel sc = (SocketChannel) key.channel();
                    int write = sc.write(buffer);
                    log.info("写入的字节数：{}", write);
                    //清理操作，写完之后，需要清除buffer
                    if (!buffer.hasRemaining()) {
                        key.interestOps(key.interestOps() - SelectionKey.OP_WRITE);
                        key.attach(null);
                    }
                }
            }
        }

    }
}
