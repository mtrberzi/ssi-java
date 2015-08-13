package io.lp0onfire.ssi.model.reactions;

import java.util.List;

public class Reaction {

  private final String reactionName;
  public String getName() {
    return this.reactionName;
  }
  
  private final int reactionTime;
  public int getTime() {
    return this.reactionTime;
  }
  
  private final List<String> categories;
  public List<String> getCategories() {
    return this.categories;
  }
  
  private final List<Reactant> reactants;
  public List<Reactant> getReactants() {
    return this.reactants;
  }
  
  private final List<Product> products;
  public List<Product> getProducts() {
    return this.products;
  }
  
  public Reaction(String reactionName, int reactionTime,
      List<String> reactionCategories, List<Reactant> reactants,
      List<Product> products) {
    this.reactionName = reactionName;
    this.reactionTime = reactionTime;
    this.categories = reactionCategories;
    this.reactants = reactants;
    this.products = products;
  }
  
  
  
}
