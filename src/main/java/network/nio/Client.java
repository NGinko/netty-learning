package network.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

/**
 * @author NGinko
 * @date 2022-05-02 12:38
 */
public class Client {

    public static void main(String[] args) throws IOException {
        SocketChannel sc = SocketChannel.open();
        sc.connect(new InetSocketAddress("localhost", 8080));
        System.out.println("waiting ...");
        /**
         * sc.write(Charset.defaultCharset().encode("0123\n456789abcdef"));
         * Charset.defaultCharset().decode()
         */
    }
}
