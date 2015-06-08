package io.lp0onfire.ssi.microcontroller.instructions;

import static org.junit.Assert.assertEquals;
import io.lp0onfire.ssi.microcontroller.RV32Core;

import org.junit.Test;

public class TestUJTypeInstruction {

  class BogusInstruction extends UJTypeInstruction {

    public BogusInstruction(int instruction) {
      super(instruction);
    }

    @Override
    public void execute(RV32Core cpu) {
      throw new UnsupportedOperationException();
    }
  }
  
  @Test
  public void testDecode() {
    // immediate is 1 01100010 1 0111000111
    // encoded as 1 0111000111 1 01100010
    // + 01100 0100011
    UJTypeInstruction insn = new BogusInstruction(0b10111000111101100010011000100011);
    assertEquals(-644210, insn.getImm());
    assertEquals(0b01100, insn.getRd());
    assertEquals(0b0100011, insn.getOpcode());
  }
  
}
