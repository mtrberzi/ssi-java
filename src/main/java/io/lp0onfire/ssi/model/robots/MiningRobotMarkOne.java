package io.lp0onfire.ssi.model.robots;

import io.lp0onfire.ssi.microcontroller.Microcontroller;
import io.lp0onfire.ssi.microcontroller.peripherals.MiningLaserController;

public class MiningRobotMarkOne extends AxialThrustRobot {

  private MiningLaserController laserCtrl;
  
  public MiningRobotMarkOne(Microcontroller mcu) {
    super(mcu);
    laserCtrl = new MiningLaserController(this);
    mcu.attachMiningLaserController(laserCtrl);
  }

  @Override
  public int engine_getIdentification(int eIdx) {
    // TODO standardize engine identification table
    return 0;
  }

  @Override
  protected double getThrusterForce() {
    return 10.0;
  }

  @Override
  protected int getThrusterMaxPowerLevel() {
    return 1024;
  }

  @Override
  protected int getThrusterPowerSlewRate() {
    return 1024;
  }

  @Override
  public double getMass() {
    // TODO calculate mass from chassis material + mass of items
    return 1.0;
  }

  @Override
  public int getNumberOfManipulators() {
    return 1;
  }

  @Override
  public ManipulatorType getManipulatorType(int mIdx) {
    if (mIdx == 0) {
      return ManipulatorType.LIGHT_ARM;
    } else {
      return null;
    }
  }

  @Override
  public int getType() {
    // TODO standardized robot identification table
    return 0;
  }
  
  public boolean hasMiningLaser() {
    return true;
  }
  public int miningLaser_getMaximumPowerLevel() { 
    return 256;
  }
  public int miningLaser_getMaximumDamage() { 
    return 5; 
  }

}
