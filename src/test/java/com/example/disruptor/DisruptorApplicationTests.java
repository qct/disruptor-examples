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

  private final Disruptor<MyEvent> disruptor;

  // enable MatcherTradeEvent pooling
  public static final boolean EVENTS_POOLING = false;

  private static final EventTranslatorOneArg<MyEvent, Integer> TRANSLATOR =
      (myEvent, seq, arg) -> {
        myEvent.setSequence(seq);
        myEvent.setValue(arg);
      };

  public DisruptorApplicationTests() {
    ThreadFactory threadFactory = new ThreadFactoryBuilder().build();
    int ringBufferSize = 1024;
    disruptor =
        new Disruptor<>(
            MyEvent::new,
            ringBufferSize,
            threadFactory,
            ProducerType.MULTI,
            new BlockingWaitStrategy());
    // 1. start --->  1,2   ---> end
    //        disruptor.handleEventsWith(
    //            (event, sequence, endOfBatch) -> System.out.println(
    //                "Consumer1: Seq: " + sequence + ",endOfBatch: " + endOfBatch + ", received
    // Event: " +
    // event)).then(
    //            (event, sequence, endOfBatch) -> System.out.println(
    //                "Consumer2: Seq: " + sequence + ",endOfBatch: " + endOfBatch + ", received
    // Event: " + event));
    // 2.           1
    // start --->       ---> end
    //             2
    //        disruptor.handleEventsWithWorkerPool(event -> System.out.println("WorkHandler1,
    // received Event: " +
    // event),
    //            event -> System.out.println("WorkHandler2, received Event: " + event));
    // 3.                2
    // start ---> 1 ->      ---> end
    //                  3
    //        EventHandlerGroup<MyEvent> group = disruptor.handleEventsWith(
    //            (event, sequence, endOfBatch) -> System.out.println(
    //                "Consumer1: Seq: " + sequence + ",endOfBatch: " + endOfBatch + ", received
    // Event: " + event));
    //        group.handleEventsWith((event, sequence, endOfBatch) -> System.out.println(
    //            "Consumer2: Seq: " + sequence + ",endOfBatch: " + endOfBatch + ", received Event:
    // " + event));
    //        group.handleEventsWith((event, sequence, endOfBatch) -> System.out.println(
    //            "Consumer3: Seq: " + sequence + ",endOfBatch: " + endOfBatch + ", received Event:
    // " + event));
    // 4.simulate exchange-core
    // 1. grouping processor (G)
    final CoreWaitStrategy coreWaitStrategy = CoreWaitStrategy.BLOCKING;
    final SharedPool sharedPool = new SharedPool(4, 1, 1);
    final EventHandlerGroup<MyEvent> afterGrouping =
        disruptor.handleEventsWith(
            (rb, bs) -> new GroupingProcessor(rb, rb.newBarrier(bs), coreWaitStrategy, sharedPool));
  }

  @BeforeEach
  void setUp() {
    disruptor.start();
  }

  @AfterEach
  void tearDown() {
    disruptor.shutdown();
  }

  @Test
  void shouldProduceAndConsume() throws InterruptedException {
    for (int i = 0; i < 8; i++) {
      disruptor.getRingBuffer().publishEvent(TRANSLATOR, i);
      Thread.sleep(1000L);
    }
  }
}
