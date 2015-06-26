package io.lp0onfire.ssi.microcontroller;

public interface SystemBusPeripheral {

  int getNumberOfPages(); // number of 1024-byte pages mapped by this peripheral

  default int translateAddress(int pAddr) {
    int numberOfBytes = getNumberOfPages() << 10;
    int nZeros = Integer.numberOfLeadingZeros(numberOfBytes);
    int mask = ~(0x80000000 >> nZeros);
    return pAddr & mask;
  }
  
  int readByte(int pAddr) throws AddressTrapException;
  int readHalfword(int pAddr) throws AddressTrapException;
  int readWord(int pAddr) throws AddressTrapException;
  void writeByte(int pAddr, int value) throws AddressTrapException;
  void writeHalfword(int pAddr, int value) throws AddressTrapException;
  void writeWord(int pAddr, int value) throws AddressTrapException;
  
  void cycle();
  
}
