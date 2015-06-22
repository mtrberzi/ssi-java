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
    case 0b00000:
      return decode_LOAD(insn);
    // 1: LOAD-FP
    // 2: custom-0
    // 3: MISC-MEM
    // 4: OP-IMM
    case 0b00100:
      return decode_OP_IMM(insn);
    // 5: AUIPC
    case 0b00101:
      return new RV32_AUIPC(insn);
    // 6: OP-IMM-32
    // 7: 48b
    // 8: STORE
    case 0b01000:
      return decode_STORE(insn);
    // 9: STORE-FP
    // 10: custom-1
    // 11: AMO
    case 0b01011:
      return decode_AMO(insn);
    // 12: OP
    case 0b01100:
      return decode_OP(insn);
    // 13: LUI
    case 0b01101:
      return new RV32_LUI(insn);
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
    case 0b11000:
      return decode_BRANCH(insn);
    // 25: JALR
    case 0b11001:
      return new RV32_JALR(insn);
    // 26: reserved
    // 27: JAL
    case 0b11011:
      return new RV32_JAL(insn);
    // 28: SYSTEM
    case 0b11100:
      return decode_SYSTEM(insn);
    // 29: reserved
    // 30: custom-3
    // 31: >= 80b
    default:
      // TODO
      return new RV32IllegalInstruction(insn);
    }
  }
  
  private RV32Instruction decode_LOAD(int insn) {
    // opcode = 0000011
    // now decode funct3
    int funct3 = (insn & 0b00000000000000000111000000000000) >>> 12;
    switch (funct3) {
    case 0b000:
      return new RV32_LB(insn);
    case 0b001:
      return new RV32_LH(insn);
    case 0b010:
      return new RV32_LW(insn);
    case 0b100:
      return new RV32_LBU(insn);
    case 0b101:
      return new RV32_LHU(insn);
    default:
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
      return new RV32IllegalInstruction(insn);
    }
  }
  
  private RV32Instruction decode_STORE(int insn) {
    // opcode = 0100011
    // now decode funct3
    int funct3 = (insn & 0b00000000000000000111000000000000) >>> 12;
    switch (funct3) {
    case 0b000:
      return new RV32_SB(insn);
    case 0b001:
      return new RV32_SH(insn);
    case 0b010:
      return new RV32_SW(insn);
    default:
      return new RV32IllegalInstruction(insn);
    }
  }
  
  private RV32Instruction decode_AMO(int insn) {
    // opcode = 0101111
    int funct7 = (insn & 0b11111110000000000000000000000000) >>> 25;
    int funct3 = (insn & 0b00000000000000000111000000000000) >>> 12;
    // check funct3 = 010
    if (funct3 == 0b010) {
      // decode the 5 highest bits of funct7
      int amofunct = funct7 >>> 2;
      switch (amofunct) {
      case 0b00010: return new RV32_LRW(insn);
      case 0b00011: return new RV32_SCW(insn);
      case 0b00001: return new RV32_AMOSWAPW(insn);
      case 0b00000: return new RV32_AMOADDW(insn);
      case 0b00100: return new RV32_AMOXORW(insn);
      case 0b01100: return new RV32_AMOANDW(insn);
      case 0b01000: return new RV32_AMOORW(insn);
      case 0b10000: return new RV32_AMOMINW(insn);
      case 0b10100: return new RV32_AMOMAXW(insn);
      case 0b11000: return new RV32_AMOMINUW(insn);
      case 0b11100: return new RV32_AMOMAXUW(insn);
      default:
        return new RV32IllegalInstruction(insn);
      }
    } else {
      return new RV32IllegalInstruction(insn);
    }
  }
  
  private RV32Instruction decode_OP(int insn) {
    // opcode = 0110011
    // first decode funct7 as MUL/DIV have a different prefix
    int funct7 = (insn & 0b11111110000000000000000000000000) >>> 25;
    int funct3 = (insn & 0b00000000000000000111000000000000) >>> 12;
    switch (funct7) {
    case 0b0000000:
      // standard integer ops
    {
      switch (funct3) {
      case 0b000:
        return new RV32_ADD(insn);
      case 0b001:
        return new RV32_SLL(insn);
      case 0b010:
        return new RV32_SLT(insn);
      case 0b011:
        return new RV32_SLTU(insn);
      case 0b100:
        return new RV32_XOR(insn);
      case 0b101:
        return new RV32_SRL(insn);
      case 0b110:
        return new RV32_OR(insn);
      case 0b111:
        return new RV32_AND(insn);
      default:
        return new RV32IllegalInstruction(insn);
      }
    }
    case 0b0000001:
      // MUL/DIV
    {
      switch (funct3) {
      case 0b000: 
        return new RV32_MUL(insn);
      case 0b001: 
        return new RV32_MULH(insn);
      case 0b010: 
        return new RV32_MULHSU(insn);
      case 0b011: 
        return new RV32_MULHU(insn);
      case 0b100: 
        return new RV32_DIV(insn);
      case 0b101: 
        return new RV32_DIVU(insn);
      case 0b110: 
        return new RV32_REM(insn);
      case 0b111: 
        return new RV32_REMU(insn);
      default:
        return new RV32IllegalInstruction(insn);
      }
    }
    case 0b0100000:
      // other standard integer ops
    {
      switch (funct3) {
      case 0b000:
        return new RV32_SUB(insn);
      case 0b101:
        return new RV32_SRA(insn);
        default:
          return new RV32IllegalInstruction(insn);
      }
    }
    default:
      return new RV32IllegalInstruction(insn);
    }
  }
  
  private RV32Instruction decode_BRANCH(int insn) {
    // opcode = 1100011
    // now decode funct3
    int funct3 = (insn & 0b00000000000000000111000000000000) >>> 12;
    switch (funct3) {
    case 0b000:
      return new RV32_BEQ(insn);
    case 0b001:
      return new RV32_BNE(insn);
    case 0b100:
      return new RV32_BLT(insn);
    case 0b101:
      return new RV32_BGE(insn);
    case 0b110:
      return new RV32_BLTU(insn);
    case 0b111:
      return new RV32_BGEU(insn);
    default:
      return new RV32IllegalInstruction(insn);
    }
  }
  
  private RV32Instruction decode_SYSTEM(int insn) {
    // opcode = 1110011
    // now decode funct3
    int funct3 = (insn & 0b00000000000000000111000000000000) >>> 12;
    switch (funct3) {
    case 0b000:
    {
      // decode imm (funct12)
      int imm = (insn & 0b11111111111100000000000000000000) >>> 20;
      switch (imm) {
      case 0b000000000000:
        return new RV32_SCALL(insn);
      case 0b000000000001:
        return new RV32_SBREAK(insn);
      case 0b000100000000:
        return new RV32_ERET(insn);
      default:
        return new RV32IllegalInstruction(insn);
      }
    }
    case 0b001: 
      return new RV32_CSRRW(insn);
    case 0b010:
      return new RV32_CSRRS(insn);
    case 0b011: 
      return new RV32_CSRRC(insn);
    case 0b101: 
      return new RV32_CSRRWI(insn);
    case 0b110: 
      return new RV32_CSRRSI(insn);
    case 0b111: 
      return new RV32_CSRRCI(insn);
    default:
      return new RV32IllegalInstruction(insn);
    }
  }
  
}
