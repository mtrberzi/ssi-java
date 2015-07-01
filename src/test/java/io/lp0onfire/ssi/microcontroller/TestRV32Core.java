package io.lp0onfire.ssi.microcontroller;

import static org.junit.Assert.*;
import io.lp0onfire.ssi.microcontroller.instructions.*;

import org.junit.Test;

public class TestRV32Core {

  class TestPeripheral implements SystemBusPeripheral {
    
    @Override
    public int getNumberOfPages() {
      return 1;
    }

    @Override
    public int readByte(int address) throws AddressTrapException {
      return 0x042;
    }

    @Override
    public int readHalfword(int address) throws AddressTrapException {
      return 0x02142;
    }

    @Override
    public int readWord(int address) throws AddressTrapException {
      return 0xabadd00d;
    }

    @Override
    public void writeByte(int address, int value) throws AddressTrapException {
    }

    @Override
    public void writeHalfword(int address, int value) throws AddressTrapException {
    }

    @Override
    public void writeWord(int address, int value) throws AddressTrapException {
    }
    
    @Override
    public void cycle() {
    }
    
  }
  
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
  
  @Test
  public void testExecuteJALR() {
    // load x1 with 0x76543210
    // set the program counter to 0xFEDCBA98
    // and JALR x2, 0(x1)
    // then check that next_pc = 0x76543210
    // and x2 = 0xFEDCBA98 + 4
    
    RV32_JALR insn = new RV32_JALR(0b00000000000000001000000101100111);
    RV32Core cpu = new RV32Core();
    cpu.setXRegister(1, 0x76543210);
    cpu.setPC(0xFEDCBA98);
    cpu.execute(insn);
    assertEquals(0x76543210, cpu.getNextPC());
    assertEquals(0xFEDCBA98 + 4, cpu.getXRegister(2));    
  }
  
  @Test
  public void testExecuteBLT_Taken() {
    // load x10 with -1, load x11 with 1
    // and BLT x11, x10, +8
    // since the branch is taken next_pc should be 8
    RV32_BLT insn = new RV32_BLT(0x00b54463);
    RV32Core cpu = new RV32Core();
    cpu.setXRegister(10, -1);
    cpu.setXRegister(11, 1);
    cpu.execute(insn);
    assertEquals(8, cpu.getNextPC());
  }
  
  @Test
  public void testExecuteBLT_NotTaken() {
    // load x10 with 1, load x11 with -1
    // and BLT x11, x10, +8
    // since the branch is not taken, next_pc should not be 8
    RV32_BLT insn = new RV32_BLT(0x00b54463);
    RV32Core cpu = new RV32Core();
    cpu.setXRegister(10, 1);
    cpu.setXRegister(11, -1);
    cpu.execute(insn);
    assertNotEquals(8, cpu.getNextPC());
  }
  
  @Test
  public void testExecuteBLTU_Taken() {
    // bltu x11, x10, 8
    // since the branch is taken next_pc should be 8
    RV32_BLTU insn = new RV32_BLTU(0x00a5e463);
    RV32Core cpu = new RV32Core();
    cpu.setXRegister(10, -1);
    cpu.setXRegister(11, 1);
    cpu.execute(insn);
    assertEquals(8, cpu.getNextPC());
  }
  
  @Test
  public void testExecuteBLTU_NotTaken() {
    // bltu x11, x10, 8
    // since the branch is not taken, next_pc should not be 8
    RV32_BLTU insn = new RV32_BLTU(0x00a5e463);
    RV32Core cpu = new RV32Core();
    cpu.setXRegister(10, 1);
    cpu.setXRegister(11, -1);
    cpu.execute(insn);
    assertNotEquals(8, cpu.getNextPC());
  }
  
  @Test
  public void testExecuteLW() {
    // lw a0, 0(t0)
    RV32_LW insn = new RV32_LW(0x0002a503);
    RV32Core cpu = new RV32Core();
    cpu.getSystemBus().attachPeripheral(new TestPeripheral(), 0x10000000);
    cpu.setXRegister(5, 0x10000000);
    cpu.execute(insn);
    assertEquals(0xabadd00d, cpu.getXRegister(10));
  }
  
  @Test
  public void testExecuteCSRRW() throws IllegalInstructionException {
    // preload scratch register, RMW it, check the read and written values
    // scratch is 0x340
    // csrrw x2, 0x340, x1
    RV32_CSRRW insn = new RV32_CSRRW(0x34009173);
    RV32Core cpu = new RV32Core();
    int initialValue = 0xDEADBEEF;
    int updatedValue = 0xABADD00D;
    cpu.writeCSR(0x340, initialValue);
    cpu.setXRegister(1, updatedValue);
    cpu.execute(insn);
    assertEquals(initialValue, cpu.getXRegister(2));
    assertEquals(updatedValue, cpu.readCSR(0x340));
  }
  
  @Test
  public void testExecuteLR_SC() {
    // lr.w x2, (x1)
    // sc.w x3, x0, (x1)
    RV32_LRW lr = new RV32_LRW(0x1000a12f);
    RV32_SCW sc = new RV32_SCW(0x1800a1af);
    RV32Core cpu = new RV32Core();
    cpu.getSystemBus().attachPeripheral(new TestPeripheral(), 0x10000000);
    cpu.setXRegister(1, 0x10000000);
    cpu.setXRegister(3, -1);
    cpu.execute(lr);
    assertEquals(0xabadd00d, cpu.getXRegister(2));
    assertTrue(cpu.getSystemBus().isReserved(0x10000000));
    cpu.execute(sc);
    assertEquals(0, cpu.getXRegister(3));
    assertFalse(cpu.getSystemBus().isReserved(0x10000000));
  }
  
  @Test
  public void testExecuteSCALL() {
    RV32_SCALL insn = new RV32_SCALL(0b00000000000000000000000001110011);
    RV32Core cpu = new RV32Core();
    cpu.setPC(0xabcd1234);
    cpu.execute(insn);
    // check that we have entered the "trap from machine mode" handler
    assertEquals(0x000001C0, cpu.getPC());
    // check that mepc = the last PC when we executed this instruction
    assertEquals(0xabcd1234, cpu.mepc);
    // check that mcause = 11 (envcall from M-mode)
    assertEquals(11, cpu.mcause);
    // check that mstatus[0] = 0 (interrupts disabled)
    assertEquals(false, cpu.mstatus_ie);
  }
  
  @Test
  public void testExecuteSBREAK() {
    RV32_SBREAK insn = new RV32_SBREAK(0b00000000000100000000000001110011);
    RV32Core cpu = new RV32Core();
    cpu.setPC(0xabcd1234);
    cpu.execute(insn);
    // check that we have entered the "trap from machine mode" handler
    assertEquals(0x000001C0, cpu.getPC());
    // check that mepc = the last PC when we executed this instruction
    assertEquals(0xabcd1234, cpu.mepc);
    // check that mcause = 3 (breakpoint)
    assertEquals(3, cpu.mcause);
    // check that mstatus[0] = 0 (interrupts disabled)
    assertEquals(false, cpu.mstatus_ie);
  }
  
  @Test
  public void testExecuteERET() {
    RV32_ERET insn = new RV32_ERET(0b00010000000000000000000001110011);
    RV32Core cpu = new RV32Core();
    cpu.mepc = 0xabcd1234;
    cpu.execute(insn);
    assertEquals(0xabcd1234, cpu.mepc);
  }
  
}
