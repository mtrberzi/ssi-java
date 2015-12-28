package io.lp0onfire.ssi.model;

import static org.junit.Assert.*;
import io.lp0onfire.ssi.ParseException;
import io.lp0onfire.ssi.microcontroller.AddressTrapException;
import io.lp0onfire.ssi.microcontroller.peripherals.InventoryController;
import io.lp0onfire.ssi.model.items.Component;
import io.lp0onfire.ssi.model.items.ComponentBuilder;
import io.lp0onfire.ssi.model.items.ComponentLibrary;
import io.lp0onfire.ssi.model.reactions.MaterialCategoryConstraint;
import io.lp0onfire.ssi.model.reactions.Product;
import io.lp0onfire.ssi.model.reactions.ProductBuilder;
import io.lp0onfire.ssi.model.reactions.Reactant;
import io.lp0onfire.ssi.model.reactions.ReactantBuilder;
import io.lp0onfire.ssi.model.reactions.Reaction;
import io.lp0onfire.ssi.model.reactions.ReactionBuilder;
import io.lp0onfire.ssi.model.reactions.ReactionLibrary;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestMachine {

  private static Material metal;
  private static Material nonmetal;
  
  @BeforeClass
  public static void setupclass() throws ParseException {
    ComponentLibrary.getInstance().clear();
    ReactionLibrary.getInstance().clear();
    
    // create a fake metal and non-metal material
    MaterialBuilder metalBuilder = new MaterialBuilder();
    metalBuilder.setMaterialName("bogusite");
    metalBuilder.setType(0);
    metalBuilder.setCategories(Arrays.asList("metal"));
    metal = metalBuilder.build();
    
    MaterialBuilder nonMetalBuilder = new MaterialBuilder();
    nonMetalBuilder.setMaterialName("fakelite");
    nonMetalBuilder.setType(0);
    nonmetal = nonMetalBuilder.build();
    
    ComponentBuilder cBuilder1 = new ComponentBuilder();
    cBuilder1.setComponentName("bogus1");
    cBuilder1.setType(0);
    ComponentLibrary.getInstance().addComponent(cBuilder1);
    
    ComponentBuilder cBuilder2 = new ComponentBuilder();
    cBuilder2.setComponentName("bogus2");
    cBuilder2.setType(0);
    ComponentLibrary.getInstance().addComponent(cBuilder2);
    
    /*
     * our test reaction is:
     * 2 [metal] bogus1 -> 3 [#0] bogus2
     */
    List<Reactant> reactants = new LinkedList<>();
    ReactantBuilder rBuilder = new ReactantBuilder();
    rBuilder.setQuantity(2);
    rBuilder.setComponentName("bogus1");
    rBuilder.setConstraints(Arrays.asList(new MaterialCategoryConstraint("metal")));
    reactants.add(rBuilder.build());
    
    List<Product> products = new LinkedList<>();
    ProductBuilder pBuilder = new ProductBuilder();
    pBuilder.setQuantity(3);
    pBuilder.setComponentName("bogus2");
    pBuilder.setCopiedMaterial(0);
    products.add(pBuilder.build());
    
    ReactionBuilder reactionBuilder = new ReactionBuilder();
    reactionBuilder.setReactionID(9001);
    reactionBuilder.setReactionName("fabricate bogus2");
    reactionBuilder.setReactionTime(1);
    reactionBuilder.setCategories(Arrays.asList("part-builder-1"));
    reactionBuilder.setReactants(reactants);
    reactionBuilder.setProducts(products);
    Reaction rx = reactionBuilder.build();
    ReactionLibrary.getInstance().addReaction("part-builder-1", rx);
  }
  
  @AfterClass
  public static void finishclass() {
    ComponentLibrary.getInstance().clear();
    ReactionLibrary.getInstance().clear();
  }
  
  // A test machine with a part builder manipulator (#0)
  class TestPartBuilder extends Machine {

    @Override
    public int getNumberOfManipulators() {
      return 1;
    }

    @Override
    public ManipulatorType getManipulatorType(int mIdx) {
      if (mIdx == 0) {
        return ManipulatorType.PART_BUILDER;
      } else return null;
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
  
  // Integration test for simple reactions -- requires behaviour from InventoryController
  
  private String getErrorString(short code) {
    for (InventoryController.ErrorCode err : InventoryController.ErrorCode.values()) {
      if (err.getCode() == code) {
        return err.toString();
      }
    }
    return "UNKNOWN";
  }
  
  private void checkNoErrors(InventoryController controller) throws AddressTrapException {
    int status = controller.readWord(0x0);
    if ((status & (1<<3)) != 0) {
      // error
      int errorStatus = controller.readWord(0x4);
      int errorInsn = (errorStatus & 0xFFFF0000) >>> 16;
      short errorCode = (short)(errorStatus & 0x0000FFFF);
      fail("command queue error: insn=0b" + Integer.toBinaryString(errorInsn) + ", code=" + errorCode + "(" + getErrorString(errorCode) + ")");
    }
  }
  
  private int numberOfOutstandingCommands(InventoryController controller) throws AddressTrapException {
    int status = controller.readWord(0x0);
    return (status & 0x00000007);
  }
  
  @Test
  public void inttest_MachinePerformsReaction() throws AddressTrapException {
    // make sure we can get the reaction
    Reaction reaction = ReactionLibrary.getInstance().getReactionByID(9001);
    assertNotNull(reaction);
    
    TestPartBuilder machine = new TestPartBuilder();
    InventoryController controller = new InventoryController(machine, 4);
    
    for (int i = 0; i < 2; ++i) {
      controller.getObjectBuffer(0).addLast(ComponentLibrary.getInstance().createComponent("bogus1", metal));
    }
    
    // load the reaction ID into ID register 0, and SET that reaction on manipulator 0
    controller.writeWord(0x50, reaction.getID());
    controller.writeHalfword(0x0, 0b1100000000000000);
    
    // cycle the controller a couple of times
    for (int i = 0; i < 3; ++i) {
      controller.cycle(); checkNoErrors(controller);
    }
    assertEquals("SET command did not execute", 0, numberOfOutstandingCommands(controller));
    
    // run the command GIVE #0, 0H two times
    for (int pass = 0; pass < 2; ++pass) {
      controller.writeHalfword(0x0, 0b1001000000000000);
      controller.cycle(); checkNoErrors(controller); controller.timestep(); controller.cycle(); checkNoErrors(controller);
      assertEquals("GIVE command did not execute (pass " + pass + ")", 0, numberOfOutstandingCommands(controller));
    }
    
    // now look into the machine and see if we are reacting
    assertTrue("reaction did not start", machine.manipulator_isReacting(0));
    
    // timestep the machine until the reaction is done
    for (int timestep = 0; timestep < reaction.getTime(); ++timestep) {
      machine.timestep();
    }
    
    assertFalse("reaction not finished", machine.manipulator_isReacting(0));
    
    // run the command NEXT 1T, #0 three times to retrieve the output products
    for (int pass = 0; pass < 3; ++pass) {
      controller.writeHalfword(0x0, 0b1001010001100000);
      controller.cycle(); checkNoErrors(controller); controller.timestep(); controller.cycle(); checkNoErrors(controller);
      assertEquals("NEXT command did not execute (pass " + pass + ")", 0, numberOfOutstandingCommands(controller));
      // check that the last item in object buffer #1 is a bogus2
      assertFalse("no items in object buffer 1", controller.getObjectBuffer(1).isEmpty());
      Item i = controller.getObjectBuffer(1).peekLast();
      assertTrue(i instanceof Component);
      Component product = (Component)i;
      assertEquals("incorrect reaction product", "bogus2", product.getComponentName());
    }
  }
  
  @Test
  public void inttest_MachineReactor_MSTAT() throws AddressTrapException {
    // make sure we can get the reaction
    Reaction reaction = ReactionLibrary.getInstance().getReactionByID(9001);
    assertNotNull(reaction);
    
    TestPartBuilder machine = new TestPartBuilder();
    InventoryController controller = new InventoryController(machine, 4);
    
    for (int i = 0; i < 2; ++i) {
      controller.getObjectBuffer(0).addLast(ComponentLibrary.getInstance().createComponent("bogus1", metal));
    }
    
    // load the reaction ID into ID register 0, and SET that reaction on manipulator 0
    controller.writeWord(0x50, reaction.getID());
    controller.writeHalfword(0x0, 0b1100000000000000);
    
    // cycle the controller a couple of times
    for (int i = 0; i < 3; ++i) {
      controller.cycle(); checkNoErrors(controller);
    }
    assertEquals("SET command did not execute", 0, numberOfOutstandingCommands(controller));
  }
  
}
