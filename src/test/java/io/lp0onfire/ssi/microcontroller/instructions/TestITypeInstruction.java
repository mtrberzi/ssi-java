package io.lp0onfire.ssi.microcontroller.instructions;

import static org.junit.Assert.assertEquals;
import io.lp0onfire.ssi.microcontroller.RV32Core;

import org.junit.Test;

public class TestITypeInstruction {

  class BogusInstruction extends ITypeInstruction {

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
    // decode ADDI
    // 111111111111 00110 000 01100 0010011
    ITypeInstruction insn = new BogusInstruction(0b11111111111100110000011000010011);
    assertEquals(-1, insn.getImm());
    assertEquals(0b00110, insn.getRs1());
    assertEquals(0b000, insn.getFunct3());
    assertEquals(0b01100, insn.getRd());
    assertEquals(0b0010011, insn.getOpcode());
  }
  
}
