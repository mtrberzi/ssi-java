package io.lp0onfire.ssi.microcontroller.peripherals;

import static org.junit.Assert.*;
import io.lp0onfire.ssi.microcontroller.AddressTrapException;
import io.lp0onfire.ssi.microcontroller.Microcontroller;
import io.lp0onfire.ssi.model.Material;
import io.lp0onfire.ssi.model.MaterialBuilder;
import io.lp0onfire.ssi.model.MiningProduct;
import io.lp0onfire.ssi.model.OreMiningProduct;
import io.lp0onfire.ssi.model.Robot;
import io.lp0onfire.ssi.model.Vector;
import io.lp0onfire.ssi.model.World;
import io.lp0onfire.ssi.model.items.Ore;
import io.lp0onfire.ssi.model.structures.Block;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.BeforeClass;
import org.junit.Test;

public class TestMiningLaserController {
  private static Material testMaterial;
  
  @BeforeClass
  public static void setupClass() {
    MaterialBuilder builder = new MaterialBuilder();
    builder.setMaterialName("bogusite");
    builder.setType(0);
    builder.setDurabilityModifier(1.0);
    builder.setCategories(Arrays.asList("metal"));
    builder.setCanBeSmelted(false);
    List<MiningProduct> products = new LinkedList<>();
    products.add(new OreMiningProduct(1.0));
    builder.setMiningProducts(products);
    testMaterial = builder.build();
  }
  
  private static final int CONTROLLER_BASE_ADDRESS = 0xF0000000;
  
  class TestRobot extends Robot {

    public TestRobot(Microcontroller mcu) {
      super(mcu);
    }

    @Override
    public double getMass() {
      return 1.0;
    }

    @Override
    public int getNumberOfEngines() {
      return 0;
    }

    @Override
    public int engine_getIdentification(int eIdx) {
      return 0;
    }

    @Override
    public int engine_getStatus(int eIdx) {
      return 0;
    }

    @Override
    public void engine_setControl(int eIdx, int powerLevel) {
    }

    @Override
    protected void engine_timestep() {
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
    public boolean needsSupport() {
      return false;
    }

    @Override
    public int getType() {
      return 0;
    }
    
    public boolean hasMiningLaser() {
      return true;
    }
    public int miningLaser_getMaximumPowerLevel() { return 32768; }
    // number of durability points removed at 100% power
    public int miningLaser_getMaximumDamage() { return 10000; }
    
  }
  
  @Test
  public void testMining() throws AddressTrapException {
    World w = new World(2, 2);
    // put the robot at (0, 0, 1) and the block at (0, 1, 1)
    Vector robotPosition = new Vector(0, 0, 1);
   
    Robot robot = new TestRobot(new Microcontroller(4, 4));
    MiningLaserController controller = new MiningLaserController(robot);
    robot.getMCU().attachPeripheral(controller, CONTROLLER_BASE_ADDRESS);
    assertTrue(w.addOccupant(robotPosition, new Vector(0, 0, 0), robot));
    
    Vector blockPosition = new Vector(0, 1, 1);
    Block block = new Block(testMaterial);
    assertTrue(w.addOccupant(blockPosition, new Vector(0, 0, 0), block));
    
    Vector displacement = blockPosition.subtract(robotPosition);
    
    // write displacement coordinates to the controller
    controller.writeWord(CONTROLLER_BASE_ADDRESS + 0x0, displacement.getX());
    controller.writeWord(CONTROLLER_BASE_ADDRESS + 0x4, displacement.getY());
    controller.writeWord(CONTROLLER_BASE_ADDRESS + 0x8, displacement.getZ());
    // set power level
    controller.writeWord(CONTROLLER_BASE_ADDRESS + 0xC, 32768);
    
    w.timestep();
    
    // this should destroy the block instantly
    assertFalse(w.getOccupants(blockPosition).contains(block));
    // we should see one bogusite ore at the block position
    List<Ore> ores = w.getOccupants(blockPosition).stream()
        .filter((o -> o instanceof Ore)).map((o -> (Ore)o)).collect(Collectors.toList());
    assertFalse("no ore was produced", ores.isEmpty());
    assertEquals("wrong quantity of ore produced", 1, ores.size());
    Ore ore = ores.get(0);
    assertEquals("ore is made of wrong material", testMaterial, ore.getMaterial());
    
  }
  
}
