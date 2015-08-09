package io.lp0onfire.ssi.model;

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
  
  public Material(boolean canBeSmelted, int smeltingTimesteps, int numberOfSmeltedBars) {
    this.canBeSmelted = canBeSmelted;
    this.smeltingTimesteps = smeltingTimesteps;
    this.numberOfSmeltedBars = numberOfSmeltedBars;
  }
  
}
