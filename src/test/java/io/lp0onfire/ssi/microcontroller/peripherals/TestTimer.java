package io.lp0onfire.ssi.microcontroller.peripherals;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestTimer {

  @Test
  public void testCounter_AfterReset_DoesNotCount() {
    Timer timer = new Timer();
    int counter_init = timer.getCounter();
    int prescalerCounter_init = timer.getPrescalerCounter();
    timer.cycle();
    assertEquals(counter_init, timer.getCounter());
    assertEquals(prescalerCounter_init, timer.getPrescalerCounter());
  }
  
  @Test
  public void testCounter_Enabled() {
    Timer timer = new Timer();
    int counter_init = timer.getCounter();
    timer.setTimerStart(true);
    timer.cycle();
    assertEquals(counter_init + 1, timer.getCounter());
  }
  
  @Test
  public void testCounter_Prescaler() {
    Timer timer = new Timer();
    int counter_init = timer.getCounter();
    timer.setTimerStart(true);
    timer.setPrescalerEnable(true);
    timer.setPrescalerPeriod(2); // prescaler = 8
    for (int i = 0; i < 7; ++i) {
      timer.cycle();
      assertEquals(counter_init, timer.getCounter());
    }
    timer.cycle();
    assertEquals(counter_init + 1, timer.getCounter());
  }
  
}
