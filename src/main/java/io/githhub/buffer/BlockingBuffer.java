package io.githhub.buffer;


import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class BlockingBuffer<T> {


    private final T[] buffer;
    private final long waitTime;
    private volatile int read = 0, write = -1;
    private final Lock lock = new ReentrantLock();
    private final Condition notFull  = lock.newCondition();
    private final Condition notEmpty = lock.newCondition();

    public BlockingBuffer() {
        this(10);
    }

    public BlockingBuffer(int bufferSize) {
        this(bufferSize, 100);
    }

    public BlockingBuffer(int bufferSize, long maxWaitMillis) {
        if (bufferSize <= 0) {
            throw new IllegalArgumentException("Buffer size must be greater than 0");
        }
        buffer = (T[]) new Object[bufferSize];
        waitTime = maxWaitMillis;
    }

    public void add(T elm) throws InterruptedException {
        Objects.requireNonNull(elm, "elm must not be null");
        int writeIdx = ++write;
        lock.lock();
        try {
            while (writeIdx >= buffer.length) {
                notFull.await();
            }
            buffer[writeIdx] = elm;
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    public List<T> get() throws InterruptedException {
        lock.lock();
        long getWaitTime = System.currentTimeMillis();
        try {
            while (write <= 0 && (System.currentTimeMillis() - getWaitTime) < waitTime) {
                notEmpty.await(10, TimeUnit.MILLISECONDS);
            }
            List<T> result = Arrays.stream(buffer)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            write = 0;
            notFull.signal();
            return result;
        } finally {
            lock.unlock();
        }
    }
}
