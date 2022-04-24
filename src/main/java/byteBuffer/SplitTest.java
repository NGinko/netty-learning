package byteBuffer;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static utils.ByteBufferUtil.debugAll;

/**
 * @author NGinko
 * @date 2022-04-24 22:42
 */
public class SplitTest {


    public static void main(String[] args) {
        ByteBuffer source = ByteBuffer.allocate(32);
        source.put("Hello,world\nI'm liming\nHo".getBytes());
        split(source);
        source.put("w are you?\nWelcome home".getBytes());
        split(source);
    }

    public static void split(ByteBuffer source) {
        source.flip();//切换到读模式
        int oldLimit = source.limit(); //读取限制，在写模式下即为有元素的限制坐标
        for (int i = 0; i < oldLimit; i++) {
            if (source.get(i) == '\n') {
                ByteBuffer target = ByteBuffer.allocate(i + 1 - source.position()); //因为get不改变position，这样肯定是最开始的
                source.limit(i + 1);//指定读取的限制
                target.put(source);
                debugAll(target);
                source.limit(oldLimit);//限制切换到队伍末尾
            }
        }
        source.compact();
    }
}
