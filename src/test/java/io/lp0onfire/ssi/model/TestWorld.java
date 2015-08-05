package io.lp0onfire.ssi.model;

import static org.junit.Assert.*;
import io.lp0onfire.ssi.model.structures.Bedrock;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Test;

public class TestWorld {

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
    
  };
  
  @Test
  public void testWorldCreation() {
    new World(10, 5);
  }
  
  @Test
  public void testBedrockLayerCreation() {
    // z=0 should always be filled with bedrock
    World w = new World(10, 5);
    for (int y = 0; y < 5; ++y) {
      for (int x = 0; x < 10; ++x) {
        Vector pos = new Vector(x, y, 0);
        Set<VoxelOccupant> occupants = w.getOccupants(pos);
        assertEquals(1, occupants.size());
        for (VoxelOccupant occ : occupants) {
          assertTrue(occ instanceof Bedrock);  
        }
      }
    }
  }
  
  @Test
  public void testInBounds_AllZeroes() {
    World w = new World(10, 5);
    // (0, 0, 0) is in bounds
    assertTrue(w.inBounds(new Vector(0, 0, 0)));
  }
  
  @Test
  public void testInBounds_Standard() {
    World w = new World(10, 5);
    // (2, 3, 1) is in bounds
    assertTrue(w.inBounds(new Vector(2, 3, 1)));
  }
  
  @Test
  public void testInBounds_AtEdge() {
    World w = new World(10, 5);
    // (9, 4, 0) is in bounds
    assertTrue(w.inBounds(new Vector(9, 4, 0)));
  }
  
  @Test
  public void testInBounds_NegativeX() {
    World w = new World(10, 5);
    // (-1, 3, 1) is not in bounds
    assertFalse(w.inBounds(new Vector(-1, 3, 1)));
  }
  
  @Test
  public void testInBounds_NegativeY() {
    World w = new World(10, 5);
    // (2, -1, 1) is not in bounds
    assertFalse(w.inBounds(new Vector(2, -1, 1)));
  }
  
  @Test
  public void testInBounds_NegativeZ() {
    World w = new World(10, 5);
    // (2, 3, -1) is not in bounds
    assertFalse(w.inBounds(new Vector(2, 3, -1)));
  }
  
  @Test
  public void testInBounds_OverX() {
    World w = new World(10, 5);
    // (10, 3, 1) is not in bounds
    assertFalse(w.inBounds(new Vector(10, 3, 1)));
  }
  
  @Test
  public void testInBounds_OverY() {
    World w = new World(10, 5);
    // (2, 5, 1) is not in bounds
    assertFalse(w.inBounds(new Vector(2, 5, 1)));
  }
  
  @Test(timeout=5000)
  public void testRaycast_PositiveX() {
    // tracing from (1, 1, 0) by (4, 0, 0) should give us
    // (2, 1, 0), (3, 1, 0), (4, 1, 0), (5, 1, 0)
    World w = new World(10, 5);
    Vector origin = new Vector(1, 1, 0);
    Vector direction = new Vector(4, 0, 0);
    List<Vector> expected = Arrays.asList(
        new Vector(2, 1, 0),
        new Vector(3, 1, 0),
        new Vector(4, 1, 0),
        new Vector(5, 1, 0)
        );
    List<Vector> actual = w.raycast(origin, direction);
    assertArrayEquals(expected.toArray(), actual.toArray());
  }
  
  @Test(timeout=5000)
  public void testRaycast_NegativeX() {
    // tracing from (5, 1, 0) by (-4, 0, 0) should give us
    // (4, 1, 0), (3, 1, 0), (2, 1, 0), (1, 1, 0)
    World w = new World(10, 5);
    Vector origin = new Vector(5, 1, 0);
    Vector direction = new Vector(-4, 0, 0);
    List<Vector> expected = Arrays.asList(
        new Vector(4, 1, 0),
        new Vector(3, 1, 0),
        new Vector(2, 1, 0),
        new Vector(1, 1, 0)
        );
    List<Vector> actual = w.raycast(origin, direction);
    assertArrayEquals(expected.toArray(), actual.toArray());
  }
  
  @Test(timeout=5000)
  public void testMovement_Simple() {
    World w = new World(10, 5);
    // create a test object that moves at velocity (1, 0, 0)
    VoxelOccupant obj = new TestObject();
    obj.setVelocity(new Vector(1, 0, 0));
    // place it at (0, 0, 1)
    assertTrue(w.addOccupant(new Vector(0, 0, 1), new Vector(0, 0, 0), obj));
    // run one timestep
    w.timestep();
    // the object should now be at (1, 0, 1)
    assertEquals(new Vector(1, 0, 1), obj.getPosition());
    assertFalse(w.getOccupants(new Vector(0, 0, 1)).contains(obj));
    assertTrue(w.getOccupants(new Vector(1, 0, 1)).contains(obj));
  }
  
  @Test(timeout=5000)
  public void testMovement_MultipleVoxels() {
    World w = new World(10, 5);
    // create a test object that moves at velocity (2, 0, 0)
    VoxelOccupant obj = new TestObject();
    obj.setVelocity(new Vector(2, 0, 0));
    // place it at (0, 0, 1)
    assertTrue(w.addOccupant(new Vector(0, 0, 1), new Vector(0, 0, 0), obj));
    // run one timestep
    w.timestep();
    // the object should now be at (2, 0, 1)
    assertEquals(new Vector(2, 0, 1), obj.getPosition());
    assertFalse(w.getOccupants(new Vector(0, 0, 1)).contains(obj));
    assertTrue(w.getOccupants(new Vector(2, 0, 1)).contains(obj));
  }
  
  @Test(timeout=5000)
  public void testCollision_Simple() {
    World w = new World(10, 5);
    // create a test object that moves at velocity (2, 0, 0)
    VoxelOccupant obj = new TestObject();
    obj.setVelocity(new Vector(2, 0, 0));
    // place it at (0, 0, 1)
    assertTrue(w.addOccupant(new Vector(0, 0, 1), new Vector(0, 0, 0), obj));
    // we try to move to (2, 0, 1) but oops, there is a wall in the way!
    Bedrock wall = new Bedrock();
    assertTrue(w.addOccupant(new Vector(2, 0, 1), new Vector(0, 0, 0), wall));
    
    // run one timestep
    w.timestep();
    // the bedrock does not move
    assertEquals(new Vector(2, 0, 1), wall.getPosition());
    assertTrue(w.getOccupants(new Vector(2, 0, 1)).contains(wall));
    // the object should now be at (1, 0, 1)
    assertEquals(new Vector(1, 0, 1), obj.getPosition());
    assertFalse(w.getOccupants(new Vector(0, 0, 1)).contains(obj));
    assertTrue(w.getOccupants(new Vector(1, 0, 1)).contains(obj));
    assertFalse(w.getOccupants(new Vector(2, 0, 1)).contains(obj));
  }
  
}

