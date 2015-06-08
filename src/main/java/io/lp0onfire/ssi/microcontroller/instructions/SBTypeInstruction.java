package io.lp0onfire.ssi.microcontroller.instructions;

import io.lp0onfire.ssi.microcontroller.RV32Instruction;

public abstract class SBTypeInstruction extends RV32Instruction {
  protected final int imm;
  public int getImm() {
    return this.imm;
  }
  protected final int rs2;
  public int getRs2() {
    return this.rs2;
  }
  protected final int rs1;
  public int getRs1() {
    return this.rs1;
  }
  protected final int funct3;
  public int getFunct3() {
    return this.funct3;
  }
  protected final int opcode;
  public int getOpcode() {
    return this.opcode;
  }
 
  public SBTypeInstruction(int instruction) {
    // immediate is split over four fields
    imm = (instruction & 0b10000000000000000000000000000000) >> (31-12) 
        | (instruction & 0b01111110000000000000000000000000) >>> (25-5)
        | (instruction & 0b00000000000000000000111100000000) >>> (8-1)
        | (instruction & 0b00000000000000000000000010000000) << (11-7);
    rs2 = (instruction & 0b00000001111100000000000000000000) >>> 20;
    rs1 = (instruction & 0b00000000000011111000000000000000) >>> 15;
    funct3 = (instruction & 0b00000000000000000111000000000000) >>> 12;
    opcode = (instruction & 0b00000000000000000000000001111111);
  }
}
