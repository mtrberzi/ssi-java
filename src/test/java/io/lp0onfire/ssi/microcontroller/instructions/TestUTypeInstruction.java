package io.lp0onfire.ssi.microcontroller.instructions;

import static org.junit.Assert.assertEquals;
import io.lp0onfire.ssi.microcontroller.RV32Core;

import org.junit.Test;

public class TestUTypeInstruction {

  class BogusInstruction extends UTypeInstruction {

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
    // 11110000111100001111 01100 0100011
    UTypeInstruction insn = new BogusInstruction(0b11110000111100001111011000100011);
    assertEquals(-252645376, insn.getImm());
    assertEquals(0b01100, insn.getRd());
    assertEquals(0b0100011, insn.getOpcode());
  }
  
}
