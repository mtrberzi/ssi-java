package io.lp0onfire.ssi.model.items;

import io.lp0onfire.ssi.model.Item;
import io.lp0onfire.ssi.model.Material;

/**
 * Ore is a kind of item that is produced by mining.
 * It must be smelted into bars to be made useful.
 */
public class Ore extends Item {

  public Ore(Material material) {
    super(material);
  }
  
  public short getKind() {
    return (short)1;
  }
  
  public int getType() {
    return getMaterial().getType();
  }

}
