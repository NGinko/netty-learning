package byteBuffer;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author NGinko
 * @date 2022-04-12 23:14
 */
@Slf4j
public class TestByteBuffer {

    public static void main(String[] args) {
        try (FileInputStream file = new FileInputStream("abc.txt")) {
            FileChannel channel = file.getChannel();
            ByteBuffer byteBuffer = ByteBuffer.allocate(10);// ascii码一个字符占用一个字节，所以10字节是10个字符
            while (true) {
                int len = channel.read(byteBuffer); //read into buffer
                if (len == -1) {
                    break;
                }
                byteBuffer.flip();//change buffer to read pattern
                while (byteBuffer.hasRemaining()) {
                    log.info("{}", (char) byteBuffer.get()); //read current byte at the position
                }
                //change to write pattern
                byteBuffer.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
