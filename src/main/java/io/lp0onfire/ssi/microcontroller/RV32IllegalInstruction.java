package io.lp0onfire.ssi.microcontroller;

public class RV32IllegalInstruction extends RV32Instruction {

  public RV32IllegalInstruction(int insn) {
    super(insn);
  }
  
  @Override
  public void execute(RV32Core cpu) throws IllegalInstructionException {
    throw new IllegalInstructionException(getInsn());
  }

}
