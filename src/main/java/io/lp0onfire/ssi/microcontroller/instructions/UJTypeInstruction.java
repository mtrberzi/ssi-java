package io.lp0onfire.ssi.microcontroller.instructions;

import io.lp0onfire.ssi.microcontroller.RV32Instruction;

public abstract class UJTypeInstruction extends RV32Instruction {
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
  
  public UJTypeInstruction(int instruction) {
    super(instruction);
    // immediate is broken over effectively four fields
    // [20] [10:1] [11] [19:12]
    imm = (instruction & 0x80000000) >> (31-20) // [20]
        | (instruction & 0x000ff000)            // [19:12]
        | (instruction & 0x00100000) >> (20-11) // [11]
        | (instruction & 0x7fe00000) >> (21-1)  // [10:1]
        ;
    rd = (instruction & 0b00000000000000000000111110000000) >>> 7;
    opcode = (instruction & 0b00000000000000000000000001111111);
  }
}
