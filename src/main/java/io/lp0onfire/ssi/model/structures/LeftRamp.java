package io.lp0onfire.ssi.model.structures;

import io.lp0onfire.ssi.model.Material;
import io.lp0onfire.ssi.model.Vector;

public class LeftRamp extends Ramp {

  public LeftRamp(Material material) {
    super(material, new Vector(-1, 0, 0));
  }

}
