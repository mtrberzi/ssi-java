package io.lp0onfire.ssi.model.items;

import io.lp0onfire.ssi.model.Item;
import io.lp0onfire.ssi.model.Material;

/**
 * A Bar is an item representing the refined output from a smelter.
 */
public class Bar extends Item {

  public Bar(Material material) {
    super(material);
  }

}
