package io.lp0onfire.ssi.model.reactions;

public class ProductBuilder {

  private int quantity = -1;
  public void setQuantity(int num) {
    this.quantity = num;
  }
  
  private String componentName = null;
  public void setComponentName(String name) {
    this.componentName = name;
  }
  
  private int copiedMaterial = -1;
  public void setCopiedMaterial(int materialIdx) {
    this.copiedMaterial = materialIdx;
  }
  
  public Product build() {
    if (quantity < 1) {
      throw new IllegalArgumentException("quantity must be positive");
    }
    if (componentName == null) {
      throw new IllegalArgumentException("product type must be specified");
    }
    if (copiedMaterial < 0) {
      throw new IllegalArgumentException("product material must be specified");
    }
    return new Product(quantity, componentName, copiedMaterial);
  }
  
}
