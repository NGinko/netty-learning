package example.timePojo;

import java.util.Date;

/**
 * @author NGinko
 * @date 2022-04-06 23:08
 */
public class UnixTime {

    private final long value;

    public UnixTime() {
        this(System.currentTimeMillis() / 1000L + 2208988800L);
    }

    public UnixTime(long value) {
        this.value = value;
    }

    public long value() {
        return value;
    }

    @Override
    public String toString() {
        return new Date((value() - 2208988800L) * 1000L).toString();
    }

    /**
     * 客户端优化：
     * 可以直接使用ByteBuf替换成POJO，decode时可以从其中提取信息做对象的初始化。
     * protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
     *     if (in.readableBytes() < 4) {
     *         return;
     *     }
     *
     *     out.add(new UnixTime(in.readUnsignedInt()));
     * }
     *
     * 同时channelRead也不需要再对byteBuf解码，直接转换成unixTime对象
     * public void channelRead(ChannelHandlerContext ctx, Object msg) {
     *     UnixTime m = (UnixTime) msg;
     *     System.out.println(m);
     *     ctx.close();
     * }
     */

    /**
     * 服务端优化：
     * 在服务端可以传入POJO对象
     * public void channelActive(ChannelHandlerContext ctx) {
     *     ChannelFuture f = ctx.writeAndFlush(new UnixTime());
     *     f.addListener(ChannelFutureListener.CLOSE);
     * }
     * 如果这么做了还需要一个编码器，可以实现ChannelOutBoundHandler。
     * public class TimeEncoder extends ChannelOutboundHandlerAdapter {
     *     @Override
     *     public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
     *         UnixTime m = (UnixTime) msg;
     *         ByteBuf encoded = ctx.alloc().buffer(4);
     *         encoded.writeInt((int)m.value());
     *         ctx.write(encoded, promise); // (1)
     *         //通过传递promise，这样实际编码输出的时候，会标记上成功或失败
     *         //不需要再调用flush，有一个单独的void flush方法会覆盖flush操作？？
     *     }
     * }
     *
     * 还可以更简化
     * public class TimeEncoder extends MessageToByteEncoder<UnixTime> {
     *     @Override
     *     protected void encode(ChannelHandlerContext ctx, UnixTime msg, ByteBuf out) {
     *         out.writeInt((int)msg.value());
     *     }
     * }
     * serverHandle的encode都做完之后需要的就是把TimeEncode插入到服务端的channelPipeLine当中
     */
}