package io.lp0onfire.ssi.microcontroller.instructions;

import io.lp0onfire.ssi.microcontroller.IllegalInstructionException;
import io.lp0onfire.ssi.microcontroller.RV32Core;

public class RV32_SRAI extends RTypeInstruction {

  private final int shamt;
  public int getShamt() {
    return this.shamt;
  }
  
  public RV32_SRAI(int instruction) {
    super(instruction);
    this.shamt = (instruction & 0b00000001111100000000000000000000) >>> 20;
  }

  @Override
  public void execute(RV32Core cpu) throws IllegalInstructionException {
    cpu.execute(this);
  }

}
