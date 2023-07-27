package io.githhub.buffer;



import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BlockingBuffer<T> {


    private final long maxWaitTime;
    private final BlockingQueue<T> bufferQueue;
    private final int bufferSize;

    private final Lock lock = new ReentrantLock(true);

    public BlockingBuffer() {
        this(10);
    }

    public BlockingBuffer(int bufferSize) {
        this(bufferSize, 100);
    }

    public BlockingBuffer(int bufferSize, long maxWaitMillis) {
        if (maxWaitMillis <= 0) {
            throw new IllegalArgumentException("maxWaitMillis must be greater than 0");
        }
        this.bufferSize  = bufferSize;
        this.maxWaitTime = maxWaitMillis;
        this.bufferQueue = queue(bufferSize);
    }

    private BlockingQueue<T> queue(int bufferSize) {
        return bufferSize == -1 ?
                new LinkedBlockingQueue<>() :
                new ArrayBlockingQueue<>(bufferSize);
    }


    public void add(T elm) throws InterruptedException {
        bufferQueue.put(elm);
    }

    public List<T> get() throws InterruptedException {
        try {
            lock.lock();
            return waitAndGet();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
            Thread.currentThread().interrupt();
        } finally{
            lock.unlock();
        }
        return Collections.emptyList();
    }

    private List<T> waitAndGet() throws InterruptedException {
        List<T> result = newList();
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

    private List<T> newList() {
        return bufferSize == -1 ?
                new LinkedList<>() :
                new ArrayList<>(bufferSize);
    }


    public int size() {
        return bufferQueue.size();
    }

    public void reset() {
        try{
            lock.lock();
            bufferQueue.clear();
        } finally{
            lock.unlock();
        }
    }
}
