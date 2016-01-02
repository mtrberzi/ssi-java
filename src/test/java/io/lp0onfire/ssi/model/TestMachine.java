package io.lp0onfire.ssi.model;

import static org.junit.Assert.*;
import io.lp0onfire.ssi.ParseException;
import io.lp0onfire.ssi.microcontroller.AddressTrapException;
import io.lp0onfire.ssi.microcontroller.Microcontroller;
import io.lp0onfire.ssi.microcontroller.peripherals.InventoryController;
import io.lp0onfire.ssi.model.items.Component;
import io.lp0onfire.ssi.model.items.ComponentBuilder;
import io.lp0onfire.ssi.model.items.ComponentLibrary;
import io.lp0onfire.ssi.model.reactions.CreatedMachineBuilder;
import io.lp0onfire.ssi.model.reactions.CreatedObject;
import io.lp0onfire.ssi.model.reactions.CreatedRobotBuilder;
import io.lp0onfire.ssi.model.reactions.MaterialCategoryConstraint;
import io.lp0onfire.ssi.model.reactions.Product;
import io.lp0onfire.ssi.model.reactions.ProductBuilder;
import io.lp0onfire.ssi.model.reactions.Reactant;
import io.lp0onfire.ssi.model.reactions.ReactantBuilder;
import io.lp0onfire.ssi.model.reactions.Reaction;
import io.lp0onfire.ssi.model.reactions.ReactionBuilder;
import io.lp0onfire.ssi.model.reactions.ReactionLibrary;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

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
    metalBuilder.setDurabilityModifier(1.0);
    metalBuilder.setCategories(Arrays.asList("metal"));
    metal = metalBuilder.build();
    
    MaterialBuilder nonMetalBuilder = new MaterialBuilder();
    nonMetalBuilder.setMaterialName("fakelite");
    nonMetalBuilder.setType(0);
    nonMetalBuilder.setDurabilityModifier(1.0);
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
    {
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
    
    /*
     * our test robot-assembly reaction is:
     * 1 bogus1 -> 1 TestRobot
     */
    {
      List<Reactant> reactants = new LinkedList<>();
      ReactantBuilder rBuilder = new ReactantBuilder();
      rBuilder.setQuantity(1);
      rBuilder.setComponentName("bogus1");
      reactants.add(rBuilder.build());
      
      List<CreatedObject> objs = new LinkedList<>();
      CreatedRobotBuilder oBuilder = new CreatedRobotBuilder();
      oBuilder.setRobotClass("robots.TestRobot");
      objs.add(oBuilder.build());
      
      ReactionBuilder reactionBuilder = new ReactionBuilder();
      reactionBuilder.setReactionID(9002);
      reactionBuilder.setReactionName("build test robot");
      reactionBuilder.setReactionTime(1);
      reactionBuilder.setCategories(Arrays.asList("robot-assembly-1"));
      reactionBuilder.setReactants(reactants);
      reactionBuilder.setCreatedObjects(objs);
      Reaction rx = reactionBuilder.build();
      ReactionLibrary.getInstance().addReaction("robot-assembly-1", rx);
    }
    
    /*
     * our test machine-assembly reaction is:
     * 1 bogus1 -> 1 TestMachine
     */
    {
      List<Reactant> reactants = new LinkedList<>();
      ReactantBuilder rBuilder = new ReactantBuilder();
      rBuilder.setQuantity(1);
      rBuilder.setComponentName("bogus1");
      reactants.add(rBuilder.build());
      
      List<CreatedObject> objs = new LinkedList<>();
      CreatedMachineBuilder oBuilder = new CreatedMachineBuilder();
      oBuilder.setMachineClass("machines.TestMachine");
      oBuilder.setRequiresMCU(false);
      objs.add(oBuilder.build());
      
      ReactionBuilder reactionBuilder = new ReactionBuilder();
      reactionBuilder.setReactionID(9003);
      reactionBuilder.setReactionName("construct test machine");
      reactionBuilder.setReactionTime(1);
      reactionBuilder.setCategories(Arrays.asList("construction-1"));
      reactionBuilder.setReactants(reactants);
      reactionBuilder.setCreatedObjects(objs);
      Reaction rx = reactionBuilder.build();
      ReactionLibrary.getInstance().addReaction("construction-1", rx);
    }
    
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
  
  // A test machine with a robot assembler (#0)
  class TestRobotBuilder extends Machine {

    @Override
    public int getNumberOfManipulators() {
      return 1;
    }

    @Override
    public ManipulatorType getManipulatorType(int mIdx) {
      if (mIdx == 0) {
        return ManipulatorType.ROBOT_ASSEMBLER;
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
  
  // A test machine with a field assembly device (#0)
  class TestMachineBuilder extends Machine {

    @Override
    public int getNumberOfManipulators() {
      return 1;
    }

    @Override
    public ManipulatorType getManipulatorType(int mIdx) {
      if (mIdx == 0) {
        return ManipulatorType.FIELD_ASSEMBLY_DEVICE;
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
  public void inttest_MachineBuildsRobot() throws AddressTrapException {
    World w = new World(2, 2);
    
    // make sure we can get the reaction
    Reaction reaction = ReactionLibrary.getInstance().getReactionByID(9002);
    assertNotNull(reaction);
    
    TestRobotBuilder machine = new TestRobotBuilder();
    InventoryController controller = new InventoryController(machine, 4);
    
    assertTrue(w.addOccupant(new Vector(0,0,1), new Vector(0,0,0), machine));
    
    for (int i = 0; i < 1; ++i) {
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
    
    // run the command GIVE #0, 0H once
    for (int pass = 0; pass < 1; ++pass) {
      controller.writeHalfword(0x0, 0b1001000000000000);
      controller.cycle(); checkNoErrors(controller); controller.timestep(); controller.cycle(); checkNoErrors(controller);
      assertEquals("GIVE command did not execute (pass " + pass + ")", 0, numberOfOutstandingCommands(controller));
    }
    
    // now look into the machine and see if we are reacting
    assertTrue("reaction did not start", machine.manipulator_isReacting(0));
    
    // timestep the machine until the reaction is done
    for (int timestep = 0; timestep < reaction.getTime(); ++timestep) {
      controller.timestep();
      w.timestep();
    }
    
    assertFalse("reaction not finished", machine.manipulator_isReacting(0));
    
    // now check (0, 0, 1) for the robot we should have just made
    List<Robot> robots = w.getOccupants(new Vector(0, 0, 1)).stream()
        .filter((o -> o instanceof Robot)).map((o -> (Robot)o)).collect(Collectors.toList());
    
    assertFalse("no robots were created and placed into the world", robots.isEmpty());
    assertEquals("too many robots were created", 1, robots.size());
    Robot r = robots.get(0);
    assertTrue("wrong kind of robot", r instanceof robots.TestRobot);
  }
  
  @Test
  public void inttest_MachineBuildsMachine() throws AddressTrapException {
    World w = new World(2, 2);
    
    // make sure we can get the reaction
    Reaction reaction = ReactionLibrary.getInstance().getReactionByID(9003);
    assertNotNull(reaction);
    
    TestMachineBuilder builder = new TestMachineBuilder();
    InventoryController controller = new InventoryController(builder, 4);
    
    assertTrue(w.addOccupant(new Vector(0,0,1), new Vector(0,0,0), builder));
    
    for (int i = 0; i < 1; ++i) {
      controller.getObjectBuffer(0).addLast(ComponentLibrary.getInstance().createComponent("bogus1", metal));
    }
    
    // load and SET reaction ID
    controller.writeWord(0x50, reaction.getID());
    controller.writeHalfword(0x0, 0b1100000000000000);
    
    // cycle the controller a couple of times
    for (int i = 0; i < 3; ++i) {
      controller.cycle(); checkNoErrors(controller); w.timestep(); checkNoErrors(controller);
    }
    assertEquals("SET command did not execute", 0, numberOfOutstandingCommands(controller));
    // this should have created a StagingArea at (0, 0, 1)
    List<StagingArea> stagingAreas = w.getOccupants(new Vector(0, 0, 1)).stream()
        .filter((o -> o instanceof StagingArea)).map((o -> (StagingArea)o)).collect(Collectors.toList());
    assertFalse("staging area not created", stagingAreas.isEmpty());
    assertEquals("too many staging areas created", 1, stagingAreas.size());
    StagingArea staging = stagingAreas.get(0);
    assertEquals("wrong reaction set on staging area", reaction.getID(), staging.getReaction().getID());
    
    // now add the item to the staging area by running GIVE #0, 0H
    controller.writeHalfword(0x0, 0b1001000000000000);
    controller.cycle(); checkNoErrors(controller);
    w.timestep(); // this applies the AddToStagingAreaUpdate
    w.timestep(); // and this gives the staging area a chance to build
    stagingAreas = w.getOccupants(new Vector(0, 0, 1)).stream()
        .filter((o -> o instanceof StagingArea)).map((o -> (StagingArea)o)).collect(Collectors.toList());
    assertTrue("staging area not removed", stagingAreas.isEmpty());
    // check for our TestMachine
    List<Machine> machines = w.getOccupants(new Vector(0, 0, 1)).stream()
        .filter((o -> o instanceof Machine)).map((o -> (Machine)o)).collect(Collectors.toList());
    boolean found = false;
    for (Machine machine : machines) {
      if (machine instanceof machines.TestMachine) {
        found = true;
        break;
      }
    }
    assertTrue("test machine not built", found);
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
