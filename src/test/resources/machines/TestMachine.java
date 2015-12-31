package machines;

import io.lp0onfire.ssi.model.Machine;
import io.lp0onfire.ssi.model.Vector;

/**
 * A completely uninteresting test machine.
 */
public class TestMachine extends Machine {

  @Override
  public int getNumberOfManipulators() {
    return 0;
  }

  @Override
  public ManipulatorType getManipulatorType(int mIdx) {
    return null;
  }

  @Override
  public boolean impedesXYMovement() {
    return false;
  }

  @Override
  public boolean impedesZMovement() {
    return false;
  }

  @Override
  public boolean impedesXYFluidFlow() {
    return false;
  }

  @Override
  public boolean impedesZFluidFlow() {
    return false;
  }

  @Override
  public boolean supportsOthers() {
    return false;
  }

  @Override
  public boolean needsSupport() {
    return false;
  }

  @Override
  public boolean canMove() {
    return false;
  }

  @Override
  public int getType() {
    return 0;
  }

  @Override
  public Vector getExtents() {
    return new Vector(1, 1, 1);
  }

}
