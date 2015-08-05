package io.lp0onfire.ssi.model.structures;

import io.lp0onfire.ssi.model.Material;
import io.lp0onfire.ssi.model.Vector;

public class UpRamp extends Ramp {

  public UpRamp(Material material) {
    super(material, new Vector(0, 1, 0));
  }

}
