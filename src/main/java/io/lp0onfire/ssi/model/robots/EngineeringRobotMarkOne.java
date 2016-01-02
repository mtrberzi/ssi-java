package io.lp0onfire.ssi.model.robots;

import io.lp0onfire.ssi.microcontroller.Microcontroller;

public class EngineeringRobotMarkOne extends AxialThrustRobot {

  public EngineeringRobotMarkOne(Microcontroller mcu) {
    super(mcu);
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
    return 2;
  }

  @Override
  public ManipulatorType getManipulatorType(int mIdx) {
    switch (mIdx) {
    case 0: return ManipulatorType.LIGHT_ARM;
    case 1: return ManipulatorType.FIELD_ASSEMBLY_DEVICE;
    default: return null;
    }
  }

  @Override
  public int getType() {
    // TODO standardized robot identification table
    return 0;
  }

}
