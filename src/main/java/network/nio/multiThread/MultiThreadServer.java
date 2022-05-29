
package network.nio.multiThread;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static utils.ByteBufferUtil.debugAll;

/**
 * @author NGinko
 * @date 2022-05-28 11:33
 */
@Slf4j
public class MultiThreadServer {

    public static void main(String[] args) throws IOException {
        new BootEventLoop().register();
    }

    /**
     * 单线程分配一个选择器，专门处理accept事件
     * 创建cpu核心数的线程，每个线程都包含一个选择器，轮流处理read事件
     */
    static class BootEventLoop implements Runnable {

        private Selector boss;
        private WorkerEventLoop[] workers;
        private volatile boolean start = false;
        AtomicInteger index = new AtomicInteger();

        public void register() throws IOException {
            if (!start) {
                ServerSocketChannel ssc = ServerSocketChannel.open();
                ssc.bind(new InetSocketAddress(8080));
                ssc.configureBlocking(false);
                boss = Selector.open();
                //注册channel到selector上，指定accept事件，绑定的才关注
                SelectionKey sscKey = ssc.register(boss, SelectionKey.OP_ACCEPT);
                workers = initEventLoop();
                new Thread(this, "boss").start();
                log.info("the boss thread started");
                start = true;
            }
        }

        public WorkerEventLoop[] initEventLoop() {
            WorkerEventLoop[] workerEventLoops = new WorkerEventLoop[2];
            for (int i = 0; i < workerEventLoops.length; i++) {
                workerEventLoops[i] = new WorkerEventLoop(i);
            }
            return workerEventLoops;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    boss.select();
                    Iterator<SelectionKey> iterator = boss.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        iterator.remove();
                        if (key.isAcceptable()) {
                            ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                            SocketChannel sc = ssc.accept();
                            sc.configureBlocking(false);
                            log.info("{} connected", sc.getRemoteAddress());
                            workers[index.getAndIncrement() % workers.length].register(sc);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static class WorkerEventLoop implements Runnable {

        private Selector worker;
        private int index;
        private volatile boolean start = false;

        public WorkerEventLoop(int index) {
            this.index = index;
        }

        private final ConcurrentLinkedQueue<Runnable> tasks = new ConcurrentLinkedQueue<>();

        public void register(SocketChannel sc) throws IOException {
            if (!start) {
                worker = Selector.open();

                new Thread(this, "worker-" + index).start();
                start = true;
            }
            tasks.add(() -> {
                try {
                    SelectionKey scKey = sc.register(worker, 0, null);
                    scKey.interestOps(SelectionKey.OP_READ);
                    worker.selectNow();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            worker.wakeup();

        }

        @Override
        public void run() {
            while (true) {
                try {
                    worker.select(); //结合上面的阻塞队列。没有时间触发的话会一直阻塞，所以注册好worker之后，需要wakeup
                    Runnable task = tasks.poll();
                    if (task != null) {
                        task.run();
                    }
                    Set<SelectionKey> selectionKeys = worker.selectedKeys();
                    Iterator<SelectionKey> iterator = selectionKeys.iterator();
                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        if (key.isReadable()) {
                            SocketChannel sc = (SocketChannel) key.channel();
                            ByteBuffer buffer = ByteBuffer.allocate(128);
                            try {
                                int read = sc.read(buffer);
                                if (read == -1) {
                                    key.cancel();
                                    sc.close();
                                } else {
                                    buffer.flip();
                                    log.debug("{} message", sc.getRemoteAddress());
                                    debugAll(buffer);
                                }
                            } catch (IOException e) {
                                log.error("{} :thread exception", Thread.currentThread().getName());
                                e.printStackTrace();
                                key.cancel();
                                sc.close();
                            }
                        }
                        iterator.remove();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }


}
