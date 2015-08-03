package io.lp0onfire.ssi.model.structures;

import io.lp0onfire.ssi.model.Material;
import io.lp0onfire.ssi.model.Structure;

public class Floor extends Structure {

  public Floor(Material material) {
    super(material);
  }

  @Override
  public boolean impedesMovement() {
    return false;
  }

  @Override
  public boolean impedesXYFluidFlow() {
    return false;
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

}
