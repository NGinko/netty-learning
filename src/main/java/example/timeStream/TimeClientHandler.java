package example.timeStream;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.Date;

/**
 * 因为TCP\IP基于流式传输，如何区分不同批次传输的信息
 * 1.因为time协议格式固定。所以可以创建一个内部缓冲区，然后等到4个字节被接受到内部缓冲区
 * 2.因为channelPipeLine可以添加多个channelHandle，所以可以在添加一个专用做decode
 * @author NGinko
 * @date 2022-04-06 21:48
 */
public class TimeClientHandler extends ChannelInboundHandlerAdapter {


    private ByteBuf buf;
    //ChannelHandler可以监听handlerAdded和handlerRemoved。可以任意初始化或取消任务，主要不会阻塞很久

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        buf = ctx.alloc().buffer(4);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        buf.release();
        buf = null;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf m = (ByteBuf) msg;
        //接受到的数据都应该写到buf里
        buf.writeBytes(m);
        m.release();

        //检查缓冲区空间是否充足，并且继续处理实际的业务逻辑。
        //每次数据传入，都会调用channelRead，最终缓冲区会写满4个字节
        if (buf.readByte() >= 4) {
            long currentTimeMillis = (buf.readUnsignedInt() - 2208988800L) * 1000L;
            System.out.println(new Date(currentTimeMillis));
            ctx.close();
        }
    }



}
