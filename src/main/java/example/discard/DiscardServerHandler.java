package example.discard;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

import java.nio.charset.Charset;

/**
 * @author NGinko
 * @date 2022-04-04 17:38
 */
public class DiscardServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf in = (ByteBuf) msg;
        try {
            //两种写法
            /*
            while (in.isReadable()) {
                System.out.println((char) in.readByte());
                System.out.flush();
            }*/
            System.out.println(in.toString(CharsetUtil.US_ASCII));
            //这里调用write可以将接收到的消息逐字的写入到的消息。
            // 调用write的话不用再像前面那样release，netty自己会release
            ctx.writeAndFlush(in);
        }finally {
            //也可以直接in.release()
            //ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //异常关闭连接
        cause.printStackTrace();
        ctx.close();
    }
}
