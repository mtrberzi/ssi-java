package io.lp0onfire.ssi.model.robots;

import io.lp0onfire.ssi.microcontroller.Microcontroller;

public class MiningRobotMarkOne extends AxialThrustRobot {

  public MiningRobotMarkOne(Microcontroller mcu) {
    super(mcu);
    // TODO
    throw new UnsupportedOperationException("not implemented yet");
  }

  @Override
  public int engine_getIdentification(int eIdx) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  protected double getThrusterForce() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  protected int getThrusterMaxPowerLevel() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  protected int getThrusterPowerSlewRate() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public double getMass() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int getNumberOfManipulators() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public ManipulatorType getManipulatorType(int mIdx) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int getType() {
    // TODO Auto-generated method stub
    return 0;
  }

}
