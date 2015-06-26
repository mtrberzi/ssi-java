package io.lp0onfire.ssi.microcontroller;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RV32SystemBus {

  // divide the address space into 1024-byte pages and
  // track which peripherals are mapped into each page
  private Map<Integer, SystemBusPeripheral> mappedPages = new HashMap<>();
  
  private static final int LAST_VALID_PAGE = 0xFFFFFFFF >>> 10;
  
  private RV32InstructionDecoder decoder;
  
  // reservations are always made on word-aligned addresses
  private Set<Integer> reservedAddresses = new HashSet<>();
  
  private Map<Integer, RV32Instruction> instructionCache = new HashMap<>();
  
  public RV32SystemBus() {
    this.decoder = new RV32InstructionDecoder();
  }
  
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
  
  public RV32Instruction fetchInstruction(int address) throws AddressTrapException {
    if (instructionCache.containsKey(address)) {
      return instructionCache.get(address);
    }
    // must be 4-byte aligned
    if ((address & 0x00000003) != 0) {
      throw new AddressTrapException(0, address);
    }
    int pageNumber = (address & 0xFFFFF800) >>> 10;
    if (mappedPages.containsKey(pageNumber)) {
      int insn = mappedPages.get(pageNumber).readWord(address);
      RV32Instruction instruction = decoder.decode(insn);
      instructionCache.put(address, instruction);
      return instruction;
    } else {
      throw new AddressTrapException(1, address);
    }
  }
  
  public void storeByte(int address, int value) throws AddressTrapException {
    int pageNumber = (address & 0xFFFFF800) >>> 10;
    if (mappedPages.containsKey(pageNumber)) {
      mappedPages.get(pageNumber).writeByte(address, value);
      clearReservation(address);
    } else {
      throw new AddressTrapException(7, address);
    }
  }
  
  public void storeHalfword(int address, int value) throws AddressTrapException {
    // must be 2-byte aligned
    if ((address & 0x00000001) != 0) {
      throw new AddressTrapException(6, address);
    }
    int pageNumber = (address & 0xFFFFF800) >>> 10;
    if (mappedPages.containsKey(pageNumber)) {
      mappedPages.get(pageNumber).writeHalfword(address, value);
      clearReservation(address);
    } else {
      throw new AddressTrapException(7, address);
    }
  }
  
  public void storeWord(int address, int value) throws AddressTrapException {
    // must be 4-byte aligned
    if ((address & 0x00000003) != 0) {
      throw new AddressTrapException(6, address);
    }
    int pageNumber = (address & 0xFFFFF800) >>> 10;
    if (mappedPages.containsKey(pageNumber)) {
      mappedPages.get(pageNumber).writeWord(address, value);
      clearReservation(address);
    } else {
      throw new AddressTrapException(7, address);
    }
  }
  
  public void setReservation(int address) {
    reservedAddresses.add(address >>> 2);
  }
  
  public boolean isReserved(int address) {
    return reservedAddresses.contains(address >>> 2);
  }
  
  public void clearReservation(int address) {
    reservedAddresses.remove(address >>> 2);
  }
  
  public void clearAllReservations() {
    reservedAddresses.clear();
  }
  
  public void clearInstructionCache() {
    instructionCache.clear();
  }
  
}
