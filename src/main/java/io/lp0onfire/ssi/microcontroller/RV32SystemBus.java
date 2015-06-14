package io.lp0onfire.ssi.microcontroller;

public class RV32SystemBus {

  public int loadByte(int address) throws AddressTrapException {
    // TODO
    throw new UnsupportedOperationException("not yet implemented");
  }
  
  public int loadHalfword(int address) throws AddressTrapException {
    // must be 2-byte aligned
    if ((address & 0x00000001) != 0) {
      throw new AddressTrapException(4, address);
    }
    // TODO
    throw new UnsupportedOperationException("not yet implemented");
  }
  
  public int loadWord(int address) throws AddressTrapException {
    // must be 4-byte aligned
    if ((address & 0x00000003) != 0) {
      throw new AddressTrapException(4, address);
    }
    // TODO
    throw new UnsupportedOperationException("not yet implemented");
  }
  
  public int fetchInstruction(int address) throws ProcessorTrapException {
    // must be 4-byte aligned
    if ((address & 0x00000003) != 0) {
      throw new AddressTrapException(0, address);
    }
    // TODO
    throw new UnsupportedOperationException("not yet implemented");
  }
  
  public void storeByte(int address, int value) throws AddressTrapException {
    // TODO
    throw new UnsupportedOperationException("not yet implemented");
  }
  
  public void storeHalfword(int address, int value) throws AddressTrapException {
    // must be 2-byte aligned
    if ((address & 0x00000001) != 0) {
      throw new AddressTrapException(6, address);
    }
    // TODO
    throw new UnsupportedOperationException("not yet implemented");
  }
  
  public void storeWord(int address, int value) throws AddressTrapException {
    // must be 4-byte aligned
    if ((address & 0x00000003) != 0) {
      throw new AddressTrapException(6, address);
    }
    // TODO
    throw new UnsupportedOperationException("not yet implemented");
  }
  
}
