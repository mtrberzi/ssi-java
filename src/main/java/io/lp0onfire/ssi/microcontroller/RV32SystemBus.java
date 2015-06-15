package io.lp0onfire.ssi.microcontroller;

import java.util.HashMap;
import java.util.Map;

public class RV32SystemBus {

  // divide the address space into 1024-byte pages and
  // track which peripherals are mapped into each page
  private Map<Integer, SystemBusPeripheral> mappedPages = new HashMap<>();
  
  private static final int LAST_VALID_PAGE = 0xFFFFFFFF >>> 10;
  
  public void attachPeripheral(SystemBusPeripheral p, int baseAddress) {
    int basePage = baseAddress >>> 10;
    // first pass to validate, don't map anything yet
    for (int i = basePage; i < basePage + p.getNumberOfPages(); ++i) {
      if (i > LAST_VALID_PAGE) {
        throw new IllegalStateException("attempt to map peripheral beyond a legal address");
      }
      if (mappedPages.containsKey(i)) {
        throw new IllegalStateException("attempt to map two peripherals into the same page");
      }
    }
    // second pass to map
    for (int i = basePage; i < basePage + p.getNumberOfPages(); ++i) {
      mappedPages.put(i, p);
    }
  }
  
  public int loadByte(int address) throws AddressTrapException {
    int pageNumber = (address & 0xFFFFF800) >>> 10;
    if (mappedPages.containsKey(pageNumber)) {
      return mappedPages.get(pageNumber).readByte(address);
    } else {
      throw new AddressTrapException(5, address);
    }
  }
  
  public int loadHalfword(int address) throws AddressTrapException {
    // must be 2-byte aligned
    if ((address & 0x00000001) != 0) {
      throw new AddressTrapException(4, address);
    }
    int pageNumber = (address & 0xFFFFF800) >>> 10;
    if (mappedPages.containsKey(pageNumber)) {
      return mappedPages.get(pageNumber).readHalfword(address);
    } else {
      throw new AddressTrapException(5, address);
    }
  }
  
  public int loadWord(int address) throws AddressTrapException {
    // must be 4-byte aligned
    if ((address & 0x00000003) != 0) {
      throw new AddressTrapException(4, address);
    }
    int pageNumber = (address & 0xFFFFF800) >>> 10;
    if (mappedPages.containsKey(pageNumber)) {
      return mappedPages.get(pageNumber).readWord(address);
    } else {
      throw new AddressTrapException(5, address);
    }
  }
  
  public int fetchInstruction(int address) throws ProcessorTrapException {
    // must be 4-byte aligned
    if ((address & 0x00000003) != 0) {
      throw new AddressTrapException(0, address);
    }
    int pageNumber = (address & 0xFFFFF800) >>> 10;
    if (mappedPages.containsKey(pageNumber)) {
      return mappedPages.get(pageNumber).readWord(address);
    } else {
      throw new AddressTrapException(1, address);
    }
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
