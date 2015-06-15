package io.lp0onfire.ssi.microcontroller;

public class AddressTrapException extends ProcessorTrapException {

  private static final long serialVersionUID = 1L;
  
  private final int badaddr;
  public int getBadAddr() {
    return this.badaddr;
  }
  
  public AddressTrapException(int mcause, int badaddr) {
    super(mcause);
    this.badaddr = badaddr;
  }
  
}
