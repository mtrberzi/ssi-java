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
  
  private RV32SystemBus systemBus;
  public RV32SystemBus getSystemBus() {
    return this.systemBus;
  }
  
  // split mstatus register, because we only need two fields
  private boolean mstatus_ie = false;
  private boolean mstatus_ie1 = false;
  protected int getMstatus() {
    // [31:6] are all zeroes
    // [5:4] = "11"
    // [3] is mstatus_ie1
    // [2:1] = "11"
    // [0] is mstatus_ie
    int mstatus = 0x00000036;
    if (mstatus_ie) mstatus |= 0x00000001;
    if (mstatus_ie1) mstatus |= 0x00000008;
    return mstatus;
  }
  protected void setMstatus(int mstatus) {
    mstatus_ie = ((mstatus & 0x00000001) != 0);
    mstatus_ie1 = ((mstatus & 0x00000008) != 0);
  }
  
  private int mscratch;
  
  // machine exception program counter
  private int mepc;
  
  // machine exception cause
  private int mcause;
  
  // machine bad address register
  private int mbadaddr;
  
  protected int readCSR(int csr) throws IllegalInstructionException {
    switch (csr) {
    case 0x300:
      return getMstatus();
    case 0x340:
        return mscratch;
    case 0x341:
      return mepc;
    case 0x342:
      return mcause;
    case 0x343:
      return mbadaddr;
    case 0xF00:
      // mcpuid
      // base 00 (RV32I),
      // extensions I, M, A
      return 0b00000000000000000001000100000001;
    case 0xF01:
      // mimpid
      // 0x8000 = anonymous source
      // this is microcontroller version 0.0.1,
      // patch level 0
      return 0x00108000;
    case 0xF10:
      // mhartid
      return 0;
    default:
      // attempts to access a non-existent CSR raise an illegal instruction exception
      throw new IllegalInstructionException(0);
    }
  }
  
  protected void writeCSR(int csr, int value) throws IllegalInstructionException {
    switch (csr) {
    case 0x300:
      setMstatus(value);
      break;
    case 0x340:
      mscratch = value;
      break;
    case 0x341:
      mepc = value;
      break;
    case 0x342:
      mcause = value;
      break;
    case 0x343:
      mbadaddr = value;
      break;
    default:
      // attempts to access a non-existent CSR
      // or write to a read-only CSR 
      // raise an illegal instruction exception
      throw new IllegalInstructionException(0);
    }
  }
  
  public RV32Core() {
    for (int i = 0; i < 32; ++i) {
      xRegister[i] = 0;
    }
    systemBus = new RV32SystemBus();
  }
  
  public void step() {
    try {
      // fetch + decode
      RV32Instruction instruction = systemBus.fetchInstruction(pc);
      next_pc = pc + 4;
      instruction.execute(this);
      pc = next_pc;
    } catch (ProcessorTrapException e) {
      processorTrap(e);
    }
  }
  
  private void processorTrap(ProcessorTrapException e) {
    if (e instanceof AddressTrapException) {
      mbadaddr = ((AddressTrapException)e).getBadAddr();
    }
    // when a trap is taken, the mstatus stack is pushed to the left
    // and IE is set to 0
    mstatus_ie1 = mstatus_ie;
    mstatus_ie = false;
    // save program counter
    // TODO there are exceptions where we actually save pc+4
    mepc = pc;
    // set mcause
    mcause = e.getMCause();
    // jump to the correct trap handler, which is always at 0x100 + whatever offset
    if (true) {
      // trap from machine mode
      pc = 0x000001C0;
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
  public void execute(RV32_CSRRC rv32_CSRRC) throws IllegalInstructionException {
    try {
      int csr = rv32_CSRRC.getImm();
      int old_value = readCSR(csr);
      if (rv32_CSRRC.getRs1() != 0) {
        writeCSR(csr, old_value & ~getXRegister(rv32_CSRRC.getRs1()));
      }
      setXRegister(rv32_CSRRC.getRd(), old_value);
    } catch (IllegalInstructionException e) {
      // re-raise with correct instruction word
      throw new IllegalInstructionException(rv32_CSRRC.getInsn());
    }
  }
  public void execute(RV32_CSRRCI rv32_CSRRCI) throws IllegalInstructionException {
    try {
      int csr = rv32_CSRRCI.getImm();
      int old_value = readCSR(csr);
      if (rv32_CSRRCI.getRs1() != 0) {
        writeCSR(csr, old_value | rv32_CSRRCI.getRs1());
      }
      setXRegister(rv32_CSRRCI.getRd(), old_value);
    } catch (IllegalInstructionException e) {
      // re-raise with correct instruction word
      throw new IllegalInstructionException(rv32_CSRRCI.getInsn());
    }
  }
  public void execute(RV32_CSRRS rv32_CSRRS) throws IllegalInstructionException {
    try {
      int csr = rv32_CSRRS.getImm();
      int old_value = readCSR(csr);
      if (rv32_CSRRS.getRs1() != 0) {
        writeCSR(csr, old_value | getXRegister(rv32_CSRRS.getRs1()));
      }
      setXRegister(rv32_CSRRS.getRd(), old_value);
    } catch (IllegalInstructionException e) {
      // re-raise with correct instruction word
      throw new IllegalInstructionException(rv32_CSRRS.getInsn());
    }
  }
  public void execute(RV32_CSRRSI rv32_CSRRSI) throws IllegalInstructionException {
    try {
      int csr = rv32_CSRRSI.getImm();
      int old_value = readCSR(csr);
      if (rv32_CSRRSI.getRs1() != 0) {
        writeCSR(csr, old_value | rv32_CSRRSI.getRs1());
      }
      setXRegister(rv32_CSRRSI.getRd(), old_value);
    } catch (IllegalInstructionException e) {
      // re-raise with correct instruction word
      throw new IllegalInstructionException(rv32_CSRRSI.getInsn());
    }
  }
  public void execute(RV32_CSRRW rv32_CSRRW) throws IllegalInstructionException {
    try {
      int csr = rv32_CSRRW.getImm();
      int old_value = readCSR(csr);
      writeCSR(csr, getXRegister(rv32_CSRRW.getRs1()));
      setXRegister(rv32_CSRRW.getRd(), old_value);
    } catch (IllegalInstructionException e) {
      // re-raise with correct instruction word
      throw new IllegalInstructionException(rv32_CSRRW.getInsn());
    }
  }
  public void execute(RV32_CSRRWI rv32_CSRRWI) throws IllegalInstructionException {
    try {
      int csr = rv32_CSRRWI.getImm();
      int old_value = readCSR(csr);
      if (rv32_CSRRWI.getRs1() != 0) {
        writeCSR(csr, rv32_CSRRWI.getRs1());
      }
      setXRegister(rv32_CSRRWI.getRd(), old_value);
    } catch (IllegalInstructionException e) {
      // re-raise with correct instruction word
      throw new IllegalInstructionException(rv32_CSRRWI.getInsn());
    }
  }
  public void execute(RV32_ERET rv32_ERET) {
    // pop the interrupt stack to the right and set 
    // the leftmost entry to interrupts enabled
    mstatus_ie = mstatus_ie1;
    mstatus_ie1 = true;
    next_pc = mepc;
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
  public void execute(RV32_LB rv32_LB) {
    int addr = getXRegister(rv32_LB.getRs1()) + rv32_LB.getImm();
    try {
      int data = systemBus.loadWord(addr);
      // sign-extend to 32 bits
      data = (data << 24) >> 24;
      setXRegister(rv32_LB.getRd(), data);
    } catch (AddressTrapException e) {
      processorTrap(e);
    }
  }
  public void execute(RV32_LBU rv32_LBU) {
    int addr = getXRegister(rv32_LBU.getRs1()) + rv32_LBU.getImm();
    try {
      int data = systemBus.loadWord(addr);
      // zero-extend to 32 bits
      data = (data & 0x000000FF);
      setXRegister(rv32_LBU.getRd(), data);
    } catch (AddressTrapException e) {
      processorTrap(e);
    }
  }
  public void execute(RV32_LH rv32_LH) {
    int addr = getXRegister(rv32_LH.getRs1()) + rv32_LH.getImm();
    try {
      int data = systemBus.loadWord(addr);
      // sign-extend to 32 bits
      data = (data << 16) >> 16;
      setXRegister(rv32_LH.getRd(), data);
    } catch (AddressTrapException e) {
      processorTrap(e);
    }
  }
  public void execute(RV32_LHU rv32_LHU) {
    int addr = getXRegister(rv32_LHU.getRs1()) + rv32_LHU.getImm();
    try {
      int data = systemBus.loadWord(addr);
      // zero-extend to 32 bits
      data = (data & 0x0000FFFF);
      setXRegister(rv32_LHU.getRd(), data);
    } catch (AddressTrapException e) {
      processorTrap(e);
    }
  }
  public void execute(RV32_LUI rv32_LUI) {
    setXRegister(rv32_LUI.getRd(), rv32_LUI.getImm());
  }
  public void execute(RV32_LW rv32_LW) {
    int addr = getXRegister(rv32_LW.getRs1()) + rv32_LW.getImm();
    try {
      int data = systemBus.loadWord(addr);
      setXRegister(rv32_LW.getRd(), data);
    } catch (AddressTrapException e) {
      processorTrap(e);
    }
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
  public void execute(RV32_SB rv32_SB) {
    int addr = getXRegister(rv32_SB.getRs1()) + rv32_SB.getImm();
    int data = getXRegister(rv32_SB.getRs2()) & 0x000000FF;
    try {
      systemBus.storeByte(addr, data);
    } catch (AddressTrapException e) {
      processorTrap(e);
    }
  }
  public void execute(RV32_SH rv32_SH) {
    int addr = getXRegister(rv32_SH.getRs1()) + rv32_SH.getImm();
    int data = getXRegister(rv32_SH.getRs2()) & 0x0000FFFF;
    try {
      systemBus.storeHalfword(addr, data);
    } catch (AddressTrapException e) {
      processorTrap(e);
    }
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
  public void execute(RV32_SRLI rv32_SRLI) {
    int rs1 = getXRegister(rv32_SRLI.getRs1());
    setXRegister(rv32_SRLI.getRd(), rs1 >>> rv32_SRLI.getShamt());
  }
  public void execute(RV32_SUB rv32_SUB) {
    int rs1 = getXRegister(rv32_SUB.getRs1());
    int rs2 = getXRegister(rv32_SUB.getRs2());
    setXRegister(rv32_SUB.getRd(), rs1 + rs2);
  }
  public void execute(RV32_SW rv32_SW) {
    int addr = getXRegister(rv32_SW.getRs1()) + rv32_SW.getImm();
    int data = getXRegister(rv32_SW.getRs2());
    try {
      systemBus.storeWord(addr, data);
    } catch (AddressTrapException e) {
      processorTrap(e);
    }
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
