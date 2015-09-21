package io.lp0onfire.ssi.model.structures;

import static org.junit.Assert.*;
import io.lp0onfire.ssi.model.Machine;
import io.lp0onfire.ssi.model.MaterialLibrary;
import io.lp0onfire.ssi.model.Vector;
import io.lp0onfire.ssi.model.World;

import org.junit.Test;

public class TestRamp {

  class TestObject extends Machine {

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
      return true;
    }

    @Override
    public boolean canMove() {
      return true;
    }

    @Override
    public Vector getExtents() {
      return new Vector(1, 1, 1);
    }
    
    @Override
    public boolean hasWorldUpdates() {
      return false;
    }
    
    @Override
    public int getType() {
      return 0;
    }

    @Override
    public int getNumberOfManipulators() {
      return 0;
    }

    @Override
    public ManipulatorType getManipulatorType(int mIdx) {
      return null;
    }
    
  };
  
  @Test
  public void testLeftRamp() {
    World w = new World(10, 10);
    // add a left-facing ramp at (2, 2, 1)
    Ramp ramp = new LeftRamp(MaterialLibrary.getInstance().getMaterial("bedrock"));
    assertTrue(w.addOccupant(new Vector(2, 2, 1), new Vector(0, 0, 0), ramp));
    // add a test vehicle at (3, 2, 1)
    Machine vehicle = new TestObject();
    assertTrue(w.addOccupant(new Vector(3, 2, 1), new Vector(0, 0, 0), vehicle));
    // add a floor at (1, 2, 2)
    Floor floor = new Floor(MaterialLibrary.getInstance().getMaterial("bedrock"));
    assertTrue(w.addOccupant(new Vector(1, 2, 2), new Vector(0, 0, 0), floor));
    // move the vehicle left by 2
    vehicle.setVelocity(new Vector(-2, 0, 0));
    w.timestep();
    // now it should be on top of the floor at (1, 2, 2)
    assertEquals("didn't go up the ramp", new Vector(1, 2, 2), vehicle.getPosition());
    // move the vehicle right by 2
    vehicle.setVelocity(new Vector(2, 0, 0));
    w.timestep();
    // now it should be back where it started
    assertEquals("didn't go back down the ramp", new Vector(3, 2, 1), vehicle.getPosition());
  }
  
}
