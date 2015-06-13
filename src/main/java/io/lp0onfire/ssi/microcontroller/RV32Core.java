package io.lp0onfire.ssi.microcontroller;

import io.lp0onfire.ssi.microcontroller.instructions.*;

public class RV32Core {

  private int xRegister[] = new int[32];
  protected int getXRegister(int idx) {
    // x0 is hardwired to constant 0.
    if (idx == 0) {
      return 0;
    } else {
      return xRegister[idx];
    }
  }
  protected void setXRegister(int idx, int value) {
    // x0 is hardwired to constant 0, hence not writable.
    if (idx == 0) {
      return;
    } else {
      xRegister[idx] = value;
    }
  }
  
  private int pc = 0;
  protected int getPC() {
    return this.pc;
  }
  protected void setPC(int value) {
    this.pc = value;
  }
  
  private int next_pc = 0;
  protected int getNextPC() {
    return this.next_pc;
  }
  protected void setNextPC(int value) {
    this.next_pc = value;
  }
  
  public RV32Core() {
    for (int i = 0; i < 32; ++i) {
      xRegister[i] = 0;
    }
  }
  public void execute(RV32_ADD rv32_ADD) {
    int rs1 = getXRegister(rv32_ADD.getRs1());
    int rs2 = getXRegister(rv32_ADD.getRs2());
    setXRegister(rv32_ADD.getRd(), rs1 + rs2);
  }
  public void execute(RV32_ADDI rv32_ADDI) {
    int rs1 = getXRegister(rv32_ADDI.getRs1());
    setXRegister(rv32_ADDI.getRd(), rs1 + rv32_ADDI.getImm());
  }
  public void execute(RV32_AND rv32_AND) {
    int rs1 = getXRegister(rv32_AND.getRs1());
    int rs2 = getXRegister(rv32_AND.getRs2());
    setXRegister(rv32_AND.getRd(), rs1 & rs2);
  }
  public void execute(RV32_ANDI rv32_ANDI) {
    int rs1 = getXRegister(rv32_ANDI.getRs1());
    setXRegister(rv32_ANDI.getRd(), rs1 & rv32_ANDI.getImm());
  }
  public void execute(RV32_AUIPC rv32_AUIPC) {
    setXRegister(rv32_AUIPC.getRd(), getPC() + rv32_AUIPC.getImm());
  }
  public void execute(RV32_BEQ rv32_BEQ) {
    int rs1 = getXRegister(rv32_BEQ.getRs1());
    int rs2 = getXRegister(rv32_BEQ.getRs2());
    if (rs1 == rs2) {
      setNextPC(getPC() + rv32_BEQ.getImm());
    }
  }
  public void execute(RV32_BGE rv32_BGE) {
    int rs1 = getXRegister(rv32_BGE.getRs1());
    int rs2 = getXRegister(rv32_BGE.getRs2());
    if (rs1 >= rs2) {
      setNextPC(getPC() + rv32_BGE.getImm());
    }
  }
  public void execute(RV32_BGEU rv32_BGEU) {
    int rs1 = getXRegister(rv32_BGEU.getRs1());
    int rs2 = getXRegister(rv32_BGEU.getRs2());
    boolean isLT = (rs1 < rs2) ^ (rs1 < 0) ^ (rs2 < 0);
    if (!isLT) {
      setNextPC(getPC() + rv32_BGEU.getImm());
    }
  }
  public void execute(RV32_BLT rv32_BLT) {
    int rs1 = getXRegister(rv32_BLT.getRs1());
    int rs2 = getXRegister(rv32_BLT.getRs2());
    if (rs1 < rs2) {
      setNextPC(getPC() + rv32_BLT.getImm());
    }
  }
  public void execute(RV32_BLTU rv32_BLTU) {
    int rs1 = getXRegister(rv32_BLTU.getRs1());
    int rs2 = getXRegister(rv32_BLTU.getRs2());
    boolean isLT = (rs1 < rs2) ^ (rs1 < 0) ^ (rs2 < 0);
    if (isLT) {
      setNextPC(getPC() + rv32_BLTU.getImm());
    }
  }
  public void execute(RV32_BNE rv32_BNE) {
    int rs1 = getXRegister(rv32_BNE.getRs1());
    int rs2 = getXRegister(rv32_BNE.getRs2());
    if (rs1 != rs2) {
      setNextPC(getPC() + rv32_BNE.getImm());
    }
  }
  public void execute(RV32_JAL rv32_JAL) {
    int target = getPC() + rv32_JAL.getImm();
    setNextPC(target);
    setXRegister(rv32_JAL.getRd(), getPC() + 4);
  }
  public void execute(RV32_JALR rv32_JALR) {
    int target = (getXRegister(rv32_JALR.getRs1()) + rv32_JALR.getImm()) & ~(0x00000001);
    setNextPC(target);
    setXRegister(rv32_JALR.getRd(), getPC() + 4);
  }
  public void execute(RV32_LUI rv32_LUI) {
    setXRegister(rv32_LUI.getRd(), rv32_LUI.getImm());
  }
  public void execute(RV32_OR rv32_OR) {
    int rs1 = getXRegister(rv32_OR.getRs1());
    int rs2 = getXRegister(rv32_OR.getRs2());
    setXRegister(rv32_OR.getRd(), rs1 | rs2);
  }
  public void execute(RV32_ORI rv32_ORI) {
    int rs1 = getXRegister(rv32_ORI.getRs1());
    setXRegister(rv32_ORI.getRd(), rs1 | rv32_ORI.getImm());
  }
  public void execute(RV32_SLL rv32_SLL) {
    int rs1 = getXRegister(rv32_SLL.getRs1());
    int rs2 = getXRegister(rv32_SLL.getRs2());
    setXRegister(rv32_SLL.getRd(), rs1 << (rs2 & 0x0000001F));
  }
  public void execute(RV32_SLLI rv32_SLLI) {
    int rs1 = getXRegister(rv32_SLLI.getRs1());
    setXRegister(rv32_SLLI.getRd(), rs1 << rv32_SLLI.getShamt());
  }
  public void execute(RV32_SLT rv32_SLT) {
    int rs1 = getXRegister(rv32_SLT.getRs1());
    int rs2 = getXRegister(rv32_SLT.getRs2());
    boolean isLT = rs1 < rs2;
    if (isLT) {
      setXRegister(rv32_SLT.getRd(), 1);
    } else {
      setXRegister(rv32_SLT.getRd(), 0);
    }
  }
  public void execute(RV32_SLTI rv32_SLTI) {
    int rs1 = getXRegister(rv32_SLTI.getRs1());
    if (rs1 < rv32_SLTI.getImm()) {
      setXRegister(rv32_SLTI.getRd(), 1);
    } else {
      setXRegister(rv32_SLTI.getRd(), 0);
    }
  }
  public void execute(RV32_SLTU rv32_SLTU) {
    int rs1 = getXRegister(rv32_SLTU.getRs1());
    int rs2 = getXRegister(rv32_SLTU.getRs2());
    boolean isLT = (rs1 < rs2) ^ (rs1 < 0) ^ (rs2 < 0);
    if (isLT) {
      setXRegister(rv32_SLTU.getRd(), 1);
    } else {
      setXRegister(rv32_SLTU.getRd(), 0);
    }
  }
  public void execute(RV32_SLTIU rv32_SLTIU) {
    int i = getXRegister(rv32_SLTIU.getRs1());
    int j = rv32_SLTIU.getImm();
    boolean isLT = (i < j) ^ (i < 0) ^ (j < 0);
    if (isLT) {
      setXRegister(rv32_SLTIU.getRd(), 1);
    } else {
      setXRegister(rv32_SLTIU.getRd(), 0);
    }
  }
  public void execute(RV32_SRA rv32_SRA) {
    int rs1 = getXRegister(rv32_SRA.getRs1());
    int rs2 = getXRegister(rv32_SRA.getRs2());
    setXRegister(rv32_SRA.getRd(), rs1 >> (rs2 & 0x0000001F));
  }
  public void execute(RV32_SRAI rv32_SRAI) {
    int rs1 = getXRegister(rv32_SRAI.getRs1());
    setXRegister(rv32_SRAI.getRd(), rs1 >> rv32_SRAI.getShamt());
  }
  public void execute(RV32_SRL rv32_SRL) {
    int rs1 = getXRegister(rv32_SRL.getRs1());
    int rs2 = getXRegister(rv32_SRL.getRs2());
    setXRegister(rv32_SRL.getRd(), rs1 >>> (rs2 & 0x0000001F));
  }
  public void execute(RV32_SUB rv32_SUB) {
    int rs1 = getXRegister(rv32_SUB.getRs1());
    int rs2 = getXRegister(rv32_SUB.getRs2());
    setXRegister(rv32_SUB.getRd(), rs1 + rs2);
  }
  public void execute(RV32_SRLI rv32_SRLI) {
    int rs1 = getXRegister(rv32_SRLI.getRs1());
    setXRegister(rv32_SRLI.getRd(), rs1 >>> rv32_SRLI.getShamt());
  }
  public void execute(RV32_XOR rv32_XOR) {
    int rs1 = getXRegister(rv32_XOR.getRs1());
    int rs2 = getXRegister(rv32_XOR.getRs2());
    setXRegister(rv32_XOR.getRd(), rs1 ^ rs2);
  }
  public void execute(RV32_XORI rv32_XORI) {
    int rs1 = getXRegister(rv32_XORI.getRs1());
    setXRegister(rv32_XORI.getRd(), rs1 ^ rv32_XORI.getImm());
  }
  
}
