package io.lp0onfire.ssi.microcontroller.peripherals;

import io.lp0onfire.ssi.microcontroller.AddressTrapException;
import io.lp0onfire.ssi.microcontroller.RV32Core;
import io.lp0onfire.ssi.microcontroller.SystemBusPeripheral;

public class InterruptController implements SystemBusPeripheral {

  private final RV32Core cpu;
  
  public InterruptController(RV32Core cpu) {
    this.cpu = cpu;
    this.interruptEnabled = new boolean[32];
    for (int i = 0; i < 32; ++i) {
      this.interruptEnabled[i] = false;
    }
    
    this.interruptPending = new boolean[32];
    for (int i = 0; i < 32; ++i) {
      this.interruptPending[i] = false;
    }
    
    this.interruptPriority = new int[32];
    for (int i = 0; i < 32; ++i) {
      this.interruptPriority[i] = 0;
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
    case 2: // Interrupt Pending Register
      return this.interruptPendingRegister;
    default:
      if (registerNumber >= 4 && registerNumber < 4+32) {
        // Interrupt # Priority
        return getInterruptPriority(registerNumber - 4);
      }
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
    case 3: // Interrupt Acknowledge Register
      for (int i = 0; i < 32; ++i) {
        if ((value & 0x00000001) != 0) {
          acknowledgeInterrupt(i);
        }
        value = value >>> 1;
      }
      break;
    default:
      if (registerNumber >= 4 && registerNumber < 4+32) {
        // Interrupt # Priority
        setInterruptPriority(registerNumber - 4, value & 0x0000001F);
        break;
      }
      throw new AddressTrapException(7, pAddr);
    }
  }

  private boolean masterEnable = false;
  public boolean getMasterEnable() {
    return this.masterEnable;
  }
  public void setMasterEnable(boolean b) {
    this.masterEnable = b;
    stateChange = true;
  }
  
  private int currentInterrupt = -1;
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
    stateChange = true;
  }
  
  private boolean stateChange = false;
  
  public int nextInterrupt() {
    if (!masterEnable) {
      return -1;
    }
    int nextInterrupt = -1;
    int currentPriority = 32;
    for (int i = 31; i >= 0; --i) {
      if (interruptPending[i] && interruptEnabled[i]) {
        // priority check: priority must be smaller than
        // last priority, or equal priority and lower interrupt number
        if (interruptPriority[i] <= currentPriority) {
          nextInterrupt = i;
          currentPriority = interruptPriority[i];
        }
      }
    }
    return nextInterrupt;
  }
  
  @Override
  public void cycle() {
    if (stateChange) {
      if (masterEnable) {
        int nextInterrupt = nextInterrupt();
        if (nextInterrupt != -1) {
          // new interrupt pending
          if (nextInterrupt != currentInterrupt) {
            if (cpu.interruptsEnabled()) {
              stateChange = false;
              // interrupt the CPU
              cpu.externalInterrupt();
            }
          }
        }
      }
    }
  }
  
  private int interruptPendingRegister;
  private boolean[] interruptPending;
  
  public void assertInterrupt(int irq) {
    if (!interruptPending[irq]) {
      interruptPending[irq] = true;
      interruptPendingRegister |= (1 << irq);
      stateChange = true;
    }
  }
  
  public void acknowledgeInterrupt(int irq) {
    if (interruptPending[irq]) {
      interruptPending[irq] = false;
      interruptPendingRegister &= ~(1 << irq);
      stateChange = true;
      if (currentInterrupt == irq) {
        currentInterrupt = -1;
      }
    }
  }
  
  private int[] interruptPriority;
  public int getInterruptPriority(int irq) {
    return interruptPriority[irq];
  }
  public void setInterruptPriority(int irq, int priority) {
    interruptPriority[irq] = priority;
    stateChange = true;
  }
  
}
