package com.example.disruptor;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <p>Created by qdd on 2022/9/14.
 */
public class LongEventMainBasic {

    private static final int CONSUMER_NUMS = 10;
    private static final int SUM = 100000;

    public static void main(String[] args) throws InterruptedException {
        ExecutorService executor = Executors.newCachedThreadPool();

        long start = System.currentTimeMillis();
        int ringBufferSize = 1024;
        Disruptor<LongEvent> disruptor = new Disruptor<>(LongEvent::new, ringBufferSize, executor);

        // consumer
        EventHandler<LongEvent>[] consumers = new LongEventHandler[CONSUMER_NUMS];
        for (int i = 0; i < CONSUMER_NUMS; i++) {
            consumers[i] = new LongEventHandler();
        }
        disruptor.handleEventsWith(consumers);
        disruptor.start();

        // producer
        RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer();
        ByteBuffer bb = ByteBuffer.allocate(8);
        for (int l = 0; l < SUM; l++) {
            bb.putLong(0, l);
            ringBuffer.publishEvent((event, sequence, buffer) -> event.set(buffer.getLong(0)), bb);
        }
        long end = System.currentTimeMillis();
        disruptor.shutdown();
        System.out.println("Total time : " + (end - start));
    }
}
