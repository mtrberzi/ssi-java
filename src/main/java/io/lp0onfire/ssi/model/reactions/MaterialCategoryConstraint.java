package io.lp0onfire.ssi.model.reactions;

import io.lp0onfire.ssi.model.Item;
import io.lp0onfire.ssi.model.Material;

public class MaterialCategoryConstraint extends ReactantConstraint {

  private final String category;
  
  public MaterialCategoryConstraint(String category) {
    this.category = category;
  }

  @Override
  public boolean matches(Item i) {
    Material m = i.getMaterial();
    return (m.getCategories().contains(category));
  }
  
}
