package io.lp0onfire.ssi.model;

import static org.junit.Assert.*;
import io.lp0onfire.ssi.model.structures.Bedrock;

import java.util.List;
import java.util.Set;

import org.junit.Test;

public class TestWorld {

  @Test
  public void testWorldCreation() {
    new World(10, 5);
  }
  
  @Test
  public void testBedrockLayerCreation() {
    // z=0 should always be filled with bedrock
    World w = new World(10, 5);
    for (int y = 0; y < y; ++y) {
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
  
}
