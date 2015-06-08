package io.lp0onfire.ssi.microcontroller.instructions;

import static org.junit.Assert.assertEquals;
import io.lp0onfire.ssi.microcontroller.RV32Core;

import org.junit.Test;

public class TestRTypeInstruction {
  
  class BogusInstruction extends RTypeInstruction {

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
    // decode SRA instruction:
    // 0100000 10111 00110 101 01100 0110011
    RTypeInstruction insn = new BogusInstruction(0b01000001011100110101011000110011);
    assertEquals(0b0100000, insn.getFunct7());
    assertEquals(0b10111, insn.getRs2());
    assertEquals(0b00110, insn.getRs1());
    assertEquals(0b101, insn.getFunct3());
    assertEquals(0b01100, insn.getRd());
    assertEquals(0b0110011, insn.getOpcode());
  }
  
}
