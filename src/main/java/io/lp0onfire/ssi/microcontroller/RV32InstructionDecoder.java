package io.lp0onfire.ssi.microcontroller;

import io.lp0onfire.ssi.microcontroller.instructions.*;

public class RV32InstructionDecoder {

  public RV32Instruction decode(int insn) {
    // Table 8.1 gives us a quick way to decode most of an instruction:
    // check that bits [1:0] are "11", then 
    // check bits [6:2] of the instruction word 
    // and jump to an appropriate handler
    
    if ((insn & 0x00000003) != 3) {
      // bits [1:0] not "11", this is not an RV32 base opcode
      return new RV32IllegalInstruction(insn);
    }
    
    int opcodeTag = (insn & 0x0000007C) >>> 2;
    
    switch (opcodeTag) {
    // 0: LOAD
    // 1: LOAD-FP
    // 2: custom-0
    // 3: MISC-MEM
    // 4: OP-IMM
    case 0b00100:
      return decode_OP_IMM(insn);
    // 5: AUIPC
    // 6: OP-IMM-32
    // 7: 48b
    // 8: STORE
    // 9: STORE-FP
    // 10: custom-1
    // 11: AMO
    // 12: OP
    // 13: LUI
    // 14: OP-32
    // 15: 64b
    // 16: MADD
    // 17: MSUB
    // 18: NMSUB
    // 19: NMADD
    // 20: OP-FP
    // 21: reserved
    // 22: custom-2
    // 23: 48b
    // 24: BRANCH
    // 25: JALR
    // 26: reserved
    // 27: JAL
    // 28: SYSTEM
    // 29: reserved
    // 30: custom-3
    // 31: >= 80b
    default:
      // TODO
      return new RV32IllegalInstruction(insn);
    }
  }
  
  private RV32Instruction decode_OP_IMM(int insn) {
    // opcode = 0010011
    // now decode funct3
    int funct3 = (insn & 0b00000000000000000111000000000000) >>> 12;
    switch (funct3) {
    case 0b000:
      return new RV32_ADDI(insn);
    case 0b001:
    {
      // decode funct7
      int funct7 = (insn & 0b11111110000000000000000000000000) >>> 25;
      if (funct7 == 0b0000000) {
        return new RV32_SLLI(insn);
      } else {
        return new RV32IllegalInstruction(insn);
      }
    }
    case 0b010:
      return new RV32_SLTI(insn);
    case 0b011:
      return new RV32_SLTIU(insn);
    case 0b100:
      return new RV32_XORI(insn);
    case 0b101:
    {
      // decode funct7
      int funct7 = (insn & 0b11111110000000000000000000000000) >>> 25;
      if (funct7 == 0b0000000) {
        return new RV32_SRLI(insn);
      } else if (funct7 == 0b0100000) {
        return new RV32_SRAI(insn);
      } else {
        return new RV32IllegalInstruction(insn);
      }
    }
    case 0b110:
      return new RV32_ORI(insn);
    case 0b111:
      return new RV32_ANDI(insn);
    default:
      // TODO
      return new RV32IllegalInstruction(insn);
    }
  }
  
}
