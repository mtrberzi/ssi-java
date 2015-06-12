package io.lp0onfire.ssi.microcontroller;

public abstract class RV32Instruction {

  protected final int insn;
  public int getInsn() {
    return this.insn;
  }
  
  public RV32Instruction(int insn) {
    this.insn = insn;
  }
  
  public abstract void execute(RV32Core cpu) throws IllegalInstructionException;
  
}
