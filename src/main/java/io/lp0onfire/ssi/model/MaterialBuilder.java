package io.lp0onfire.ssi.model;

import java.util.LinkedList;
import java.util.List;

public class MaterialBuilder {
  
  private String name = null;
  public void setMaterialName(String name) {
    this.name = name;
  }
  
  private boolean canBeSmelted = false;
  public void setCanBeSmelted(boolean b) {
    this.canBeSmelted = b;
  }
  
  private int smeltingTimesteps = -1;
  public void setSmeltingTimesteps(int i) {
    this.smeltingTimesteps = i;
  }
  
  private int numberOfSmeltedBars = -1;
  public void setNumberOfSmeltedBars(int i) {
    this.numberOfSmeltedBars = i;
  }
  
  private List<String> categories = new LinkedList<>();
  public void setCategories(List<String> categories) {
    this.categories = new LinkedList<String>(categories);
  }
  
  public Material build() {
    if (name == null) {
      throw new IllegalArgumentException("must specify material name");
    }
    if (canBeSmelted) {
      if (smeltingTimesteps < 0) {
        throw new IllegalArgumentException("must specify number of smelting timesteps");
      }
      if (numberOfSmeltedBars < 0) {
        throw new IllegalArgumentException("must specify number of smelted bars");
      }
    }
    return new Material(name,
        canBeSmelted, smeltingTimesteps, numberOfSmeltedBars, categories);
  }
  
}
