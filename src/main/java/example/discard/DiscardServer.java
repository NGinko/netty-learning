package example.discard;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @author NGinko
 * @date 2022-04-04 17:34
 */
public class DiscardServer {

    private int port;

    public DiscardServer(int port) {
        this.port = port;
    }

    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(); //接收传入的连接
        EventLoopGroup workerGroup = new NioEventLoopGroup();//负责处理bossGroup接收到的连接的传输，然后注册到worker上
        /**
         * 其中有多少的线程被使用，或者是有多少连接的通道 都取决于EventLoopGroup的执行，都可以通过构造函数配置
         */
        try {
            //一个帮助类，用来启动server。也可以直接使用channel设置server，不过没必要比较麻烦
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)//这里用NioServerSocketChannel来实例化一个channel，接收传入的连接
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        /**
                         * 这里指定的handle始终由新接收的Channel评估,ChannelInitializer用作帮用户配置新的channel。
                         * 你可能希望通过添加一些处理程序比如DiscardServerHandler，来配置channelPipeline中新的channel。
                         * 随着应用程序变得复杂，最终可能需要向pipeline中添加更多的handlers,最终将匿名类提取到一个顶级类当中
                         */
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new DiscardServerHandler());
                        }
                    })
                    /**
                     * 针对channel的实现可以设置特定的实现参数。
                     * 当前是一个TCP/IP server，所以可能选择socket选项比如tcpNoDelay，keepAlive。
                     * 可以参考channelOption的API文档和具体ChannelConfig实现
                     */
                    .option(ChannelOption.SO_BACKLOG, 128)
                    /**
                     * option用于配置 NioServerSocketChannel的传入连接。
                     * childOption配置父级serverChannel接收的channels，这里是NioSocketChannel
                     */
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture f = b.bind(port).sync();

            //等待知道server关闭，这个例子中不会关闭，但是也可以这样优雅的关闭
            f.channel().closeFuture().sync();

        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }


    }

    public static void main(String[] args) throws Exception {
        int port = 8080;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        new DiscardServer(port).run();
        System.out.println("Server start");
    }


}
