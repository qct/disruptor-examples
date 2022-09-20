package com.example.disruptor;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.EventHandlerGroup;
import com.lmax.disruptor.dsl.ProducerType;
import java.util.concurrent.ThreadFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Created by qdd on 2022/4/24. */
public class DisruptorApplicationTests {

    private Disruptor<MyEvent> disruptor;

    // enable MatcherTradeEvent pooling
    public static final boolean EVENTS_POOLING = false;

    private static final EventTranslatorOneArg<MyEvent, Integer> TRANSLATOR = (myEvent, seq, arg) -> {
        myEvent.setSequence(seq);
        myEvent.setValue(arg);
    };

    @BeforeEach
    void setUp() throws InterruptedException {
        ThreadFactory threadFactory = new ThreadFactoryBuilder().build();
        int ringBufferSize = 1024;
        disruptor = new Disruptor<>(
                MyEvent::new, ringBufferSize, threadFactory, ProducerType.SINGLE, new BlockingWaitStrategy());
    }

    @AfterEach
    void tearDown() {
        disruptor.shutdown();
    }

    /**
     * 1. start --->  1,2   ---> end
     */
    @Test
    public void sequential() throws InterruptedException {
        disruptor
                .handleEventsWith((event, sequence, endOfBatch) -> System.out.println("Consumer1: Seq: " + sequence))
                .then((event, sequence, endOfBatch) -> System.out.println("Consumer2: Seq: " + sequence));
        disruptor.start();
        produce(2);
    }

    /**
     *             1
     * start --->       ---> end
     *             2
     */
    @Test
    void parallel() throws InterruptedException {
        disruptor.handleEventsWithWorkerPool(
                event -> System.out.println("WorkHandler1, received Event: " + event.getSequence()),
                event -> System.out.println("WorkHandler2, received Event: " + event.getSequence()));
        disruptor.start();
        produce(1);
    }

    /**
     *                  2
     * start ---> 1 ->      ---> end
     *                  3
     */
    @Test
    void sequentialAndParallel() throws InterruptedException {
        EventHandlerGroup<MyEvent> group = disruptor.handleEventsWith((event, sequence, endOfBatch) -> {
            System.out.println("Consumer1: Seq: " + sequence);
            System.out.println("Sleep 2 seconds Seq " + sequence);
            Thread.sleep(2000L);
        });
        group.handleEventsWith((event, sequence, endOfBatch) -> System.out.println("Consumer2: Seq: " + sequence));
        group.handleEventsWith((event, sequence, endOfBatch) -> System.out.println("Consumer3: Seq: " + sequence));
        disruptor.start();
        produce(2);
    }

    /**
     * simulate exchange-core.
     */
    @Test
    void simulateExchangeCore() {
        // 1. grouping processor (G)
        final CoreWaitStrategy coreWaitStrategy = CoreWaitStrategy.BLOCKING;
        final SharedPool sharedPool = new SharedPool(4, 1, 1);
        final EventHandlerGroup<MyEvent> afterGrouping = disruptor.handleEventsWith(
                (rb, bs) -> new GroupingProcessor(rb, rb.newBarrier(bs), coreWaitStrategy, sharedPool));
    }

    private void produce(int num) throws InterruptedException {
        for (int i = 0; i < num; i++) {
            disruptor.getRingBuffer().publishEvent(TRANSLATOR, i);
            Thread.sleep(1000L);
        }
    }
}
