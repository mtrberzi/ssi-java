package io.lp0onfire.ssi.microcontroller.instructions;

import io.lp0onfire.ssi.microcontroller.RV32Instruction;

public abstract class RTypeInstruction extends RV32Instruction {

  protected final int funct7;
  public int getFunct7() {
    return this.funct7;
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
  protected final int rd;
  public int getRd() {
    return this.rd;
  }
  protected final int opcode;
  public int getOpcode() {
    return this.opcode;
  }
  
  public RTypeInstruction(int instruction) {
    super(instruction);
    funct7 = (instruction & 0b11111110000000000000000000000000) >>> 25;
    rs2 = (instruction & 0b00000001111100000000000000000000) >>> 20;
    rs1 = (instruction & 0b00000000000011111000000000000000) >>> 15;
    funct3 = (instruction & 0b00000000000000000111000000000000) >>> 12;
    rd = (instruction & 0b00000000000000000000111110000000) >>> 7;
    opcode = (instruction & 0b00000000000000000000000001111111);
  }
  
}
