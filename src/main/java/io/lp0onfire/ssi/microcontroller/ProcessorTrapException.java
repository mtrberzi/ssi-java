package io.lp0onfire.ssi.microcontroller;

public class ProcessorTrapException extends Exception {

  private static final long serialVersionUID = 1L;
  
  private final int mcause;
  public int getMCause() {
    return this.mcause;
  }
  
  public ProcessorTrapException(int mcause) {
    this.mcause = mcause;
  }
  
}
