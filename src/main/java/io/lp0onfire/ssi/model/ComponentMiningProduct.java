package io.lp0onfire.ssi.model;

import io.lp0onfire.ssi.model.items.ComponentLibrary;

public class ComponentMiningProduct extends MiningProduct {

  private boolean usesBaseMaterial;
  private String material;
  
  private String componentName;
  
  public ComponentMiningProduct(String componentName, double prob) {
    super(prob);
    this.componentName = componentName;
    usesBaseMaterial = true;
  }
  
  public ComponentMiningProduct(String componentName, String material, double prob) {
    super(prob);
    this.usesBaseMaterial = false;
    this.material = material;
    this.componentName = componentName;
  }

  @Override
  public Item getProduct(Material baseMaterial) {
    Material mat;
    if (usesBaseMaterial) {
      mat = baseMaterial;
    } else {
      mat = MaterialLibrary.getInstance().getMaterial(material);
    }
    return ComponentLibrary.getInstance().createComponent(componentName, mat);
  }
  
}
