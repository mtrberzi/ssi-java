package io.lp0onfire.ssi.model.reactions;

public class Product {

  private final int quantity;
  public int getQuantity() {
    return this.quantity;
  }
  
  private final String componentName;
  public String getComponentName() {
    return this.componentName;
  }
  
  private final int copiedMaterial;
  public int getCopiedMaterial() {
    return this.copiedMaterial;
  }
  
  public Product(int quantity, String componentName, int copiedMaterial) {
    this.quantity = quantity;
    this.componentName = componentName;
    this.copiedMaterial = copiedMaterial;
  }
  
}
