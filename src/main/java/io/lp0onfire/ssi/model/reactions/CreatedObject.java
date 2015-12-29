package io.lp0onfire.ssi.model.reactions;

import java.util.List;

import io.lp0onfire.ssi.model.Item;
import io.lp0onfire.ssi.model.VoxelOccupant;

public abstract class CreatedObject {

  public abstract VoxelOccupant createObject(List<List<Item>> reactants);
  
}
