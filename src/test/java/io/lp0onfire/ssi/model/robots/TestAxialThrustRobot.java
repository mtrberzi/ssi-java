package io.lp0onfire.ssi.model.robots;

import static org.junit.Assert.*;
import io.lp0onfire.ssi.TimeConstants;
import io.lp0onfire.ssi.microcontroller.Microcontroller;
import io.lp0onfire.ssi.model.Vector;
import io.lp0onfire.ssi.model.World;

import org.junit.Before;
import org.junit.Test;

public class TestAxialThrustRobot {

  private static final Integer ENGINE_FULL_POWER = 256;
  
  class TestRobot extends AxialThrustRobot {

    public TestRobot(Microcontroller mcu) {
      super(mcu);
    }

    @Override
    public int engine_getIdentification(int eIdx) {
      return 0;
    }

    @Override
    protected double getThrusterForce() {
      return 1.0;
    }

    @Override
    protected int getThrusterMaxPowerLevel() {
      return ENGINE_FULL_POWER;
    }

    @Override
    protected int getThrusterPowerSlewRate() {
      return ENGINE_FULL_POWER;
    }

    @Override
    public double getMass() {
      return 1.0;
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
    public int getType() {
      return 0;
    }
    
  }
  
  private Microcontroller mcu;
  
  @Before
  public void setup() {
    mcu = new Microcontroller(4, 4);
    // TODO maybe attach a fake ROM here so the mcu doesn't immediately bus-error?
  }
  
  @Test
  public void testThrust_PositiveX() {
    World w = new World(10, 10);
    // create our robot at (5, 5, 1) somewhere in the middle of the voxel
    TestRobot robot = new TestRobot(mcu);
    assertTrue(w.addOccupant(
        new Vector(5, 5, 1), 
        new Vector(TimeConstants.SUBVOXELS_PER_VOXEL / 2, TimeConstants.SUBVOXELS_PER_VOXEL / 2, TimeConstants.SUBVOXELS_PER_VOXEL / 2), 
        robot));
    // fire positive-X thruster at full power
    robot.engine_setControl(0, 256);
    w.timestep();
    // the thruster can fire instantaneously, and the force is 1.0 kg-voxels/timestep^2
    // from 0, the average acceleration during the first timestep is 0.5 voxels/timestep^2
    // so right here, we should either be at (6, 5.5, 1.5) or very close to it
    double posX = (double)robot.getPosition().getX() + (double)robot.getSubvoxelPosition().getX() / (double)TimeConstants.SUBVOXELS_PER_VOXEL;
    double posY = (double)robot.getPosition().getY() + (double)robot.getSubvoxelPosition().getY() / (double)TimeConstants.SUBVOXELS_PER_VOXEL;
    double posZ = (double)robot.getPosition().getZ() + (double)robot.getSubvoxelPosition().getZ() / (double)TimeConstants.SUBVOXELS_PER_VOXEL;
    
    assertEquals("incorrect X position", 6.0, posX, 0.1);
    assertEquals("incorrect Y position", 5.5, posY, 0.1);
    assertEquals("incorrect Z position", 1.5, posZ, 0.1);
  }
  
}
