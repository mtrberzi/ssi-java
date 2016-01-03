package io.lp0onfire.ssi.model.structures;

import java.util.List;
import java.util.Random;

import io.lp0onfire.ssi.model.Item;
import io.lp0onfire.ssi.model.Material;
import io.lp0onfire.ssi.model.MiningProduct;
import io.lp0onfire.ssi.model.RelativeAddObjectUpdate;
import io.lp0onfire.ssi.model.Structure;
import io.lp0onfire.ssi.model.Vector;
import io.lp0onfire.ssi.model.WorldUpdate;

/**
 * A Block represents a solid slab of some material.
 * When the durability of a Block is reduced to 0, typically by mining,
 * useful raw materials may be dropped.
 */
public class Block extends Structure {

  public Block(Material material) {
    super(material);
  }

  @Override
  public boolean impedesXYMovement() {
    return true;
  }
  
  @Override
  public boolean impedesZMovement() {
    return true;
  }

  @Override
  public boolean impedesXYFluidFlow() {
    return true;
  }

  @Override
  public boolean impedesZFluidFlow() {
    return true;
  }
  
  @Override
  public boolean supportsOthers() {
    return true;
  }

  @Override
  public boolean needsSupport() {
    return true;
  }

  @Override
  public int getType() {
    return 1;
  }
  
  @Override
  public int getBaseDurability() { return 200; }
  
  @Override
  public List<WorldUpdate> onDestroy() {
    List<WorldUpdate> updates = super.onDestroy();
    
    // Figure out which materials may be dropped by this block.
    List<MiningProduct> products = getMaterial().getMiningProducts();
    Random rng = new Random();
    for (MiningProduct product : products) {
      if (rng.nextDouble() < product.getProbability()) {
        Item i = product.getProduct(getMaterial());
        updates.add(new RelativeAddObjectUpdate(this, new Vector(0, 0, 0), i));
      }
    }
    
    return updates;
  }
  
}
