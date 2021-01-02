package io.githhub.buffer;



import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BlockingBuffer<T> {


    private final T[] buffer;
    private final long maxWaitTime;
    private volatile int writeIndex = -1;
    private final Lock lock = new ReentrantLock(true);
    private final Condition notFull  = lock.newCondition();
    private final Condition notEmpty = lock.newCondition();
    private long lastWriteMillis = System.currentTimeMillis();

    private static void checkNotNull(Object v) {
        if (v == null)
            throw new NullPointerException();
    }

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
        buffer = (T[]) new Object[bufferSize];
        maxWaitTime = maxWaitMillis;
    }


    public void add(T elm) throws InterruptedException {
        checkNotNull(elm);
        lock.lock();
        try {
            while (writeIndex >= buffer.length) {
                notFull.await(1, TimeUnit.MILLISECONDS);
            }
            int writeIdx = ++writeIndex;
            buffer[writeIdx] = elm;
            if (writeIdx == buffer.length-1) {
                notEmpty.signal();
            }
        } finally {
            lock.unlock();
        }
    }


    public List<T> get() throws InterruptedException {
        lock.lock();
        long currentTime = System.currentTimeMillis();
        try {
            while (availableCapacity() > 0 &&
                    (System.currentTimeMillis() - currentTime) < maxWaitTime) {
                notEmpty.await(1, TimeUnit.MILLISECONDS);
            }
            List<T> result = new ArrayList<>(10);
            for (int i = 0; i < buffer.length; i++) {
                if (buffer[i] != null) {
                    result.add(buffer[i]);
                }
                buffer[i] = null;
            }
            writeIndex = -1;
            notFull.signal();
            return result;
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        return (buffer.length) - availableCapacity();
    }


    private int availableCapacity() {
        return (buffer.length-1) - writeIndex;
    }
}
