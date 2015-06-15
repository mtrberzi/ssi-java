package io.lp0onfire.ssi.microcontroller.instructions;

import io.lp0onfire.ssi.microcontroller.RV32Instruction;

public abstract class UTypeInstruction extends RV32Instruction {

  protected final int imm;
  public int getImm() {
    return this.imm;
  }
  protected final int rd;
  public int getRd() {
    return this.rd;
  }
  protected final int opcode;
  public int getOpcode() {
    return this.opcode;
  }
  
  public UTypeInstruction(int instruction) {
    super(instruction);
    imm = (instruction & 0b11111111111111111111000000000000);
    // the lowest 12 bits of the immediate are taken to be zero
    rd = (instruction & 0b00000000000000000000111110000000) >>> 7;
    opcode = (instruction & 0b00000000000000000000000001111111);
  }
  
}
