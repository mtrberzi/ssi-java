package io.lp0onfire.ssi.microcontroller.peripherals;

import io.lp0onfire.ssi.microcontroller.AddressTrapException;
import io.lp0onfire.ssi.microcontroller.SystemBusPeripheral;

public class InterruptController implements SystemBusPeripheral {

  public InterruptController() {
    this.interruptEnabled = new boolean[32];
    for (int i = 0; i < 32; ++i) {
      this.interruptEnabled[i] = false;
    }
  }
  
  @Override
  public int getNumberOfPages() {
    return 4;
  }

  @Override
  public int readByte(int address) throws AddressTrapException {
    throw new AddressTrapException(4, address);
  }

  @Override
  public int readHalfword(int address) throws AddressTrapException {
    throw new AddressTrapException(4, address);
  }

  @Override
  public void writeByte(int address, int value) throws AddressTrapException {
    throw new AddressTrapException(6, address);
  }

  @Override
  public void writeHalfword(int address, int value) throws AddressTrapException {
    throw new AddressTrapException(6, address);
  }
  
  @Override
  public int readWord(int pAddr) throws AddressTrapException {
    int address = translateAddress(pAddr);
    int registerNumber = (address & 0x00000FFF) >>> 2;
    switch (registerNumber) {
    case 0: // Master Interrupt Status
      return (getMasterEnable() ? 0x80000000 : 0x00000000) | getCurrentInterrupt();
    case 1: // Interrupt Enable Register
      return this.interruptEnableRegister;
    default:
      throw new AddressTrapException(5, pAddr);
    }
  }

  @Override
  public void writeWord(int pAddr, int value) throws AddressTrapException {
    int address = translateAddress(pAddr);
    int registerNumber = (address & 0x00000FFF) >>> 2;
    switch (registerNumber) {
    case 0: // Master Interrupt Status
      setMasterEnable((value & 0x80000000) != 0);
      break;
    case 1: // Interrupt Enable Register
      for (int i = 0; i < 32; ++i) {
        setInterruptEnabled(i, (value & 0x00000001) != 0);
        value = value >>> 1;
      }
      break;
    default:
      throw new AddressTrapException(7, pAddr);
    }
  }

  private boolean masterEnable = false;
  public boolean getMasterEnable() {
    return this.masterEnable;
  }
  public void setMasterEnable(boolean b) {
    this.masterEnable = b;
  }
  
  private int currentInterrupt = 0;
  public int getCurrentInterrupt() {
    return this.currentInterrupt;
  }

  private int interruptEnableRegister = 0;
  private boolean[] interruptEnabled;
  
  public boolean getInterruptEnabled(int i) {
    return interruptEnabled[i];
  }
  
  public void setInterruptEnabled(int i, boolean e) {
    interruptEnabled[i] = e;
    // update interruptEnableRegister;
    if (e) {
      interruptEnableRegister |= (1 << i);
    } else {
      interruptEnableRegister &= ~(1 << i);
    }
  }
  
  @Override
  public void cycle() {
    // TODO Auto-generated method stub
    
  }
  
}
