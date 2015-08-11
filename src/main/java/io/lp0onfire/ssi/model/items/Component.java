package io.lp0onfire.ssi.model.items;

import io.lp0onfire.ssi.model.Item;
import io.lp0onfire.ssi.model.Material;

public class Component extends Item {

  private final String componentName;
  public String getComponentName() {
    return this.componentName;
  }
  
  public Component(Material material, String componentName) {
    super(material);
    this.componentName = componentName;
  }
  
}
