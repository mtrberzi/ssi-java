package io.lp0onfire.ssi.model;

import java.util.LinkedList;
import java.util.List;

public class Material {
  private final String name;
  public String getName() {
    return this.name;
  }
  
  private final int type;
  public int getType() {
    return this.type;
  }
  
  private final double durabilityModifier;
  public double getDurabilityModifier() {
    return this.durabilityModifier;
  }
  
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
  
  public Material(String name, int type, double durabilityModifier,
      boolean canBeSmelted, int smeltingTimesteps, int numberOfSmeltedBars,
      List<String> categories) {
    this.name = name;
    this.type = type;
    this.durabilityModifier = durabilityModifier;
    this.canBeSmelted = canBeSmelted;
    this.smeltingTimesteps = smeltingTimesteps;
    this.numberOfSmeltedBars = numberOfSmeltedBars;
    this.categories = new LinkedList<String>(categories);
  }
  
}
