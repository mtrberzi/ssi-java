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
  
  public RV32Core() {
    for (int i = 0; i < 32; ++i) {
      xRegister[i] = 0;
    }
  }
  public void execute(RV32_ADDI rv32_ADDI) {
    // TODO Auto-generated method stub
    
  }
  public void execute(RV32_ANDI rv32_ANDI) {
    // TODO Auto-generated method stub
    
  }
  public void execute(RV32_ORI rv32_ORI) {
    // TODO Auto-generated method stub
    
  }
  public void execute(RV32_SLLI rv32_SLLI) {
    // TODO Auto-generated method stub
    
  }
  public void execute(RV32_SLTI rv32_SLTI) {
    // TODO Auto-generated method stub
    
  }
  public void execute(RV32_SLTIU rv32_SLTIU) {
    // TODO Auto-generated method stub
    
  }
  public void execute(RV32_SRAI rv32_SRAI) {
    // TODO Auto-generated method stub
    
  }
  public void execute(RV32_SRLI rv32_SRLI) {
    // TODO Auto-generated method stub
    
  }
  public void execute(RV32_XORI rv32_XORI) {
    // TODO Auto-generated method stub
    
  }
  
}
