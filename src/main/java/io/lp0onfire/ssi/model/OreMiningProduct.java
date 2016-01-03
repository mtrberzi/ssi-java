package io.lp0onfire.ssi.model;

import io.lp0onfire.ssi.model.items.Ore;

public class OreMiningProduct extends MiningProduct {

  private boolean usesBaseMaterial;
  private String material;
  
  public OreMiningProduct(double prob) {
    super(prob);
    usesBaseMaterial = true;
  }
  
  public OreMiningProduct(String material, double prob) {
    super(prob);
    this.usesBaseMaterial = false;
    this.material = material;
  }

  @Override
  public Item getProduct(Material baseMaterial) {
    if (usesBaseMaterial) {
      return new Ore(baseMaterial);
    } else {
      return new Ore(MaterialLibrary.getInstance().getMaterial(material));
    }
  }
  
}
