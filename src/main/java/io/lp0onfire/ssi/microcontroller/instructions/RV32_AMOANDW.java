package io.lp0onfire.ssi.microcontroller.instructions;

import io.lp0onfire.ssi.microcontroller.IllegalInstructionException;
import io.lp0onfire.ssi.microcontroller.RV32Core;

public class RV32_AMOANDW extends RTypeInstruction {

  public RV32_AMOANDW(int instruction) {
    super(instruction);
  }

  @Override
  public void execute(RV32Core cpu) throws IllegalInstructionException {
    cpu.execute(this);
  }

}
