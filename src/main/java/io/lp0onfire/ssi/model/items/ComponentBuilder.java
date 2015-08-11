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
  
  public void validate() throws IllegalArgumentException {
    if (componentName == null) {
      throw new IllegalArgumentException("component name not set");
    }
  }
  
  public Component build(Material material) {
    return new Component(material, componentName);
  }
  
}
