package io.lp0onfire.ssi.microcontroller.peripherals;

import io.lp0onfire.ssi.microcontroller.AddressTrapException;
import io.lp0onfire.ssi.microcontroller.SystemBusPeripheral;

public class ROM implements SystemBusPeripheral {

  private final int numberOfPages;
  private byte[] memory;
  
  public ROM(int numberOfPages) {
    this.numberOfPages = numberOfPages;
    memory = new byte[this.numberOfPages * 1024];
  }

  @Override
  public int getNumberOfPages() {
    return this.numberOfPages;
  }

  @Override
  public int readByte(int address) throws AddressTrapException {
    int localAddr = translateAddress(address);
    if (localAddr >= 0 && localAddr < memory.length) {
      return memory[localAddr];
    } else {
      return 0;
    }
  }

  @Override
  public int readHalfword(int address) throws AddressTrapException {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int readWord(int address) throws AddressTrapException {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void writeByte(int address, int value) throws AddressTrapException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void writeHalfword(int address, int value) throws AddressTrapException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void writeWord(int address, int value) throws AddressTrapException {
    // TODO Auto-generated method stub
    
  }

}
