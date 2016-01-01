package io.lp0onfire.ssi.model;

import java.util.Set;

public class AddToStagingAreaUpdate extends WorldUpdate {

  private final VoxelOccupant baseObject;
  private final Item addedObject;
  
  public AddToStagingAreaUpdate(VoxelOccupant baseObject, Item addedObject) {
    this.baseObject = baseObject;
    this.addedObject = addedObject;
  }
  
  @Override
  public WorldUpdateResult apply(World w) {
    Vector position = baseObject.getPosition();
    // check if there is a staging area at the position
    Set<VoxelOccupant> occupants = w.getOccupants(position);
    StagingArea staging = null;
    for (VoxelOccupant occupant : occupants) {
      if (occupant instanceof StagingArea) {
        staging = (StagingArea)occupant;
        break;
      }
    }
    
    if (staging == null || !staging.canAcceptReactants()) {
      return new WorldUpdateResult(false);
    } else {
      staging.addReactant(addedObject);
      return new WorldUpdateResult(true);
    }
  }

}
