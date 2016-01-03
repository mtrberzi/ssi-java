package io.lp0onfire.ssi.model;

public abstract class MiningProduct {

  private double probability;
  public double getProbability() {
    return this.probability;
  }
  
  public MiningProduct(double prob) {
    this.probability = prob;
  }
  
  public abstract Item getProduct(Material baseMaterial);
  
}
