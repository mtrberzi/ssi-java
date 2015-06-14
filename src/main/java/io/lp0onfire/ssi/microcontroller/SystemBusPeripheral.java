package io.lp0onfire.ssi.microcontroller;

public interface SystemBusPeripheral {

  int getBaseAddress(); // the 10 lowest bits are ignored so that this always starts at the beginning of a page
  int getNumberOfPages(); // number of 1024-byte pages mapped by this peripheral
  
  int readByte(int address) throws AddressTrapException;
  int readHalfword(int address) throws AddressTrapException;
  int readWord(int address) throws AddressTrapException;
  void writeByte(int address, int value) throws AddressTrapException;
  void writeHalfword(int address, int value) throws AddressTrapException;
  void writeWord(int address, int value) throws AddressTrapException;
  
}
