package io.lp0onfire.ssi.model.items;

import io.lp0onfire.ssi.model.Item;
import io.lp0onfire.ssi.model.Material;

public class Component extends Item {

  private final String componentName;
  public String getComponentName() {
    return this.componentName;
  }
  
  private final int type;
  public int getType() {
    return this.type;
  }
  
  public Component(Material material, String componentName, int type) {
    super(material);
    this.componentName = componentName;
    this.type = type;
  }
  
  public short getKind() {
    return (short)2;
  }
  
}
