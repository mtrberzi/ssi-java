package io.lp0onfire.ssi.microcontroller.peripherals;

import static org.junit.Assert.assertEquals;
import io.lp0onfire.ssi.microcontroller.RV32Core;

import org.junit.Before;
import org.junit.Test;

public class TestInterruptController {

  private RV32Core cpu;
  private InterruptController pic;
  
  @Before
  public void setup() {
    cpu = new RV32Core();
    pic = new InterruptController(cpu);
  }
  
  @Test
  public void testAssertInterruptWhileMasterDisabled() {
    pic.setMasterEnable(false);
    pic.setInterruptEnabled(0, true);
    pic.assertInterrupt(0);
    assertEquals(-1, pic.nextInterrupt());
  }
  
  @Test
  public void testOneInterrupt() {
    pic.setMasterEnable(true);
    pic.setInterruptEnabled(0, true);
    pic.assertInterrupt(0);
    assertEquals(0, pic.nextInterrupt());
  }
  
  @Test
  public void testAssertDisabledInterrupt() {
    pic.setMasterEnable(true);
    pic.setInterruptEnabled(0, false);
    pic.assertInterrupt(0);
    assertEquals(-1, pic.nextInterrupt());
  }
  
  @Test
  public void testTwoInterrupts_EqualPriority() {
    pic.setMasterEnable(true);
    pic.setInterruptEnabled(2, true);
    pic.setInterruptEnabled(5, true);
    pic.setInterruptPriority(2, 0);
    pic.setInterruptPriority(5, 0);
    pic.assertInterrupt(2);
    pic.assertInterrupt(5);
    assertEquals(2, pic.nextInterrupt());
  }
  
  @Test
  public void testTwoInterrupts_DifferentPriorities() {
    pic.setMasterEnable(true);
    pic.setInterruptEnabled(2, true);
    pic.setInterruptEnabled(5, true);
    pic.setInterruptPriority(2, 3);
    pic.setInterruptPriority(5, 0);
    pic.assertInterrupt(2);
    pic.assertInterrupt(5);
    assertEquals(5, pic.nextInterrupt());
  }
  
}
