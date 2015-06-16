package io.lp0onfire.ssi.microcontroller;

import static org.junit.Assert.*;
import io.lp0onfire.ssi.microcontroller.instructions.*;

import org.junit.Test;

public class TestRV32InstructionDecoder {

  @Test
  public void testDecodeIllegalInstruction() {
    RV32InstructionDecoder dec = new RV32InstructionDecoder();
    RV32Instruction insn = dec.decode(0);
    assertTrue(insn instanceof RV32IllegalInstruction);
  }
  
  @Test
  public void testDecodeADDI() {
    RV32InstructionDecoder dec = new RV32InstructionDecoder();
    RV32Instruction insn = dec.decode(0b00000000000011111000111110010011);
    assertTrue(insn instanceof RV32_ADDI);
  }
  
  @Test
  public void testDecodeSLTI() {
    RV32InstructionDecoder dec = new RV32InstructionDecoder();
    RV32Instruction insn = dec.decode(0b00000000000011111010111110010011);
    assertTrue(insn instanceof RV32_SLTI);
  }
  
  @Test
  public void testDecodeSLTIU() {
    RV32InstructionDecoder dec = new RV32InstructionDecoder();
    RV32Instruction insn = dec.decode(0b00000000000011111011111110010011);
    assertTrue(insn instanceof RV32_SLTIU);
  }
  
  @Test
  public void testDecodeXORI() {
    RV32InstructionDecoder dec = new RV32InstructionDecoder();
    RV32Instruction insn = dec.decode(0b00000000000011111100111110010011);
    assertTrue(insn instanceof RV32_XORI);
  }
  
  @Test
  public void testDecodeORI() {
    RV32InstructionDecoder dec = new RV32InstructionDecoder();
    RV32Instruction insn = dec.decode(0b00000000000011111110111110010011);
    assertTrue(insn instanceof RV32_ORI);
  }
  
  @Test
  public void testDecodeANDI() {
    RV32InstructionDecoder dec = new RV32InstructionDecoder();
    RV32Instruction insn = dec.decode(0b00000000000011111111111110010011);
    assertTrue(insn instanceof RV32_ANDI);
  }
  
  @Test
  public void testDecodeSRLI() {
    RV32InstructionDecoder dec = new RV32InstructionDecoder();
    RV32Instruction insn = dec.decode(0b00000000000011111101111110010011);
    assertTrue(insn instanceof RV32_SRLI);
  }
  
  @Test
  public void testDecodeSRAI() {
    RV32InstructionDecoder dec = new RV32InstructionDecoder();
    RV32Instruction insn = dec.decode(0b01000000000011111101111110010011);
    assertTrue(insn instanceof RV32_SRAI);
  }
  
  @Test
  public void testDecodeSLLI() {
    RV32InstructionDecoder dec = new RV32InstructionDecoder();
    RV32Instruction insn = dec.decode(0b00000000000011111001111110010011);
    assertTrue(insn instanceof RV32_SLLI);
  }
  
  @Test
  public void testDecodeJALR() {
    RV32InstructionDecoder dec = new RV32InstructionDecoder();
    RV32Instruction insn = dec.decode(0x00008067);
    assertTrue(insn instanceof RV32_JALR);
  }
  
  @Test
  public void testDecodeBLT() {
    RV32InstructionDecoder dec = new RV32InstructionDecoder();
    RV32Instruction insn = dec.decode(0x00a5c463);
    assertTrue(insn instanceof RV32_BLT);
  }
  
  @Test
  public void testDecodeBLTU() {
    RV32InstructionDecoder dec = new RV32InstructionDecoder();
    RV32Instruction insn = dec.decode(0x00a5e463);
    assertTrue(insn instanceof RV32_BLTU);
  }
  
  @Test
  public void testDecodeLW() {
    RV32InstructionDecoder dec = new RV32InstructionDecoder();
    RV32Instruction insn = dec.decode(0x0002a503);
    assertTrue(insn instanceof RV32_LW);
    
  }
  
}
