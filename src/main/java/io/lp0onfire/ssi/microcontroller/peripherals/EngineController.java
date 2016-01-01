package io.lp0onfire.ssi.microcontroller.peripherals;

import io.lp0onfire.ssi.microcontroller.AddressTrapException;
import io.lp0onfire.ssi.microcontroller.SystemBusPeripheral;
import io.lp0onfire.ssi.model.Robot;

public class EngineController implements SystemBusPeripheral {

  private Robot robot;
  
  public EngineController(Robot r) {
    this.robot = r;
    
    for (int i = 0; i < 32; ++i) {
      enginePowerLevel[i] = 0;
      engineThrottle[i] = 0x7FFF;
    }
  }
  
  @Override
  public int getNumberOfPages() {
    return 1;
  }

  private boolean masterEmergencyStop = false;
  private int masterThrottle = 0x7FFF;
  
  private int[] enginePowerLevel = new int[32];
  private int[] engineThrottle = new int[32];
  
  protected int readEngineIDRegister(int eIdx) {
    if (eIdx < 0 || eIdx >= robot.getNumberOfEngines()) {
      return 0;
    }
    return robot.engine_getIdentification(eIdx);
  }
  
  protected int readEngineStatusRegister(int eIdx) {
    if (eIdx < 0 || eIdx >= robot.getNumberOfEngines()) {
      return 0;
    }
    return robot.engine_getStatus(eIdx);
  }
  
  protected int readEngineControlRegister(int eIdx) {
    if (eIdx < 0 || eIdx >= robot.getNumberOfEngines()) {
      return 0;
    }
    int val = 0;
    // bits 15-0: engine power level
    val |= (enginePowerLevel[eIdx] & 0x0000FFFF);
    return val;
  }
  
  protected void writeEngineControlRegister(int eIdx, int val) {
    if (eIdx < 0 || eIdx >= robot.getNumberOfEngines()) {
      return;
    }
    // bits 15-0: engine power level
    // mask and sign-extend to int
    short powerLevel = (short) (val & 0x0000FFFF);
    enginePowerLevel[eIdx] = (int)powerLevel;
  }
  
  protected int readEngineThrottleRegister(int eIdx) {
    if (eIdx < 0 || eIdx >= robot.getNumberOfEngines()) {
      return 0;
    }
    int val = 0;
    // bits 14-0: throttle absolute value
    val |= (engineThrottle[eIdx] & 0x00007FFF);
    return val;
  }
  
  protected void writeEngineThrottleRegister(int eIdx, int val) {
    if (eIdx < 0 || eIdx >= robot.getNumberOfEngines()) {
      return;
    }
    // bits 14-0: throttle absolute value
    short throttle = (short) (val & 0x00007FFF);
    engineThrottle[eIdx] = (int)throttle;
  }
  
  protected int readEngineRegister(int addr) {
    // Engine Identification Registers are at 0x10, 0x20, ..., 0x210
    // Engine Throttle Registers are at 0x1C, 0x2C, ..., 0x21C
    
    int engineIndex = (addr - 0x10) / 0x10;
    int regType = addr & 0x0F;
    
    switch (regType) {
    case 0x0: // engine ID
      return readEngineIDRegister(engineIndex);
    case 0x4: // engine status
      return readEngineStatusRegister(engineIndex);
    case 0x8: // engine control
      return readEngineControlRegister(engineIndex);
    case 0xC: // engine throttle
      return readEngineThrottleRegister(engineIndex);
    default:
      throw new IllegalArgumentException("unknown engine register " + addr);
    }
  }
  
  protected void writeEngineRegister(int addr, int val) {
    int engineIndex = (addr - 0x10) / 0x10;
    int regType = addr & 0x0F;
    
    switch (regType) {
    case 0x8: // engine control
      writeEngineControlRegister(engineIndex, val); break;
    case 0xC: // engine throttle
      writeEngineThrottleRegister(engineIndex, val); break;
    default:
      throw new IllegalArgumentException("unknown engine register " + addr);
    }
  }
  
  protected int readPlatformID() {
    int val = 0;
    // bits 4 - 0: number of engines
    val |= robot.getNumberOfEngines() & 0x0000001F;
    return val;
  }
  
  protected int readMasterControl() {
    int val = 0;
    // bit 31: master emergency stop
    if (masterEmergencyStop) {
      val |= (1<<31);
    }
    return val;
  }
  
  protected void writeMasterControl(int val) {
    // bit 31: master emergency stop
    masterEmergencyStop = ((val & (1<<31)) != 0);
  }
  
  protected int readMasterThrottle() {
    int val = 0;
    // bits 14-0: throttle absolute value
    val |= masterThrottle;
    return val;
  }
  
  protected void writeMasterThrottle(int val) {
    // bits 14-0: throttle absolute value
    masterThrottle = val & 0x00003FFF;
  }
  
  // TODO we may want to allow read/write halfword for some of these registers
  // because thrust is always a 16-bit quantity
  
  @Override
  public int readByte(int address) throws AddressTrapException {
    throw new AddressTrapException(4, address);
  }

  @Override
  public int readHalfword(int address) throws AddressTrapException {
    throw new AddressTrapException(4, address);
  }

  @Override
  public int readWord(int pAddr) throws AddressTrapException {
    int addr = translateAddress(pAddr);
    switch (addr) {
    case 0x0: // Platform ID
      return readPlatformID();
    case 0x4: // Controller status
      // TODO controller status register
      return 0;
    case 0x8: // Master Control
      return readMasterControl();
    case 0xC: // Master Throttle
      return readMasterThrottle();
    default:
      if (addr >= 0x10 && addr < 0x220) {
        // Engine Register
        return readEngineRegister(addr);
      } else {
        throw new AddressTrapException(5, pAddr);
      }
    }
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
  public void writeWord(int pAddr, int value) throws AddressTrapException {
    int addr = translateAddress(pAddr);
    switch (addr) {
    case 0x8: // Master Control
      writeMasterControl(value); break;
    case 0xC: // Master Throttle
      writeMasterThrottle(value); break;
    default:
      if (addr >= 0x10 && addr < 0x220) {
        // Engine Register
        writeEngineRegister(addr, value); break;
      } else {
        throw new AddressTrapException(7, pAddr);
      }
    }
  }

  @Override
  public void cycle() {
  }

  @Override
  public void timestep() {
    // for each engine, determine the actual power level
    for (int i = 0; i < robot.getNumberOfEngines(); ++i) {
      int powerLevel;
      if (masterEmergencyStop) {
        // this overrides everything
        powerLevel = 0;
      } else {
        powerLevel = enginePowerLevel[i];
        int throttle = Math.min(masterThrottle, engineThrottle[i]);
        if (Math.abs(powerLevel) > throttle) {
          if (powerLevel < 0) {
            powerLevel = -throttle;
          } else {
            powerLevel = throttle;
          }
        }
      }
      // send engine command
      robot.engine_setControl(i, powerLevel);
    }
  }

}
