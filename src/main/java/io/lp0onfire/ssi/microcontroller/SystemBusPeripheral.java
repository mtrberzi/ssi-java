package io.lp0onfire.ssi.microcontroller;

public interface SystemBusPeripheral {

  int getNumberOfPages(); // number of 1024-byte pages mapped by this peripheral

  default int translateAddress(int address) {
    int numberOfBytes = getNumberOfPages() << 10;
    int nZeros = Integer.numberOfLeadingZeros(numberOfBytes);
    int mask = ~(0x80000000 >> nZeros);
    return address & mask;
  }
  
  int readByte(int address) throws AddressTrapException;
  int readHalfword(int address) throws AddressTrapException;
  int readWord(int address) throws AddressTrapException;
  void writeByte(int address, int value) throws AddressTrapException;
  void writeHalfword(int address, int value) throws AddressTrapException;
  void writeWord(int address, int value) throws AddressTrapException;
  
}
