package io.lp0onfire.ssi.microcontroller.peripherals;

import io.lp0onfire.ssi.microcontroller.AddressTrapException;
import io.lp0onfire.ssi.microcontroller.SystemBusPeripheral;
import io.lp0onfire.ssi.model.Robot;
import io.lp0onfire.ssi.model.Vector;

public class MiningLaserController implements SystemBusPeripheral {

  private Robot robot;
  
  public MiningLaserController(Robot r) {
    this.robot = r;
  }
  
  @Override
  public int getNumberOfPages() {
    return 1;
  }
  
  private int targetX = 0;
  private int targetY = 0;
  private int targetZ = 0;
  private int powerLevel = 0;
  
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
    case 0x0: // Target X-Displacement
      return targetX;
    case 0x4: // Target Y-Displacement
      return targetY;
    case 0x8: // Target Z-Displacement
      return targetZ;
    case 0xC: // Power Control
      return powerLevel;
    default:
      throw new AddressTrapException(5, pAddr);
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
    case 0x0: // Target X-Displacement
      targetX = value; break;
    case 0x4: // Target Y-Displacement
      targetY = value; break;
    case 0x8: // Target Z-Displacement
      targetZ = value; break;
    case 0xC: // Power Control
      powerLevel = value; break;
    default:
      throw new AddressTrapException(7, pAddr);
    }
  }

  @Override
  public void cycle() {
  }

  @Override
  public void timestep() {
    if (powerLevel > 0) {
      robot.miningLaser_fire(new Vector(targetX, targetY, targetZ), powerLevel);
    }
  }
  
}
