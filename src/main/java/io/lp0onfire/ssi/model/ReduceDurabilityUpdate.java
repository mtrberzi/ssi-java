package io.lp0onfire.ssi.model;

import java.util.List;

public class ReduceDurabilityUpdate extends WorldUpdate {

  private final VoxelOccupant target;
  private int durabilityChange;
  
  public ReduceDurabilityUpdate(VoxelOccupant target, int durabilityChange) {
    if (durabilityChange < 0) {
      throw new IllegalArgumentException("durability change must be non-negative");
    }
    this.target = target;
    this.durabilityChange = durabilityChange;
  }
  
  @Override
  public WorldUpdateResult apply(World w) {
    int durability = target.getCurrentDurability();
    // if the target is already effectively destroyed, don't do anything
    if (durability <= 0) {
      return new WorldUpdateResult(true);
    } else {
      durability -= durabilityChange;
      target.setCurrentDurability(durability);
      // if durability dropped below 0 as a result of this reduction,
      // the target is "destroyed" -- immediately apply all onDestroy() updates.
      // It is safe to do this because we'll only ever enter this method during
      // a serialized world update, so we can chain world updates without
      // worrying about concurrency problems.
      if (durability <= 0) {
        List<WorldUpdate> updates = target.onDestroy();
        for (WorldUpdate update : updates) {
          update.apply(w);
        }
      }
      return new WorldUpdateResult(true);
    }
  }

}
