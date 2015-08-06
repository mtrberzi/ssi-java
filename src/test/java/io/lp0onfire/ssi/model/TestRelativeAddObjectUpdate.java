package io.lp0onfire.ssi.model;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class TestRelativeAddObjectUpdate {

  // This test object creates a new copy of itself at relative position x+1.
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
      return false;
    }

    @Override
    public boolean canMove() {
      return true;
    }

    @Override
    public Vector getExtents() {
      return new Vector(1, 1, 1);
    }

    protected boolean placementSuccessful = false;
    private List<WorldUpdate> updates = new LinkedList<>();
    
    @Override
    public void timestep() {
      if (!placementSuccessful) {
        updates.clear();
        updates.add(new RelativeAddObjectUpdate(this, new Vector(1, 0, 0), new TestObject()));
      }
    }
    
    @Override
    public boolean hasWorldUpdates() {
      return !placementSuccessful;
    }
    
    @Override
    public List<WorldUpdate> getWorldUpdates() {
      return updates;
    }
    
    @Override
    public void collectUpdateResults(Map<WorldUpdate, WorldUpdateResult> results) {
      for (WorldUpdateResult result : results.values()) {
        if (result.wasSuccessful()) {
          placementSuccessful = true;
        }
      }
    }
    
  };
  
  @Test(timeout=5000)
  public void testRelativeAddObjectUpdate() {
    World w = new World(5, 10);
    // put down a test object at (0, 0, 1)
    TestObject initialObj = new TestObject();
    assertTrue(w.addOccupant(new Vector(0, 0, 1), new Vector(0,0,0), initialObj));
    // run a timestep
    w.timestep();
    // placement should have been successful
    assertTrue(initialObj.placementSuccessful);
    // we should also see a TestObject at (1, 0, 1)
    assertTrue(w.getOccupants(new Vector(1, 0, 1)).stream().anyMatch(obj -> (obj instanceof TestObject)));
  }
  
}
