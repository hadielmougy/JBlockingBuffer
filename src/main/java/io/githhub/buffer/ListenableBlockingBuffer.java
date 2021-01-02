package io.githhub.buffer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;

public class ListenableBlockingBuffer<T> implements Runnable {

    private final BlockingBuffer<T> buffer;
    private final ExecutorService executor;
    private List<Consumer<List<T>>> listeners;

    public ListenableBlockingBuffer(BlockingBuffer<T> buffer) {
        this(buffer, ForkJoinPool.commonPool());
    }

    public ListenableBlockingBuffer(BlockingBuffer<T> buffer, ExecutorService executorService) {
        this.buffer = Objects.requireNonNull(buffer);
        this.executor = executorService;
        this.listeners = new LinkedList<>();
        startWorker();
    }

    private void startWorker() {
        executor.submit(this);
    }


    public void add(T elm) throws InterruptedException {
        buffer.add(elm);
    }


    public void addListener(Consumer<List<T>> consumer) {
        this.listeners.add(Objects.requireNonNull(consumer));
    }


    @Override
    public void run() {
        for (;;) {
            for (Consumer<List<T>> c : listeners) {
                final List<T> items = getItems();
                if (items != null && !items.isEmpty()) {
                    executor.submit(()-> c.accept(items));
                }
            }
        }
    }

    private List<T> getItems() {
        try {
            return buffer.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
