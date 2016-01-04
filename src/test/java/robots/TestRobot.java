package robots;

import io.lp0onfire.ssi.microcontroller.Microcontroller;
import io.lp0onfire.ssi.model.Robot;

/**
 * A completely uninteresting test robot.
 */
public class TestRobot extends Robot {

  public TestRobot(Microcontroller mcu) {
    super(mcu);
  }

  @Override
  public double getMass() {
    return 1.0;
  }

  @Override
  public int getNumberOfEngines() {
    return 0;
  }

  @Override
  public int engine_getIdentification(int eIdx) {
    return 0;
  }

  @Override
  public int engine_getStatus(int eIdx) {
    return 0;
  }

  @Override
  public void engine_setControl(int eIdx, int powerLevel) {
  }

  @Override
  protected void engine_timestep() {
  }

  @Override
  public int getNumberOfManipulators() {
    return 0;
  }

  @Override
  public ManipulatorType getManipulatorType(int mIdx) {
    return null;
  }

  @Override
  public boolean needsSupport() {
    return false;
  }

  @Override
  public int getType() {
    return 0;
  }
  
}
