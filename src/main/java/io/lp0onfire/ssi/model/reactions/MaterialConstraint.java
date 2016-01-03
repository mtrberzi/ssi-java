package io.lp0onfire.ssi.model.reactions;

import io.lp0onfire.ssi.model.Item;
import io.lp0onfire.ssi.model.Material;
import io.lp0onfire.ssi.model.MaterialLibrary;

public class MaterialConstraint extends ReactantConstraint {

  private final String materialName;
  
  public MaterialConstraint(String materialName) {
    this.materialName = materialName;
  }

  @Override
  public boolean matches(Item i) {
    Material expected = MaterialLibrary.getInstance().getMaterial(materialName);
    Material actual = i.getMaterial();
    return actual.equals(expected);
  }
  
}
