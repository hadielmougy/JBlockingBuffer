package io.githhub.buffer;


import sun.reflect.generics.reflectiveObjects.NotImplementedException;

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
        int writeIdx = ++writeIndex;
        lock.lock();
        try {
            while (writeIdx >= buffer.length) {
                notFull.await();
            }
            buffer[writeIdx] = elm;
            lastWriteMillis = System.currentTimeMillis();
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }


    public List<T> get() throws InterruptedException {
        lock.lock();
        long currentTime = System.currentTimeMillis();
        long millisToWait = Math.abs(currentTime - lastWriteMillis - maxWaitTime);
        try {
            while (availableCapacity() != 0
                    && System.currentTimeMillis() < (currentTime + millisToWait)) {
                notEmpty.await(100, TimeUnit.NANOSECONDS);
            }
            List<T> result = Arrays.stream(buffer)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            writeIndex = 0;
            notFull.signal();
            return result;
        } finally {
            lock.unlock();
        }
    }


    private int availableCapacity() {
        return buffer.length - writeIndex - 1;
    }
}
