package example.time;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 *
 * 这里实现一个时间协议，发送信息包含32位的字节。不接受任何请求在信息发送后关闭连接
 * 因为要不需要接收消息，但是要在建立连接后立即发送信息，这里就不能用channelRead()。应该用channelActive()
 * @author NGinko
 * @date 2022-04-04 23:14
 */
public class TimeServerHandle extends ChannelInboundHandlerAdapter {

    //active方法会在连接建立同时准备生成流量的时候建立
    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        //为了发送一条新的消息，需要分配一个新的缓冲区其中包含信息。发送的信息32位，所以这里容量最小要4字节。
        final ByteBuf time = ctx.alloc().buffer(4);
        time.writeInt((int) (System.currentTimeMillis() / 1000L + 2208988800L));

        //这里和通常的Nio不同的是没有指针。ByteBuf有两个标记，分别用在读写操作上。
        //ChannelHandlerContext.write()会返回channelFuture。会返回channelFuture表示I/O操作还没发送
        //任何请求可能都还没执行，因为Netty中所有的操作都是异步的。在发送消息前连接可能是关闭的。
        //因此需要在ChannelFuture完成后调用close()方法，写操作结束后会统计监听器。而且close也可能没真正关闭而是返回channelFuture
        final ChannelFuture f = ctx.writeAndFlush(time);
        //那么如何在写请求完成后收到通知呢。只要添加一个ChannelFutureListener给返回的future。这里创建的Listener是在操作结束的时候关闭
        //也可以使用更简单的代码 f.addListener(ChannelFutureListener.CLOSE);
        f.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                assert f == future;
                ctx.close();
            }
        });
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}
