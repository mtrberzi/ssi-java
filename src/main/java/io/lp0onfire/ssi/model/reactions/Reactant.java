package io.lp0onfire.ssi.model.reactions;

import java.util.List;
import java.util.LinkedList;

public class Reactant {

  private final int quantity;
  public int getQuantity() {
    return this.quantity;
  }
  
  private final String componentName;
  public String getComponentName() {
    return this.componentName;
  }
  
  private final List<ReactantConstraint> constraints;
  public List<ReactantConstraint> getConstraints() {
    return this.constraints;
  }
  
  public Reactant(int quantity, String componentName, List<ReactantConstraint> constraints) {
    this.quantity = quantity;
    this.componentName = componentName;
    this.constraints = new LinkedList<ReactantConstraint>(constraints);
  }
  
}
