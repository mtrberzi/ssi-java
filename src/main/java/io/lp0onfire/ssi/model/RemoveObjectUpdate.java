package io.lp0onfire.ssi.model;

public class RemoveObjectUpdate extends WorldUpdate {

  private final VoxelOccupant target;
  
  public RemoveObjectUpdate(VoxelOccupant target) {
    this.target = target;
  }
  
  @Override
  public WorldUpdateResult apply(World w) {
    w.removeOccupant(target);
    return new WorldUpdateResult(true);
  }
  
}
