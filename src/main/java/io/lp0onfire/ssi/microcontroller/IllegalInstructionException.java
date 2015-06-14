package io.lp0onfire.ssi.microcontroller;

public class IllegalInstructionException extends ProcessorTrapException {

  private static final long serialVersionUID = 1L;

  private final int insn;
  
  public IllegalInstructionException(int insn) {
    super(2);
    this.insn = insn;
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(Integer.toHexString(insn));
    if (sb.length() < 8) {
      for (int i = 0; i < 8 - sb.length(); ++i) {
        sb.insert(0, '0');
      }
      sb.insert(0, "illegal instruction 0x");
    }
    return sb.toString();
  }
  
}
