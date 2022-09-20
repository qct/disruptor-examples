package com.example.disruptor;

import com.lmax.disruptor.EventProcessor;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.Sequence;
import com.lmax.disruptor.Sequencer;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.WorkProcessor;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.EventProcessorFactory;
import com.lmax.disruptor.dsl.ExceptionHandlerWrapper;
import com.lmax.disruptor.util.DaemonThreadFactory;
import java.nio.ByteBuffer;

/**
 * <p>Created by qdd on 2022/9/15.
 */
public class LongEventMainProcessor {

    private static final int SUM = 1;

    public static void main(String[] args) throws InterruptedException {
        int bufferSize = 1024;
        Disruptor<LongEvent> disruptor = new Disruptor<>(LongEvent::new, bufferSize, DaemonThreadFactory.INSTANCE);

        RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer();
        disruptor.handleEventsWith(new EventProcessorFactory<LongEvent>() {
            @Override
            public EventProcessor createEventProcessor(RingBuffer<LongEvent> rb, Sequence[] bs) {
                return new WorkProcessor<>(
                        rb,
                        rb.newBarrier(),
                        new WorkHandler<LongEvent>() {
                            @Override
                            public void onEvent(LongEvent event) throws Exception {
                                System.out.println(event);
                            }
                        },
                        new ExceptionHandlerWrapper<>(),
                        new Sequence(Sequencer.INITIAL_CURSOR_VALUE));
            }
        });
        disruptor.start();

        LongEventProducerTranslator producer = new LongEventProducerTranslator(ringBuffer);
        ByteBuffer bb = ByteBuffer.allocate(8);
        for (long l = 0; l < SUM; l++) {
            bb.putLong(0, l);
            producer.onData(bb);
        }
    }
}
