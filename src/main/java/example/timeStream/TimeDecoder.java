package example.timeStream;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * ByteToMessageDecoder是ChannelInboundHandler 的一个实现，目的是解决碎片处理的问题
 * @author NGinko
 * @date 2022-04-06 22:26
 */
public class TimeDecoder extends ByteToMessageDecoder {
    //接收到新的数据时，ByteToMessageDecoder使用内部维护的缓冲区，同时会调用decode
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < 4) {
            return;
        }

        out.add(in.readBytes(4)); // 如果被添加到out,到这里说明decode成功。
        //然后ByteToMessageDecoder会丢弃现在缓冲区的部分。不需要额外解码多个信息，ByteToMessageDecoder会一直调用decode知道没有能被添加到out的
    }

    /**
     * 需要修改channelPipeLine插入到channelInitializer中
     * b.handler(new ChannelInitializer<SocketChannel>() {
     *     @Override
     *     public void initChannel(SocketChannel ch) throws Exception {
     *         ch.pipeline().addLast(new TimeDecoder(), new TimeClientHandler());
     *     }
     * });
     */

    /**
     * 如果激进点也可以用ReplayingDecoder
     * public class TimeDecoder extends ReplayingDecoder<Void> {
     *     @Override
     *     protected void decode(
     *             ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
     *         out.add(in.readBytes(4));
     *     }
     * }
     */

    /**
     * netty也提供了很多可以直接用的协议在下面两个路径下
     * io.netty.example.factorial  for a binary protocol, and
     * io.netty.example.telnet  for a text line-based protocol.
     */


}
