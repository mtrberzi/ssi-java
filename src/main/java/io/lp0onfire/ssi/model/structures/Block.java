package io.lp0onfire.ssi.model.structures;

import io.lp0onfire.ssi.model.Material;
import io.lp0onfire.ssi.model.Structure;

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
  
}
