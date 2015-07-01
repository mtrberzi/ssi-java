package io.lp0onfire.ssi.microcontroller.peripherals;

import static org.junit.Assert.assertEquals;
import io.lp0onfire.ssi.microcontroller.AddressTrapException;
import io.lp0onfire.ssi.microcontroller.IllegalInstructionException;
import io.lp0onfire.ssi.microcontroller.RV32Core;

import org.junit.Before;
import org.junit.Test;

public class TestInterruptController {

  class DebugCore extends RV32Core {
    @Override
    public void writeCSR(int csr, int value) throws IllegalInstructionException {
      super.writeCSR(csr, value);
    }
  }
  
  private DebugCore cpu;
  private InterruptController pic;
  
  @Before
  public void setup() {
    cpu = new DebugCore();
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
  
  @Test
  public void testReadWord_MIS_Enabled() throws AddressTrapException {
    pic.setMasterEnable(true);
    int result = pic.readWord(0xEA001000);
    assertEquals(0x80000000, result & 0x80000000);
  }
  
  @Test
  public void testReadWord_MIS_Disabled() throws AddressTrapException {
    pic.setMasterEnable(false);
    int result = pic.readWord(0xEA001000);
    assertEquals(0x00000000, result & 0x80000000);
  }
  
  @Test
  public void testReadWord_MIS_CurrentInterrupt() throws AddressTrapException, IllegalInstructionException {
    pic.setMasterEnable(true);
    pic.setInterruptEnabled(2, true);
    pic.assertInterrupt(2);
    assertEquals("precondition failed", 2, pic.nextInterrupt());
    // enable interrupts
    cpu.writeCSR(0x300, 0x00000001);
    pic.cycle();
    int result = pic.readWord(0xEA001000);
    assertEquals(2, result & 0x0000001F);
  }
  
  @Test
  public void testReadWord_IER_Enabled() throws AddressTrapException {
    pic.setInterruptEnabled(2, true);
    int result = pic.readWord(0xEA001004);
    assertEquals(0x00000004, result & 0x00000004);
  }
  
  @Test
  public void testReadWord_IER_Disabled() throws AddressTrapException {
    pic.setInterruptEnabled(2, false);
    int result = pic.readWord(0xEA001004);
    assertEquals(0x00000000, result & 0x00000004);
  }
  
  @Test
  public void testReadWord_IPR_Enabled() throws AddressTrapException {
    pic.assertInterrupt(2);
    int result = pic.readWord(0xEA001008);
    assertEquals(0x00000004, result & 0x00000004);
  }
  
  @Test
  public void testReadWord_IPR_Disabled() throws AddressTrapException {
    int result = pic.readWord(0xEA001008);
    assertEquals(0x00000000, result & 0x00000004);
  }
  
  @Test
  public void testReadWord_InterruptPriority() throws AddressTrapException {
    pic.setInterruptPriority(2, 30);
    int result = pic.readWord(0xEA001018);
    assertEquals(30, result);
  }
  
  @Test
  public void testWriteWord_MIS_Enable() throws AddressTrapException {
    pic.setMasterEnable(false);
    pic.writeWord(0xEA001000, 0x80000000);
    assertEquals(true, pic.getMasterEnable());
  }
  
  @Test
  public void testWriteWord_MIS_Disable() throws AddressTrapException {
    pic.setMasterEnable(true);
    pic.writeWord(0xEA001000, 0x00000000);
    assertEquals(false, pic.getMasterEnable());
  }
  
  @Test
  public void testWriteWord_IER_Enable() throws AddressTrapException {
    pic.setInterruptEnabled(2, false);
    pic.writeWord(0xEA001004, 0x00000004);
    assertEquals(true, pic.getInterruptEnabled(2));
  }
  
  @Test
  public void testWriteWord_IER_Disable() throws AddressTrapException {
    pic.setInterruptEnabled(2, true);
    pic.writeWord(0xEA001004, 0xFFFFFFFB);
    assertEquals(false, pic.getInterruptEnabled(2));
  }
  
  @Test
  public void testWriteWord_IAR_Acknowledge() throws AddressTrapException {
    pic.setMasterEnable(true);
    pic.setInterruptEnabled(2, true);
    pic.assertInterrupt(2);
    assertEquals("precondition failed", 2, pic.nextInterrupt());
    pic.writeWord(0xEA00100C, 0x00000004);
    assertEquals(-1, pic.nextInterrupt());
  }
  
  @Test
  public void testWriteWord_IAR_MissedAcknowledge() throws AddressTrapException {
    pic.setMasterEnable(true);
    pic.setInterruptEnabled(2, true);
    pic.assertInterrupt(2);
    assertEquals("precondition failed", 2, pic.nextInterrupt());
    pic.writeWord(0xEA00100C, 0x00000001);
    assertEquals(2, pic.nextInterrupt());
  }
  
  @Test
  public void testWriteWord_InterruptPriority() throws AddressTrapException {
    pic.setInterruptPriority(2, 0);
    pic.writeWord(0xEA001018, 30);
    assertEquals(30, pic.getInterruptPriority(2));
  }
  
}
