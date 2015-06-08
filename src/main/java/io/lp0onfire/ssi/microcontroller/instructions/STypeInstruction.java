package io.lp0onfire.ssi.microcontroller.instructions;

import io.lp0onfire.ssi.microcontroller.RV32Instruction;

public abstract class STypeInstruction extends RV32Instruction {

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
 
  public STypeInstruction(int instruction) {
    // immediate is split over two fields
    imm = (instruction & 0b11111110000000000000000000000000) >> (25-5) 
        | (instruction & 0b00000000000000000000111110000000) >>> 7;
    rs2 = (instruction & 0b00000001111100000000000000000000) >>> 20;
    rs1 = (instruction & 0b00000000000011111000000000000000) >>> 15;
    funct3 = (instruction & 0b00000000000000000111000000000000) >>> 12;
    opcode = (instruction & 0b00000000000000000000000001111111);
  }
  
}
