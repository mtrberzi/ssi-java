package io.lp0onfire.ssi.microcontroller;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestRV32Core {

  @Test
  public void testReadAfterWrite() {
    RV32Core cpu = new RV32Core();
    for (int i = 1; i < 32; ++i) {
      cpu.setXRegister(i, i);
      int actual = cpu.getXRegister(i);
      assertEquals("register " + Integer.toString(i) + " broken", i, actual);
    }
  }
  
  @Test
  public void testX0_Read_EqualsZero() {
    RV32Core cpu = new RV32Core();
    int x0 = cpu.getXRegister(0);
    assertEquals(0, x0);
  }
  
  @Test
  public void testX0_ReadAfterWrite_EqualsZero() {
    RV32Core cpu = new RV32Core();
    cpu.setXRegister(0, 9001);
    int x0 = cpu.getXRegister(0);
    assertEquals(0, x0);
  }
  
}
