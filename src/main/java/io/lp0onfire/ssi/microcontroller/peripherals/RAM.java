package io.lp0onfire.ssi.microcontroller.peripherals;

import io.lp0onfire.ssi.microcontroller.AddressTrapException;
import io.lp0onfire.ssi.microcontroller.SystemBusPeripheral;

public class RAM implements SystemBusPeripheral {

  private final int numberOfPages;
  private byte[] memory;
  
  public RAM(int numberOfPages) {
    this.numberOfPages = numberOfPages;
    memory = new byte[this.numberOfPages * 1024];
  }

  @Override
  public int getNumberOfPages() {
    return this.numberOfPages;
  }

  public void setContents(byte[] contents) {
    if (contents.length != memory.length) {
      throw new IllegalArgumentException();
    }
    System.arraycopy(contents, 0, memory, 0, memory.length);
  }
  
  @Override
  public int readByte(int address) throws AddressTrapException {
    int localAddr = translateAddress(address);
    if (localAddr >= 0 && localAddr < memory.length) {
      return ((int)memory[localAddr]) & 0x0FF;
    } else {
      return 0;
    }
  }

  // precondition: address is halfword aligned
  @Override
  public int readHalfword(int address) throws AddressTrapException {
    int localAddr = translateAddress(address);
    if (localAddr >= 0 && localAddr+1 < memory.length) {
      return
          (memory[localAddr+0] & 0x000000FF) |
          (memory[localAddr+1] & 0x000000FF) << 8
          ;
    } else {
      return 0;
    }
  }

  @Override
  public int readWord(int address) throws AddressTrapException {
    int localAddr = translateAddress(address);
    if (localAddr >= 0 && localAddr+3 < memory.length) {
      return
          (memory[localAddr+0] & 0x000000FF) |
          (memory[localAddr+1] & 0x000000FF) <<  8 |
          (memory[localAddr+2] & 0x000000FF) << 16 |
          (memory[localAddr+3] & 0x000000FF) << 24
          ;
    } else {
      return 0;
    }
  }

  @Override
  public void writeByte(int address, int value) throws AddressTrapException {
    int localAddr = translateAddress(address);
    if (localAddr >= 0 && localAddr < memory.length) {
      memory[localAddr] = (byte)(value & 0x0FF);
    }
  }

  @Override
  public void writeHalfword(int address, int value) throws AddressTrapException {
    int localAddr = translateAddress(address);
    if (localAddr >= 0 && localAddr+1 < memory.length) {
      memory[localAddr+0] = (byte)((value & 0x000000FF));
      memory[localAddr+1] = (byte)((value & 0x0000FF00) >>> 8);
    }
  }

  @Override
  public void writeWord(int address, int value) throws AddressTrapException {
    int localAddr = translateAddress(address);
    if (localAddr >= 0 && localAddr+3 < memory.length) {
      memory[localAddr+0] = (byte)((value & 0x000000FF));
      memory[localAddr+1] = (byte)((value & 0x0000FF00) >>>  8);
      memory[localAddr+2] = (byte)((value & 0x00FF0000) >>> 16);
      memory[localAddr+3] = (byte)((value & 0xFF000000) >>> 24);
    }
  }

}
