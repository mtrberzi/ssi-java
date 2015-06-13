package io.lp0onfire.ssi.microcontroller;

import static org.junit.Assert.*;
import io.lp0onfire.ssi.microcontroller.instructions.*;

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
  
  @Test
  public void testExecuteADDI_PositiveImmediate() throws IllegalInstructionException {
    // ADDI x1, x0, 1
    RV32_ADDI insn = new RV32_ADDI(0b00000000000100000000000010010011);
    RV32Core cpu = new RV32Core();
    cpu.execute(insn);
    assertEquals(1, cpu.getXRegister(1));
  }
  
  @Test
  public void testExecuteADDI_NegativeImmediate() throws IllegalInstructionException {
    // ADDI x1, x0, -1
    RV32_ADDI insn = new RV32_ADDI(0b11111111111100000000000010010011);
    RV32Core cpu = new RV32Core();
    cpu.execute(insn);
    assertEquals(-1, cpu.getXRegister(1));
  }
  
  @Test
  public void testExecuteANDI() throws IllegalInstructionException {
    // load x1 with 0xFF000F0F
    // and mask it with 0x000000FF
    // to get 0x0000000F
    // so ANDI x1, x1, 0x0FF
    RV32_ANDI insn = new RV32_ANDI(0b00001111111100001111000010010011);
    RV32Core cpu = new RV32Core();
    cpu.setXRegister(1, 0xFF000F0F);
    cpu.execute(insn);
    assertEquals(0x0000000F, cpu.getXRegister(1));
  }
  
}
