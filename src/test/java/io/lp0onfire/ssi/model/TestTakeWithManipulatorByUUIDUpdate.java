package io.lp0onfire.ssi.model;

import static org.junit.Assert.*;
import io.lp0onfire.ssi.model.items.ComponentBuilder;
import io.lp0onfire.ssi.model.items.ComponentLibrary;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestTakeWithManipulatorByUUIDUpdate {

  @Before
  public void setup() {
    ComponentLibrary.getInstance().clear();
    try {
      ComponentBuilder builder = new ComponentBuilder();
      builder.setComponentName("bar");
      builder.setType(0);
      ComponentLibrary.getInstance().addComponent(builder);
    } catch (Exception e) {
      fail("failed to add 'bar' component to library");
    }
    
    try {
      ComponentBuilder builder = new ComponentBuilder();
      builder.setComponentName("foo");
      builder.setType(0);
      ComponentLibrary.getInstance().addComponent(builder);
    } catch (Exception e) {
      fail("failed to add 'foo' component to library");
    }
  }
  
  @After
  public void finish() {
    ComponentLibrary.getInstance().clear();
  }
  
private static Material testMaterial;
  
  @BeforeClass
  public static void setupClass() {
    MaterialBuilder builder = new MaterialBuilder();
    builder.setMaterialName("bogusite");
    builder.setType(0);
    builder.setCategories(Arrays.asList("metal"));
    builder.setCanBeSmelted(false);
    testMaterial = builder.build();
  }
  
  // This test machine tries to take an object with a given UUID.
  class TestMachine extends Machine {

    private final UUID targetUUID;
    
    public TestMachine(UUID uuid) {
      this.targetUUID = uuid;
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
      return true;
    }

    @Override
    public Vector getExtents() {
      return new Vector(1, 1, 1);
    }

    protected boolean taken = false;
    private List<WorldUpdate> updates = new LinkedList<>();
    
    @Override
    public void timestep() {
      if (!taken) {
        updates.clear();
        updates.add(new TakeWithManipulatorByUUIDUpdate(this, 0, targetUUID));
      }
    }
    
    @Override
    public boolean hasWorldUpdates() {
      return !taken;
    }
    
    @Override
    public List<WorldUpdate> getWorldUpdates() {
      return updates;
    }
    
    @Override
    public void collectUpdateResults(Map<WorldUpdate, WorldUpdateResult> results) {
      for (WorldUpdateResult result : results.values()) {
        if (result.wasSuccessful()) {
          taken = true;
        }
      }
    }
    
    @Override
    public int getType() {
      return 0;
    }

    @Override
    public int getNumberOfManipulators() {
      return 1;
    }

    @Override
    public ManipulatorType getManipulatorType(int mIdx) {
      return ManipulatorType.LIGHT_ARM;
    }
    
  };
  
  @Test(timeout=5000)
  public void testTakeByUUID() {
    World w = new World(5, 10);
    // put down a test object at (0, 0, 1)
    Item obj = ComponentLibrary.getInstance().createComponent("foo", testMaterial);
    assertTrue(w.addOccupant(new Vector(0, 0, 1), new Vector(0,0,0), obj));
    // put down a test machine occupying the same space
    TestMachine machine = new TestMachine(obj.getUUID());
    assertTrue(w.addOccupant(new Vector(0, 0, 1), new Vector(0,0,0), machine));
    // run a timestep
    w.timestep();
    // taking the item should be successful
    assertTrue(machine.taken);
    // the item also shouldn't be in that location any more
    assertFalse(w.getOccupants(new Vector(0, 0, 1)).contains(obj));
  }
  
}
