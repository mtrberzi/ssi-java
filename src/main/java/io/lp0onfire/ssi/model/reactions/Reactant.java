package io.lp0onfire.ssi.model.reactions;

import io.lp0onfire.ssi.model.Item;
import io.lp0onfire.ssi.model.items.Component;

import java.util.ArrayList;
import java.util.Iterator;
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
  
  /**
   * Matches a list of input items against the requirements for this reactant.
   * @param inputs the items that are to be matched
   * @return a list of which items were matched, which is a subset of the input list,
   * or an empty list if the match was unsuccessful
   */
  public List<Item> match(List<Item> inputs) {
    if (componentName != null) {
      List<Item> candidates = new LinkedList<>();
      for (Item i : inputs) {
        if (i instanceof Component) {
          Component comp = (Component)i;
          candidates.add(comp);
        }
      }
      // if at this point we don't have the minimum number of items,
      // the match is guaranteed to be unsuccessful
      if (candidates.size() < quantity) {
        return new ArrayList<Item>();
      }
      Iterator<Item> i = candidates.iterator();
      while (i.hasNext()) {
        Item item = i.next();
        for (ReactantConstraint cxt : constraints) {
          if (!cxt.matches(item)) {
            i.remove();
            continue;
          }
        }
      }
      // if we no longer have enough items, fail 
      if (candidates.size() < quantity) {
        return new ArrayList<Item>();
      } else {
        // remove items until we have exactly 'quantity' of them, then return success
        while(candidates.size() > quantity) {
          candidates.remove(0);
        }
        return candidates;
      }
    } else {
      throw new UnsupportedOperationException("non-component reactants not yet implemented");
    }
  }
  
}
