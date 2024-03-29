package io.lp0onfire.ssi.model.structures;

import io.lp0onfire.ssi.model.Material;
import io.lp0onfire.ssi.model.Vector;

public class RightRamp extends Ramp {

  public RightRamp(Material material) {
    super(material, new Vector(1, 0, 0));
  }
  
  @Override
  public int getType() {
    return 4;
  }

}
