package io.lp0onfire.ssi.model.structures;

import io.lp0onfire.ssi.model.Material;
import io.lp0onfire.ssi.model.Structure;
import io.lp0onfire.ssi.model.Vector;

public abstract class Ramp extends Structure {

  private final Vector preferredDirection;
  public Vector getPreferredDirection() {
    return this.preferredDirection;
  }
  
  public Ramp(Material material, Vector preferredDirection) {
    super(material);
    this.preferredDirection = preferredDirection;
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
    return true;
  }

}
