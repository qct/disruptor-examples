package com.example.disruptor;

import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.RingBuffer;
import java.nio.ByteBuffer;

/**
 * <p>Created by qdd on 2022/9/14.
 */
public class LongEventProducerTranslator {
    private final RingBuffer<LongEvent> ringBuffer;

    public LongEventProducerTranslator(RingBuffer<LongEvent> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    private static final EventTranslatorOneArg<LongEvent, ByteBuffer> TRANSLATOR =
            new EventTranslatorOneArg<LongEvent, ByteBuffer>() {
                @Override
                public void translateTo(LongEvent event, long sequence, ByteBuffer bb) {
                    event.set(bb.getLong(0));
                }
            };

    public void onData(ByteBuffer bb) {
        ringBuffer.publishEvent(TRANSLATOR, bb);
    }
}