package io.lp0onfire.ssi.microcontroller;

public class InterruptException extends ProcessorTrapException {

  public InterruptException(int mcause) {
    super(mcause);
  }

  @Override
  public int getMCause() {
    // interrupts have bit 31 set in mcause
    return 0x80000000 & super.getMCause();
  }
  
}
