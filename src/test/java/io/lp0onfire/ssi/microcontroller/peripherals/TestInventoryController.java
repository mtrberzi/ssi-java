package io.lp0onfire.ssi.microcontroller.peripherals;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.UUID;

import io.lp0onfire.ssi.microcontroller.AddressTrapException;
import io.lp0onfire.ssi.model.Item;
import io.lp0onfire.ssi.model.Machine;
import io.lp0onfire.ssi.model.Material;
import io.lp0onfire.ssi.model.MaterialBuilder;
import io.lp0onfire.ssi.model.Vector;
import io.lp0onfire.ssi.model.World;
import io.lp0onfire.ssi.model.items.ComponentBuilder;
import io.lp0onfire.ssi.model.items.ComponentLibrary;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestInventoryController {

  class TestMachine extends Machine {
    
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
  
  private static Material testMaterial;
  
  private static final int FOO_TYPE = 42;
  private static final int BAR_TYPE = 13;
  
  @BeforeClass
  public static void setupClass() {
    {
      MaterialBuilder builder = new MaterialBuilder();
      builder.setMaterialName("bogusite");
      builder.setType(0);
      builder.setCategories(Arrays.asList("metal"));
      builder.setCanBeSmelted(false);
      testMaterial = builder.build();
    }
    
    ComponentLibrary.getInstance().clear();
    try {
      ComponentBuilder builder = new ComponentBuilder();
      builder.setComponentName("foo");
      builder.setType(FOO_TYPE);
      ComponentLibrary.getInstance().addComponent(builder);
    } catch (Exception e) {
      fail("failed to add 'foo' component to library");
    }
    try {
      ComponentBuilder builder = new ComponentBuilder();
      builder.setComponentName("bar");
      builder.setType(BAR_TYPE);
      ComponentLibrary.getInstance().addComponent(builder);
    } catch (Exception e) {
      fail("failed to add 'bar' component to library");
    }
  }
  
  @AfterClass
  public static void finishClass() {
    ComponentLibrary.getInstance().clear();
  }
  
  private static final int NUMBER_OF_BUFFERS = 8;
  private Machine machine;
  private InventoryController controller;
  
  @Before
  public void setup() {
    machine = new TestMachine();
    controller = new InventoryController(machine, NUMBER_OF_BUFFERS);
  }
  
  private String getErrorString(short code) {
    for (InventoryController.ErrorCode err : InventoryController.ErrorCode.values()) {
      if (err.getCode() == code) {
        return err.toString();
      }
    }
    return "UNKNOWN";
  }
  
  private void checkNoErrors() throws AddressTrapException {
    int status = controller.readWord(0x0);
    if ((status & (1<<3)) != 0) {
      // error
      int errorStatus = controller.readWord(0x4);
      int errorInsn = (errorStatus & 0xFFFF0000) >>> 16;
      short errorCode = (short)(errorStatus & 0x0000FFFF);
      fail("command queue error: insn=" + Integer.toHexString(errorInsn) + ", code=" + errorCode + "(" + getErrorString(errorCode) + ")");
    }
  }
  
  private int numberOfOutstandingCommands() throws AddressTrapException {
    int status = controller.readWord(0x0);
    return (status & 0x00000007);
  }
  
  @Test
  public void testInstruction_MOVE() throws AddressTrapException {
    // populate buffers like so:
    // 1: [H] FOO BAR [T]
    LinkedList<Item> buffer1 = controller.getObjectBuffer(1);
    Item foo = ComponentLibrary.getInstance().createComponent("foo", testMaterial);
    buffer1.addLast(foo);
    buffer1.addLast(ComponentLibrary.getInstance().createComponent("bar", testMaterial));
    // 2: [H] BAR BAR [T]
    LinkedList<Item> buffer2 = controller.getObjectBuffer(2);
    buffer2.addLast(ComponentLibrary.getInstance().createComponent("bar", testMaterial));
    buffer2.addLast(ComponentLibrary.getInstance().createComponent("bar", testMaterial));
    // enqueue MOVE 2T, 1H
    controller.writeHalfword(0x0, 0b0000010010100010);
    // start cycling, wait for completion
    for (int cycle = 0; cycle < 50; ++cycle) {
      controller.cycle();
      checkNoErrors();
      if (controller.getNumberOfCommands() == 0) {
        // check: buffer 1 should have 1 item
        assertEquals(1, buffer1.size());
        // check: buffer 1 does not contain FOO
        for (Item i : buffer1) {
           assertNotEquals("buffer 1 still contains foo", foo, i);
        }
        // check: buffer 2 should contain 3 items
        assertEquals(3, buffer2.size());
        // check: buffer 2's tail should be FOO, and no other item in buffer 2 is FOO
        for (Item i : buffer2) {
          if (buffer2.getLast().equals(i)) {
            assertEquals("foo not last element in buffer2", foo, i);
          } else {
            assertNotEquals("foo appears elsewhere in buffer2", foo, i);
          }
        }
        // success
        return;
      }
    }
    fail("command processing timed out");
  }
  
  @Test
  public void testInstruction_TAKE() throws AddressTrapException {
    controller.setTracing(true);
    World w = new World(5, 10);
    // put down a test object at (0, 0, 1)
    Item obj = ComponentLibrary.getInstance().createComponent("foo", testMaterial);
    assertTrue(w.addOccupant(new Vector(0, 0, 1), new Vector(0,0,0), obj));
    // put down our test machine occupying the same space
    assertTrue(w.addOccupant(new Vector(0, 0, 1), new Vector(0,0,0), machine));
    // write the UUID of obj to UUID register 0
    UUID uuid = obj.getUUID();
    controller.writeWord(0x10, (int)(uuid.getLeastSignificantBits() & 0x00000000FFFFFFFFL));
    controller.writeWord(0x14, (int)((uuid.getLeastSignificantBits() & 0xFFFFFFFF00000000L) >>> 32));
    controller.writeWord(0x18, (int)(uuid.getMostSignificantBits() & 0x00000000FFFFFFFFL));
    controller.writeWord(0x1C, (int)((uuid.getMostSignificantBits() & 0xFFFFFFFF00000000L) >>> 32));
    // enqueue TAKE.0 0H, #0
    controller.writeHalfword(0x0, 0b1000000000000000);
    // now simulate some really short timesteps
    controller.cycle();
    checkNoErrors();
    w.timestep(); controller.timestep();
    controller.cycle(); checkNoErrors(); // is this necessary?
    assertEquals("command execution not complete", 0, numberOfOutstandingCommands());
    // now buffer #0 should contain the item, and the item should have been taken out
    assertFalse(w.getOccupants(new Vector(0, 0, 1)).contains(obj));
    assertTrue(controller.getObjectBuffer(0).contains(obj));
  }
  
}
