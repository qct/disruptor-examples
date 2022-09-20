package com.example.disruptor;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <p>Created by qdd on 2022/9/14.
 */
public class LongEventMainLegacy {

    private static final int CONSUMER_NUMS = 10;
    private static final int SUM = 100000;


    public static void main(String[] args) {
        ExecutorService executor = Executors.newCachedThreadPool();

        long start = System.currentTimeMillis();
        int bufferSize = 1024;
        Disruptor<LongEvent> disruptor = new Disruptor<>(LongEvent::new, bufferSize, executor);

        // consumer
        LongEventHandler[] consumers = new LongEventHandler[CONSUMER_NUMS];
        for (int i = 0; i < CONSUMER_NUMS; i++) {
            consumers[i] = new LongEventHandler();
        }
        disruptor.handleEventsWith(consumers);
        disruptor.start();

        // producer
        RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer();
        LongEventProducer producer = new LongEventProducer(ringBuffer);
        ByteBuffer bb = ByteBuffer.allocate(8);
        for (long i = 0; i < SUM; i++) {
            bb.putLong(0, i);
            producer.onData(bb);
            System.out.println("Success producer data : " + i);
        }
        long end = System.currentTimeMillis();
        disruptor.shutdown();
        System.out.println("Total time : " + (end - start));
    }
}
