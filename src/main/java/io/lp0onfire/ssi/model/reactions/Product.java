package io.lp0onfire.ssi.model.reactions;

import io.lp0onfire.ssi.model.Item;
import io.lp0onfire.ssi.model.Material;
import io.lp0onfire.ssi.model.items.ComponentLibrary;

import java.util.LinkedList;
import java.util.List;

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
  
  public List<Item> produce(List<List<Item>> reactants) {
    List<Item> products = new LinkedList<>();
    
    Material m;
    
    if (copiedMaterial != -1) {
      m = reactants.get(copiedMaterial).get(0).getMaterial();
    } else {
      throw new UnsupportedOperationException("unhandled material case in Product");
    }
    
    for (int i = 0; i < quantity; ++i) {
      if (componentName != null) {
        products.add(ComponentLibrary.getInstance().createComponent(componentName, m));
      } else {
        throw new UnsupportedOperationException("unhandled item case in Product");
      }
    }
    
    return products;
  }
  
}
