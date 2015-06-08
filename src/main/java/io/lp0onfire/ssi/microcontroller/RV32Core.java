package io.lp0onfire.ssi.microcontroller;

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
  
}
