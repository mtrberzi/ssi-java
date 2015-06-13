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
  public void testExecuteADDI_PositiveImmediate() {
    // ADDI x1, x0, 1
    RV32_ADDI insn = new RV32_ADDI(0b00000000000100000000000010010011);
    RV32Core cpu = new RV32Core();
    cpu.execute(insn);
    assertEquals(1, cpu.getXRegister(1));
  }
  
  @Test
  public void testExecuteADDI_NegativeImmediate() {
    // ADDI x1, x0, -1
    RV32_ADDI insn = new RV32_ADDI(0b11111111111100000000000010010011);
    RV32Core cpu = new RV32Core();
    cpu.execute(insn);
    assertEquals(-1, cpu.getXRegister(1));
  }
  
  @Test
  public void testExecuteANDI() {
    // load x1 with 0xFF000F0F
    // and AND it with 0x000000FF
    // to get 0x0000000F
    // so ANDI x1, x1, 0x0FF
    RV32_ANDI insn = new RV32_ANDI(0b00001111111100001111000010010011);
    RV32Core cpu = new RV32Core();
    cpu.setXRegister(1, 0xFF000F0F);
    cpu.execute(insn);
    assertEquals(0x0000000F, cpu.getXRegister(1));
  }
  
  @Test
  public void testExecuteORI() {
    // load x1 with 0xFF000F0F
    // and OR it with 0x0FF
    // to get 0xFF000FFF
    // so ORI x1, x1, 0x0FF
    RV32_ORI insn = new RV32_ORI(0b00001111111100001110000010010011);
    RV32Core cpu = new RV32Core();
    cpu.setXRegister(1, 0xFF000F0F);
    cpu.execute(insn);
    assertEquals(0xFF000FFF, cpu.getXRegister(1));
  }
  
  @Test
  public void testExecuteSLLI() {
    // load x1 with 0x000000FF
    // and shift left by 8
    // to get 0x0000FF00
    // so SLLI x1, x1, 8
    RV32_SLLI insn = new RV32_SLLI(0b00000000100000001001000010010011);
    RV32Core cpu = new RV32Core();
    cpu.setXRegister(1, 0x000000FF);
    cpu.execute(insn);
    assertEquals(0x0000FF00, cpu.getXRegister(1));
  }
  
  @Test
  public void testExecuteSLTI_WritesOne() {
    // load -5 into x1 and compare it to -1
    // should place 1 into x2
    // so SLTI x2, x1, -1
    RV32_SLTI insn = new RV32_SLTI(0b11111111111100001010000100010011);
    RV32Core cpu = new RV32Core();
    cpu.setXRegister(1, -5);
    cpu.execute(insn);
    assertEquals(1, cpu.getXRegister(2));
  }
  
  @Test
  public void testExecuteSLTI_WritesZero() {
    // load 3 into x1 and compare it to -1
    // should place 0 into x2
    // so SLTI x2, x1, -1
    RV32_SLTI insn = new RV32_SLTI(0b11111111111100001010000100010011);
    RV32Core cpu = new RV32Core();
    cpu.setXRegister(1, 3);
    cpu.setXRegister(2, -50); // allows us to check that x2 actually gets written
    cpu.execute(insn);
    assertEquals(0, cpu.getXRegister(2));
  }
  
  @Test
  public void testExecuteSLTIU_CompareToZero() {
    // load -1 into x1 and compare it to 0
    // should place 0 into x2
    // so SLTIU x2, x1, 0
    RV32_SLTIU insn = new RV32_SLTIU(0b00000000000000001011000100010011);
    RV32Core cpu = new RV32Core();
    cpu.setXRegister(1, -1);
    cpu.setXRegister(2, -50);
    cpu.execute(insn);
    assertEquals(0, cpu.getXRegister(2));
  }
  
  @Test
  public void testExecuteSRAI() {
    // load x1 with 0xFF000000
    // and shift right by 8
    // to get 0xFFFF0000
    // so SRLI x1, x1, 8
    RV32_SRAI insn = new RV32_SRAI(0b01000000100000001101000010010011);
    RV32Core cpu = new RV32Core();
    cpu.setXRegister(1, 0xFF000000);
    cpu.execute(insn);
    assertEquals(0xFFFF0000, cpu.getXRegister(1));
  }
  
  @Test
  public void testExecuteSRLI() {
    // load x1 with 0x0000FF00
    // and shift right by 8
    // to get 0x000000FF
    // so SRLI x1, x1, 8
    RV32_SRLI insn = new RV32_SRLI(0b00000000100000001101000010010011);
    RV32Core cpu = new RV32Core();
    cpu.setXRegister(1, 0x0000FF00);
    cpu.execute(insn);
    assertEquals(0x000000FF, cpu.getXRegister(1));
  }
  
  @Test
  public void testExecuteXORI() {
    // load x1 with 0xFF00F00F
    // and XOR it with 0x0FF
    // to get 0xFF00F0F0
    // so XORI x1, x1, 0x0FF
    RV32_XORI insn = new RV32_XORI(0b00001111111100001100000010010011);
    RV32Core cpu = new RV32Core();
    cpu.setXRegister(1, 0xFF00F00F);
    cpu.execute(insn);
    assertEquals(0xFF00F0F0, cpu.getXRegister(1));
  }
  
}
