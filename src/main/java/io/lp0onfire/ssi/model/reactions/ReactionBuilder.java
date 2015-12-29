package io.lp0onfire.ssi.model.reactions;

import java.util.ArrayList;
import java.util.List;

public class ReactionBuilder {

  private Integer reactionID = null;
  public void setReactionID(Integer id) {
    this.reactionID = id;
  }
  
  private String reactionName = null;
  public void setReactionName(String name) {
    this.reactionName = name;
  }
  
  private int reactionTime = -1;
  public void setReactionTime(int time) {
    this.reactionTime = time;
  }
  
  private List<String> reactionCategories = new ArrayList<>();
  public void setCategories(List<String> reactionCategories) {
    this.reactionCategories = new ArrayList<>(reactionCategories);
  }
  
  private List<Reactant> reactants = new ArrayList<>();
  public void setReactants(List<Reactant> reactants) {
    this.reactants = new ArrayList<>(reactants);
  }
  
  private List<Product> products = new ArrayList<>();
  public void setProducts(List<Product> products) {
    this.products = new ArrayList<>(products);
  }
  
  private List<CreatedObject> createdObjects = new ArrayList<>();
  public void setCreatedObjects(List<CreatedObject> objs) {
    this.createdObjects = new ArrayList<>(objs);
  }
  
  public Reaction build() {
    if (reactionID == null) {
      throw new IllegalArgumentException("reaction ID not set");
    }
    if (reactionName == null) {
      throw new IllegalArgumentException("reaction name not set");
    }
    if (reactionTime < 0) {
      throw new IllegalArgumentException("reaction time must be a non-negative integer");
    }
    if (reactionCategories.isEmpty()) {
      throw new IllegalArgumentException("reaction not in any categories");
    }
    if (reactants.isEmpty()) {
      throw new IllegalArgumentException("reaction has no reactants");
    }
    if (products.isEmpty() && createdObjects.isEmpty()) {
      throw new IllegalArgumentException("reaction has no products or created objects");
    }
    return new Reaction(reactionID, reactionName, reactionTime, reactionCategories,
        reactants, products, createdObjects);
  }
  
}
