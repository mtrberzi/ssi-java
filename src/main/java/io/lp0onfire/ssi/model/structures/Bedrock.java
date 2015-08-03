package io.lp0onfire.ssi.model.structures;

import io.lp0onfire.ssi.model.MaterialLibrary;
import io.lp0onfire.ssi.model.Structure;

public class Bedrock extends Structure {

  public Bedrock() {
    super(MaterialLibrary.getInstance().getMaterial("bedrock"));
  }

  @Override
  public boolean impedesMovement() {
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
    return false;
  }

}
