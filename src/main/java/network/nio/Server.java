package network.nio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

import static utils.ByteBufferUtil.debugRead;

/**
 * @author NGinko
 * @date 2022-05-02 12:25
 */
@Slf4j
public class Server {

    public static void main(String[] args) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false); //设置为非阻塞模式,非阻塞下，没有可读数据时线程依然在循环，会浪费cpu资源
        ssc.bind(new InetSocketAddress(8080));
        ArrayList<SocketChannel> channels = new ArrayList<>();
        while (true) {
            log.debug("connecting...");
            //与client建立连接，socketChannel维护客户端和服务端之间的通信
            SocketChannel accept = ssc.accept(); //启用非阻塞模式后，如果没有成功建立连接，accept是null
            if (accept != null) {
                log.debug("connected... {}", accept);
                ssc.configureBlocking(false);//与客户端维护的也是需要设置为非阻塞模式
                channels.add(accept);
            }

            for (SocketChannel channel : channels) {
                log.debug("before read ... {}", channel);
                int read = channel.read(buffer);//因为设置成了非阻塞，所以线程会继续，需要判断读取的数据。实际复制数据到缓冲区时，依旧时阻塞的的
                if (read > 0) {
                    buffer.flip(); //change to read model
                    debugRead(buffer);
                    buffer.clear();
                    log.debug("after read...{}", channel);
                }
            }
        }
    }
}
