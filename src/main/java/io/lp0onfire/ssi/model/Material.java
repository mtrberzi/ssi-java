package io.lp0onfire.ssi.model;

import java.util.LinkedList;
import java.util.List;

public class Material {
  private final boolean canBeSmelted;
  public boolean getCanBeSmelted() {
    return this.canBeSmelted;
  }
  
  private final int smeltingTimesteps;
  public int getSmeltingTimesteps() {
    return this.smeltingTimesteps;
  }
  
  private final int numberOfSmeltedBars;
  public int getNumberOfSmeltedBars() {
    return this.numberOfSmeltedBars;
  }
  
  private final List<String> categories;
  public List<String> getCategories() {
    return this.categories;
  }
  
  public Material(boolean canBeSmelted, int smeltingTimesteps, int numberOfSmeltedBars,
      List<String> categories) {
    this.canBeSmelted = canBeSmelted;
    this.smeltingTimesteps = smeltingTimesteps;
    this.numberOfSmeltedBars = numberOfSmeltedBars;
    this.categories = new LinkedList<String>(categories);
  }
  
}
