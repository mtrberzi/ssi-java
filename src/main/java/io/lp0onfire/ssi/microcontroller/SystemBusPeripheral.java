package io.lp0onfire.ssi.microcontroller;

public interface SystemBusPeripheral {

  int getNumberOfPages(); // number of 1024-byte pages mapped by this peripheral

  default int translateAddress(int pAddr) {
    int v = getNumberOfPages() * 1024;
    // this is very slow
    int mask = 1;
    while (mask < v) {
      mask = mask << 1;
    }
    mask -= 1;
    return pAddr & mask;
  }
  
  int readByte(int pAddr) throws AddressTrapException;
  int readHalfword(int pAddr) throws AddressTrapException;
  int readWord(int pAddr) throws AddressTrapException;
  void writeByte(int pAddr, int value) throws AddressTrapException;
  void writeHalfword(int pAddr, int value) throws AddressTrapException;
  void writeWord(int pAddr, int value) throws AddressTrapException;
  
  void cycle();
  // called exactly once per timestep, and after all global cycles have completed
  void timestep();
  
}
