package com.example.disruptor;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import java.nio.ByteBuffer;

/**
 * <p>Created by qdd on 2022/9/13.
 */
public class LongEventMainTranslator {

    private static final int CONSUMER_NUMS = 10;
    private static final int SUM = 100000;

    private static final EventTranslatorOneArg<LongEvent, ByteBuffer> TRANSLATOR =
            new EventTranslatorOneArg<LongEvent, ByteBuffer>() {
                @Override
                public void translateTo(LongEvent event, long sequence, ByteBuffer bb) {
                    event.set(bb.getLong(0));
                }
            };

    public static void main(String[] args) throws InterruptedException {
        // Specify the size of the ring buffer, must be power of 2.
        int bufferSize = 1024;
        long start = System.currentTimeMillis();

        // Construct the Disruptor
        Disruptor<LongEvent> disruptor = new Disruptor<>(
                LongEvent::new,
                bufferSize,
                DaemonThreadFactory.INSTANCE,
                ProducerType.SINGLE,
                new BlockingWaitStrategy());

        // consumer
        EventHandler<LongEvent>[] consumers = new LongEventHandler[CONSUMER_NUMS];
        for (int i = 0; i < CONSUMER_NUMS; i++) {
            consumers[i] = new LongEventHandler();
        }
        disruptor.handleEventsWith(consumers);
        // Start the Disruptor, starts all threads running
        disruptor.start();

        // Get the ring buffer from the Disruptor to be used for publishing.
        RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer();
        //        LongEventProducerTranslator producer = new LongEventProducerTranslator(ringBuffer);
        ByteBuffer bb = ByteBuffer.allocate(8);
        for (long l = 0; l < SUM; l++) {
            bb.putLong(0, l);
            ringBuffer.publishEvent(TRANSLATOR, bb);
            //            producer.onData(bb);
        }

        long end = System.currentTimeMillis();
        disruptor.shutdown();
        System.out.println("Total time : " + (end - start));
    }
}
