package io.lp0onfire.ssi.model.structures;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.BeforeClass;
import org.junit.Test;

import io.lp0onfire.ssi.model.Material;
import io.lp0onfire.ssi.model.MaterialBuilder;
import io.lp0onfire.ssi.model.MiningProduct;
import io.lp0onfire.ssi.model.OreMiningProduct;
import io.lp0onfire.ssi.model.ReduceDurabilityUpdate;
import io.lp0onfire.ssi.model.TransportTube;
import io.lp0onfire.ssi.model.Vector;
import io.lp0onfire.ssi.model.World;
import io.lp0onfire.ssi.model.WorldUpdate;
import io.lp0onfire.ssi.model.items.Ore;

public class TestBlock {

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
  
  @Test
  public void testMineBlock_OreMiningProduct() {
    World w = new World(2, 2);
    Block block = new Block(testMaterial);
    Vector blockPosition = new Vector(0, 0, 1);
    assertTrue(w.addOccupant(blockPosition, new Vector(0,0,0), block));
    
    int durability = block.getCurrentDurability();
    WorldUpdate mineBlockUpdate = new ReduceDurabilityUpdate(block, durability);
    mineBlockUpdate.apply(w);
    
    // the block should no longer be at that position
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
