package example.time;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * 有了TimeServer 还需要一个客户端解析时间
 *
 * @author NGinko
 * @date 2022-04-05 17:41
 */
public class TimeClient {

    /**
     * 在netty中client和server最大的区别是Bootstrap和Channel使用的实现
     *
     * @param args
     */
    public static void main(String[] args) throws Exception {
        String host = "localhost";
        int port = 8081;
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            //和ServerBootStrap类似，但是用于客户端连接或者无连接通道的非服务器通道
            Bootstrap bootstrap = new Bootstrap();
            //如果只指定一个EventLoopGroup，其将同时用作boss组和worker组。不过boss worker不是用作client的
            bootstrap.group(workerGroup);
            //与 NioServerSocketChannel 不同，NioSocketChannel 用于创建客户端 Channel
            bootstrap.channel(NioSocketChannel.class);
            //注意，与 ServerBootstrap 不同，在这里不使用 childOption () ，因为客户端SocketChannel没有父节点。
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new TimeClientHandler());
                }
            });

            //调用connect连接
            ChannelFuture f = bootstrap.connect(host, port).sync();

            //等待知道连接关闭
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }

    }
}
