package io.github.buffer;

import io.githhub.buffer.BlockingBuffer;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class BlockingBufferTest {

    @Test
    public void shouldAddAndGet() throws InterruptedException {
        BlockingBuffer<Integer> buffer = new BlockingBuffer<>();
        int sum = 0;
        for (int i = 0; i < 10; i++) {
            buffer.add(i);
            sum += i;
        }
        List<Integer> result = buffer.get();
        Assert.assertEquals(10, result.size());
        Assert.assertTrue(sum ==  result.stream().collect(Collectors.summingInt(Integer::valueOf)));
    }


    @Test
    public void shouldAddConcurrentlyAndGet() throws InterruptedException, ExecutionException {
        BlockingBuffer<Integer> buffer = new BlockingBuffer<>();
        ExecutorService exe = Executors.newFixedThreadPool(4);
        int sum = 0;
        for (int i = 0; i < 10; i++) {
            final int val = i;
            exe.submit(() -> {
                try {
                    buffer.add(val);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            sum += i;
        }
        Thread.sleep(100);
        List<Integer> result = exe.submit(() -> buffer.get()).get();
        Assert.assertTrue(10 == result.size());
        Assert.assertTrue(sum ==  result.stream().collect(Collectors.summingInt(Integer::valueOf)));
    }



    @Test
    public void shouldWaitTillBufferIsFull() throws InterruptedException, ExecutionException {
        BlockingBuffer<Integer> buffer = new BlockingBuffer<>();
        ExecutorService exe = Executors.newFixedThreadPool(4);
        Future<List<Integer>> resultFuture = exe.submit(() -> buffer.get());
        for (int i = 0; i < 10; i++) {
            buffer.add(i);
        }
        List<Integer> result = resultFuture.get();
        Assert.assertTrue(10 == result.size());
    }



    @Test
    public void shouldReturnEmptyListInTimeOut() throws InterruptedException {
        BlockingBuffer<Integer> buffer = new BlockingBuffer<>();
        List<Integer> result = buffer.get();
        Assert.assertTrue(0 == result.size());
    }
}
