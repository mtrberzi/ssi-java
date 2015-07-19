package io.lp0onfire.ssi.microcontroller.peripherals;

import static org.junit.Assert.*;
import io.lp0onfire.ssi.microcontroller.AddressTrapException;

import org.junit.Test;

public class TestTimer {

  @Test
  public void testRead_CTRL() throws AddressTrapException {
    Timer timer = new Timer();
    int expected = 0;
    assertEquals(expected, timer.readWord(0x0));
    timer.setPrescalerPeriod(0b111);
    expected = 0x00000070;
    assertEquals(expected, timer.readWord(0x0));
    timer.setAutoReloadEnable(true);
    expected = 0x00000078;
    assertEquals(expected, timer.readWord(0x0));
    timer.setPrescalerEnable(true);
    expected = 0x0000007c;
    assertEquals(expected, timer.readWord(0x0));
    timer.setMasterInterruptEnable(true);
    expected = 0x0000007e;
    assertEquals(expected, timer.readWord(0x0));
    timer.setTimerStart(true);
    expected = 0x0000007f;
    assertEquals(expected, timer.readWord(0x0));
  }
  
  @Test
  public void testRead_COUNT() throws AddressTrapException {
    Timer timer = new Timer();
    int expected = 0x5a5aa5a5;
    timer.setCounter(expected);
    assertEquals(expected, timer.readWord(0x4));
  }
  
  @Test
  public void testRead_RELOAD() throws AddressTrapException {
    Timer timer = new Timer();
    int expected = 0x5a5aa5a5;
    timer.setReload(expected);
    assertEquals(expected, timer.readWord(0x8));
  }
  
  @Test
  public void testRead_MATCH() throws AddressTrapException {
    Timer timer = new Timer();
    int expected = 0x5a5aa5a5;
    timer.setMatch(expected);
    assertEquals(expected, timer.readWord(0xC));
  }
  
  @Test
  public void testRead_IE() throws AddressTrapException {
    Timer timer = new Timer();
    assertEquals(0x00000000, timer.readWord(0x10));
    timer.setOverflowInterruptEnable(true);
    assertEquals(0x00000001, timer.readWord(0x10));
    timer.setMatchInterruptEnable(true);
    assertEquals(0x00000003, timer.readWord(0x10));
  }
  
  @Test
  public void testRead_IP() throws AddressTrapException {
    Timer timer = new Timer();
    assertEquals(0x00000000, timer.readWord(0x14));
    // arrange for an overflow interrupt to be pending
    timer.setCounter(0xFFFFFFFF);
    timer.setMatch(0xEEEEEEEE);
    timer.setTimerStart(true);
    timer.cycle();
    assertEquals(0x00000001, timer.readWord(0x14));
    // now arrange for a match interrupt to be pending
    timer.setMatch(0x00000001);
    timer.cycle();
    assertEquals(0x00000003, timer.readWord(0x14));
  }
  
  @Test
  public void testWrite_CTRL_Prescaler() throws AddressTrapException {
    Timer timer = new Timer();
    timer.writeWord(0x0, 0x00000020);
    assertEquals(8, timer.getPrescalerPeriod());
  }
  
  @Test
  public void testWrite_CTRL_AutoReload() throws AddressTrapException {
    Timer timer = new Timer();
    timer.writeWord(0x0, 0x00000008);
    assertTrue(timer.getAutoReloadEnable());
  }
  
  @Test
  public void testWrite_CTRL_PrescalerEnable() throws AddressTrapException {
    Timer timer = new Timer();
    timer.writeWord(0x0, 0x00000004);
    assertTrue(timer.getPrescalerEnable());
  }
  
  @Test
  public void testWrite_CTRL_MasterInterrupt() throws AddressTrapException {
    Timer timer = new Timer();
    timer.writeWord(0x0, 0x00000002);
    assertTrue(timer.getMasterInterruptEnable());
  }
  
  @Test
  public void testWrite_CTRL_Start() throws AddressTrapException {
    Timer timer = new Timer();
    timer.writeWord(0x0, 0x00000001);
    assertTrue(timer.getTimerStart());
  }
  
  @Test
  public void testWrite_COUNT() throws AddressTrapException {
    Timer timer = new Timer();
    int expected = 0x5a5aa5a5;
    timer.writeWord(0x4, expected);
    assertEquals(expected, timer.getCounter());
  }
  
  @Test
  public void testWrite_RELOAD() throws AddressTrapException {
    Timer timer = new Timer();
    int expected = 0x5a5aa5a5;
    timer.writeWord(0x8, expected);
    assertEquals(expected, timer.readWord(0x8));
  }
  
  @Test
  public void testWrite_MATCH() throws AddressTrapException {
    Timer timer = new Timer();
    int expected = 0x5a5aa5a5;
    timer.writeWord(0xc, expected);
    assertEquals(expected, timer.readWord(0xc));
  }
  
  @Test
  public void testWrite_IE() throws AddressTrapException {
    Timer timer = new Timer();
    int expected = 0x00000003;
    timer.writeWord(0x10, expected);
    assertEquals(expected, timer.readWord(0x10));
  }
  
  @Test
  public void testWrite_IA() throws AddressTrapException {
    Timer timer = new Timer();
    assertEquals(0x00000000, timer.readWord(0x14));
    // arrange for an overflow interrupt to be pending
    timer.setCounter(0xFFFFFFFF);
    timer.setMatch(0xEEEEEEEE);
    timer.setTimerStart(true);
    timer.cycle();
    assertEquals(0x00000001, timer.readWord(0x14));
    timer.writeWord(0x18, 0x00000001);
    assertEquals(0x00000000, timer.readWord(0x14));
    // now arrange for a match interrupt to be pending
    timer.setMatch(0x00000001);
    timer.cycle();
    assertEquals(0x00000002, timer.readWord(0x14));
    timer.writeWord(0x18, 0x00000002);
    assertEquals(0x00000000, timer.readWord(0x14));
  }
  
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
  
  @Test
  public void testCounter_Reload() {
    int reload_expected = 0x5a5aa5a5;
    Timer timer = new Timer();
    timer.setCounter(0xFFFFFFFF);
    timer.setAutoReloadEnable(true);
    timer.setReload(reload_expected);
    timer.setTimerStart(true);
    timer.cycle();
    assertEquals(reload_expected, timer.getCounter());
  }
  
  @Test
  public void testCounter_NoReload() {
    Timer timer = new Timer();
    timer.setCounter(0xFFFFFFFF);
    timer.setAutoReloadEnable(false);
    timer.setTimerStart(true);
    timer.cycle();
    assertEquals(0, timer.getCounter());
  }
  
  @Test
  public void testMatchInterrupt() {
    Timer timer = new Timer();
    timer.setCounter(0x7FFFFFFF);
    timer.setMatch(0x80000000);
    timer.setTimerStart(true);
    assertFalse(timer.getMatchInterruptAsserted());
    timer.cycle();
    assertTrue(timer.getMatchInterruptAsserted());
  }
  
}
