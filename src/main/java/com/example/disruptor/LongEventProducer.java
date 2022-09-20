package com.example.disruptor;

import com.lmax.disruptor.RingBuffer;
import java.nio.ByteBuffer;

/**
 * <p>Created by qdd on 2022/9/13.
 */
public class LongEventProducer {

    private final RingBuffer<LongEvent> ringBuffer;

    public LongEventProducer(RingBuffer<LongEvent> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    public void onData(ByteBuffer bb) {
        long sequence = ringBuffer.next();
        try {
            // Get the entry in the Disruptor for the sequence
            LongEvent event = ringBuffer.get(sequence);
            // Fill with data
            event.set(bb.getLong(0));
        } finally{
            ringBuffer.publish(sequence);
        }
    }
}
