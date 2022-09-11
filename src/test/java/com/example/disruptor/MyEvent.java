package com.example.disruptor;

import lombok.Getter;
import lombok.ToString;

/** Created by qdd on 2022/5/4. */
@ToString
public class MyEvent {
  private int value;
  private long sequence;

  @Getter public long orderId;

  public int symbol;

  public OrderCommandType command;

  // filled by grouping processor:
  public long eventsGroup;
  public int serviceFlags;

  // result code of command execution - can also be used for saving intermediate state
  public String resultCode;

  // trade events chain
  public MatcherTradeEvent matcherEvent;

  // optional market data
  public String marketData;

  public void setValue(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }

  public void setSequence(long sequence) {
    this.sequence = sequence;
  }

  public long getSequence() {
    return sequence;
  }
}
