package io.githhub.buffer;



import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class BlockingBuffer<T> {


    private final long maxWaitTime;
    private final ArrayBlockingQueue<T> bufferQueue;
    private final int bufferSize;

    public BlockingBuffer() {
        this(10);
    }

    public BlockingBuffer(int bufferSize) {
        this(bufferSize, 100);
    }

    public BlockingBuffer(int bufferSize, long maxWaitMillis) {
        if (bufferSize <= 0) {
            throw new IllegalArgumentException("bufferSize must be greater than 0");
        }
        if (maxWaitMillis <= 0) {
            throw new IllegalArgumentException("maxWaitMillis must be greater than 0");
        }
        this.maxWaitTime = maxWaitMillis;
        this.bufferSize = bufferSize;
        this.bufferQueue = new ArrayBlockingQueue<>(bufferSize, true);
    }


    public void add(T elm) throws InterruptedException {
        bufferQueue.put(elm);
    }


    public List<T> get() throws InterruptedException {
        List<T> result = new ArrayList<>(bufferSize);
        long remainingWait = maxWaitTime;
        for (int i = 0; i < bufferSize; i++) {
            long timeBeforePoll = System.currentTimeMillis();
            T el = bufferQueue.poll(remainingWait, TimeUnit.MILLISECONDS);
            remainingWait = System.currentTimeMillis() - timeBeforePoll;
            if (el == null) {
                break;
            }
            result.add(el);
        }
        return result;
    }

    public int size() {
        return bufferQueue.size();
    }

    public void reset() {
        bufferQueue.clear();
    }
}
