package io.lp0onfire.ssi.model.reactions;

import io.lp0onfire.ssi.model.Item;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Reaction {

  private final Integer reactionID;
  public Integer getID() {
    return this.reactionID;
  }
  
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
  
  public Reaction(Integer reactionID, String reactionName, int reactionTime,
      List<String> reactionCategories, List<Reactant> reactants,
      List<Product> products) {
    this.reactionID = reactionID;
    this.reactionName = reactionName;
    this.reactionTime = reactionTime;
    this.categories = reactionCategories;
    this.reactants = reactants;
    this.products = products;
  }
  
  // cached items from reactantsOK() check
  List<Item> items_cached = null;
  // cached bins from reactantsOK(); index i is the list of items matched to reactant i
  List<List<Item>> matchedItems_cached = null;
  
  public boolean reactantsOK(List<Item> inputItems) {
    return (match(inputItems) != null);
  }
  
  private List<List<Item>> match(List<Item> inputItems) {
    if (items_cached != null) {
      if (items_cached.equals(inputItems)) {
        return matchedItems_cached;
      }
    }
    
    // cache miss
    List<List<Item>> matchedItems = new ArrayList<>(reactants.size());
    List<Item> itemWorklist = new ArrayList<>(inputItems);
    boolean result = true;
    for (int i = 0; i < reactants.size(); ++i) {
      Reactant reactant = reactants.get(i);
      List<Item> matchedReactants = reactant.match(itemWorklist);
      if (matchedReactants.isEmpty()) {
        result = false;
        break;
      } else {
        matchedItems.add(matchedReactants);
        itemWorklist.removeAll(matchedReactants);
      }
    }
    
    items_cached = new ArrayList<>(inputItems);
    if (result) {
      matchedItems_cached = matchedItems;
      return matchedItems;
    } else {
      matchedItems_cached = null;
      return null;
    }
  }
  
  public class Result {
    private final boolean success;
    public boolean successful() {
      return this.success;
    }
    
    private List<Item> consumedReactants;
    public List<Item> getConsumedReactants() {
      return this.consumedReactants;
    }
    
    private List<Item> outputProducts;
    public List<Item> getOutputProducts() {
      return this.outputProducts;
    }
    
    public Result() {
      this.success = false;
    }
    
    public Result(List<Item> consumedReactants, List<Item> outputProducts) {
      this.success = true;
      this.consumedReactants = consumedReactants;
      this.outputProducts = outputProducts;
    }
  }
  
  // returns: a result object, whose components are the items consumed in the reaction
  // and the output products from the reaction
  public Result react(List<Item> inputItems) {
    List<List<Item>> reactantItems = match(inputItems);
    if (reactantItems == null) {
      // fail
      return new Result();
    }
    
    List<Item> outputProducts = new LinkedList<>();
    for (Product p : products) {
      outputProducts.addAll(p.produce(reactantItems));
    }
    
    // build the list of reagents
    List<Item> consumedReactants = new LinkedList<>();
    for (List<Item> rx : reactantItems) {
      consumedReactants.addAll(rx);
    }
    
    // success
    return new Result(consumedReactants, outputProducts);
  }
  
}
