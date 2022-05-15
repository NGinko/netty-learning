package network.nio;

import lombok.extern.slf4j.Slf4j;
import utils.ByteBufferUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

/**
 * @author NGinko
 * @date 2022-05-15 21:10
 */
@Slf4j
public class SelectorDemo {
    public static void main(String[] args) {
        try (ServerSocketChannel channel = ServerSocketChannel.open()) {
            channel.bind(new InetSocketAddress(8080));

            //创建Selector来管理多个channel
            Selector selector = Selector.open();
            channel.configureBlocking(false); //设置为非阻塞
            log.info("selector start");
            //绑定channel和channel之间的联系，通过selector可以知道具体是哪个channel的事件
            channel.register(selector, SelectionKey.OP_ACCEPT);
            while (true) {
                //如果没有事件发生，select会阻塞线程后面有事件发生才会恢复
                int count = selector.select();
                log.debug("selector count:{}", count);

                //获取所有事件(其中包含了所有可用的事件集合)，每次事件发生后，都会被加入下面的集合当中
                Set<SelectionKey> selectionKeys = selector.selectedKeys();

                //遍历所有事件，事件未处理则不阻塞，需要处理或者取消
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    //判断事件类型
                    if (key.isAcceptable()) {
                        ServerSocketChannel c = (ServerSocketChannel) key.channel();
                        SocketChannel sc = c.accept();
                        sc.configureBlocking(false);
                        sc.register(selector, SelectionKey.OP_READ);
                        log.debug("SocketChannel connect : {}", sc);
                    } else if (key.isReadable()) {
                        try {
                            SocketChannel sc = (SocketChannel) key.channel();
                            ByteBuffer buffer = ByteBuffer.allocate(128);
                            int read = sc.read(buffer);
                            if (read == -1) { //正常的断开read返回-1,
                                key.cancel();//需要取消注册在selector上的channel
                                sc.close();
                            } else {
                                buffer.flip();
                                ByteBufferUtil.debugRead(buffer);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            key.cancel(); //客户端异常断开，需要吧key取消，(从selector的keys集合删除key)
                        }
                    }
                    //处理完毕之后需要将事件移除。如果不移除，会再次从事件集合中读取事件，此时将是非阻塞状态，导致循环。影响后续事件的读取
                    //本质是因为nio底层是水平触发导致的
                    iterator.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
