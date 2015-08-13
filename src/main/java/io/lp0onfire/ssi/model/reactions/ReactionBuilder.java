package io.lp0onfire.ssi.model.reactions;

import java.util.ArrayList;
import java.util.List;

public class ReactionBuilder {

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
  
  public Reaction build() {
    // TODO
    return null;
  }
  
}
