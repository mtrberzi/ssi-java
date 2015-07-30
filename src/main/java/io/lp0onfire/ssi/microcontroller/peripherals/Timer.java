package io.lp0onfire.ssi.microcontroller.peripherals;

import io.lp0onfire.ssi.microcontroller.AddressTrapException;
import io.lp0onfire.ssi.microcontroller.InterruptSource;
import io.lp0onfire.ssi.microcontroller.SystemBusPeripheral;

public class Timer implements SystemBusPeripheral, InterruptSource {

  private int rawPrescalerPeriod = 0;
  private int prescalerPeriod = 2;
  public int getPrescalerPeriod() {
    return this.prescalerPeriod;
  }
  public void setPrescalerPeriod(int p) {
    switch (p) {
    case 0:
      prescalerPeriod = 2; break;
    case 1:
      prescalerPeriod = 4; break;
    case 2:
      prescalerPeriod = 8; break;
    case 3:
      prescalerPeriod = 16; break;
    case 4:
      prescalerPeriod = 32; break;
    case 5:
      prescalerPeriod = 64; break;
    case 6:
      prescalerPeriod = 128; break;
    case 7: 
      prescalerPeriod = 256; break;
    default:
      throw new IllegalArgumentException("invalid prescaler value " + p);
    }
    rawPrescalerPeriod = p;
  }
  
  private boolean autoReload = false;
  public boolean getAutoReloadEnable() {
    return this.autoReload;
  }
  public void setAutoReloadEnable(boolean b) {
    this.autoReload = b;
  }
  
  private boolean prescalerEnabled = false;
  public boolean getPrescalerEnable() {
    return this.prescalerEnabled;
  }
  public void setPrescalerEnable(boolean b) {
    this.prescalerEnabled = b;
  }
  
  private boolean interruptsEnabled = false;
  public boolean getMasterInterruptEnable() {
    return this.interruptsEnabled;
  }
  public void setMasterInterruptEnable(boolean b) {
    this.interruptsEnabled = b;
  }
  
  private boolean timerRunning = false;
  public boolean getTimerStart() {
    return this.timerRunning;
  }
  public void setTimerStart(boolean b) {
    this.timerRunning = b;
  }
  
  private int prescalerCounter = 0;
  private int counter = 0;
  
  public int getPrescalerCounter() {
    return this.prescalerCounter;
  }
  
  public int getCounter() {
    return this.counter;
  }
  
  public void setCounter(int c) {
    this.counter = c;
  }
  
  private int reload = 0;
  public void setReload(int r) {
    this.reload = r;
  }
  
  private int match = 0;
  public void setMatch(int m) {
    this.match = m;
  }
  
  private boolean matchInterruptEnabled = false;
  public void setMatchInterruptEnable(boolean e) {
    this.matchInterruptEnabled = e;
  }
  private boolean overflowInterruptEnabled = false;
  public void setOverflowInterruptEnable(boolean e) {
    this.overflowInterruptEnabled = e;
  }
  
  private boolean matchInterruptAsserted = false;
  public boolean getMatchInterruptAsserted() {
    return this.matchInterruptAsserted;
  }
  private boolean overflowInterruptAsserted = false;
  public boolean getOverflowInterruptAsserted() {
    return this.overflowInterruptAsserted;
  }
  
  @Override
  public int getNumberOfPages() {
    return 1;
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
    case 0: // TIMER_CTRL
    {
      int retval = 0;
      // bits 6-4: prescaler period
      retval |= (rawPrescalerPeriod << 4);
      // bit 3: autoreload enable
      retval |= (getAutoReloadEnable() ? (1<<3) : 0);
      // bit 2: prescaler enable
      retval |= (getPrescalerEnable() ? (1<<2) : 0);
      // bit 1: master interrupt enable
      retval |= (getMasterInterruptEnable() ? (1<<1) : 0);
      // bit 0: timer start/stop
      retval |= (getTimerStart() ? (1<<0) : 0);
      return retval;
    }
    case 1: // TIMER_COUNT
      return counter;
    case 2: // TIMER_RELOAD
      return reload;
    case 3: // TIMER_MATCH
      return match;
    case 4: // TIMER_IE
    {
      int retval = 0;
      // bit 1: match interrupt enable
      retval |= (matchInterruptEnabled ? (1<<1) : 0);
      // bit 0: overflow interrupt enable
      retval |= (overflowInterruptEnabled ? (1<<0) : 0);
      return retval;
    }
    case 5: // TIMER_IP
    {
      int retval = 0;
      // bit 1: match interrupt pending
      retval |= (matchInterruptAsserted ? (1<<1) : 0);
      // bit 0: overflow interrupt pending
      retval |= (overflowInterruptAsserted ? (1<<0) : 0);
      return retval;
    }
    default:
      throw new AddressTrapException(5, pAddr);
    }
  }

  @Override
  public void writeWord(int pAddr, int value) throws AddressTrapException {
    int address = translateAddress(pAddr);
    int registerNumber = (address & 0x00000FFF) >>> 2;
    switch (registerNumber) {
    case 0: // TIMER_CTRL
    {
      setPrescalerPeriod((value & 0x00000070) >>> 4);
      setAutoReloadEnable((value & 0x00000008) != 0);
      setPrescalerEnable((value & 0x00000004) != 0);
      setMasterInterruptEnable((value & 0x00000002) != 0);
      setTimerStart((value & 0x00000001) != 0);
    } break;
    case 1: // TIMER_COUNT
    {
      counter = value;
    } break;
    case 2: // TIMER_RELOAD
    {
      reload = value;
    } break;
    case 3: // TIMER_MATCH
    {
      match = value;
    } break;
    case 4: // TIMER_IE
      matchInterruptEnabled = ((value & 0x00000002) != 0);
      overflowInterruptEnabled = ((value & 0x00000001) != 0);
      break;
    case 6: // TIMER_IA
      if ((value & 0x00000002) != 0) {
        matchInterruptAsserted = false;
      }
      if ((value & 0x00000001) != 0) {
        overflowInterruptAsserted = false;
      }
      break;
    default:
      throw new AddressTrapException(7, pAddr);
    }
  }

  @Override
  public void cycle() {
    if (timerRunning) {
      boolean incrementCounter = false;
      if (prescalerEnabled) {
        prescalerCounter += 1;
        if (prescalerCounter >= prescalerPeriod) {
          prescalerCounter = 0;
          incrementCounter = true;
        }
      } else {
        incrementCounter = true;
      }
      
      if (incrementCounter && counter == 0xFFFFFFFF) {
        overflowInterruptAsserted = true;
        if (autoReload) {
          counter = reload;
        } else {
          counter = 0;
        }
      } else if (incrementCounter) {
        counter += 1;
      }
      
      if (incrementCounter && counter == match) {
        matchInterruptAsserted = true;
      }
    }
    
  }

  @Override
  public boolean interruptAsserted() {
    if (!interruptsEnabled) return false;
    if (matchInterruptEnabled && matchInterruptAsserted) {
      return true;
    } else if (overflowInterruptEnabled && overflowInterruptAsserted) {
      return true;
    } else {
      return false;
    }
  }
  
  @Override
  public void acknowledgeInterrupt() {
    // we don't need to do anything special here as we
    // have our own interrupt acknowledge register
  }

}
