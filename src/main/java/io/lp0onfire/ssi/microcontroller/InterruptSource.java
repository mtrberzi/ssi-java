package io.lp0onfire.ssi.microcontroller;

public interface InterruptSource {

  boolean interruptAsserted();
  void acknowledgeInterrupt();
  
}
