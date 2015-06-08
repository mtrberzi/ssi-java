package io.lp0onfire.ssi.microcontroller.instructions;

import static org.junit.Assert.assertEquals;
import io.lp0onfire.ssi.microcontroller.RV32Core;

import org.junit.Test;

public class TestSBTypeInstruction {

  class BogusInstruction extends SBTypeInstruction {

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
    // decode something like SW but with an SB-type instruction
    // 1 011000 01100 00110 010 1100 1 0100011
    SBTypeInstruction insn = new BogusInstruction(0b10110000110000110010110010100011);
    assertEquals(-1256, insn.getImm());
    assertEquals(0b01100, insn.getRs2());
    assertEquals(0b00110, insn.getRs1());
    assertEquals(0b010, insn.getFunct3());
    assertEquals(0b0100011, insn.getOpcode());
  }
  
}
