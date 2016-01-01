package io.lp0onfire.ssi.model.items;

import io.lp0onfire.ssi.model.Material;

public class ComponentBuilder {

  private String componentName = null;
  public void setComponentName(String name) {
    this.componentName = name;
  }
  public String getComponentName() {
    return this.componentName;
  }
  
  public int type = -1;
  public void setType(int type){
    this.type = type;
  }
  public int getType() {
    return this.type;
  }
  
  public void validate() throws IllegalArgumentException {
    if (componentName == null) {
      throw new IllegalArgumentException("component name not set");
    }
    if (type == -1) {
      throw new IllegalArgumentException("component type not set");
    }
  }
  
  public Component build(Material material) {
    return new Component(material, componentName, type);
  }
  
}
