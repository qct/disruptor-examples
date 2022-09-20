package com.example.disruptor;

import com.lmax.disruptor.EventHandler;

/**
 * <p>Created by qdd on 2022/9/13.
 */
public class LongEventHandler implements EventHandler<LongEvent> {

    @Override
    public void onEvent(LongEvent event, long sequence, boolean endOfBatch) throws Exception {
        System.out.println("Event: " + event);
    }
}
