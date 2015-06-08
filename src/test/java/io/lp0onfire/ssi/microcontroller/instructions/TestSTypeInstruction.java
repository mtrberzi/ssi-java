package io.lp0onfire.ssi.microcontroller.instructions;

import static org.junit.Assert.assertEquals;
import io.lp0onfire.ssi.microcontroller.RV32Core;

import org.junit.Test;

public class TestSTypeInstruction {

  class BogusInstruction extends STypeInstruction {

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
    // decode SW
    // 1111111 01100 00110 010 11111 0100011
    STypeInstruction insn = new BogusInstruction(0b11111110110000110010111110100011);
    assertEquals(-1, insn.getImm());
    assertEquals(0b01100, insn.getRs2());
    assertEquals(0b00110, insn.getRs1());
    assertEquals(0b010, insn.getFunct3());
    assertEquals(0b0100011, insn.getOpcode());
  }
  
}
