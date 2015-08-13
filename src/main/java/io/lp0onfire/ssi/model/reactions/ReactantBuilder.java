package io.lp0onfire.ssi.model.reactions;

import java.util.LinkedList;
import java.util.List;

public class ReactantBuilder {

  private int quantity = -1;
  public void setQuantity(int quantity) {
    this.quantity = quantity;
  }
  
  private String componentName = null;
  public void setComponentName(String componentName) {
    this.componentName = componentName;
  }
  
  private List<ReactantConstraint> constraints = new LinkedList<>();
  public void setConstraints(List<ReactantConstraint> constraints) {
    this.constraints = new LinkedList<>(constraints);
  }
  
  public Reactant build() {
    if (quantity < 1) {
      throw new IllegalArgumentException("reactant quantity must be positive");
    }
    if (componentName == null) {
      throw new IllegalArgumentException("reactant type must be specified");
    }
    return new Reactant(quantity, componentName, constraints);
  }
  
}
