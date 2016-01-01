package io.lp0onfire.ssi.model;

public class RelativeRemoveObjectUpdate extends WorldUpdate {
  
  private final VoxelOccupant baseObject;
  private final Vector displacement;
  private final VoxelOccupant removedObject;
  
  public VoxelOccupant getRemovedObject() {
    return this.removedObject;
  }
  
  public RelativeRemoveObjectUpdate(VoxelOccupant baseObject, Vector displacement, VoxelOccupant removedObject) {
    this.baseObject = baseObject;
    this.displacement = displacement;
    this.removedObject = removedObject;
  }
  
  @Override
  public WorldUpdateResult apply(World w) {
    Vector position = baseObject.getPosition().add(displacement);
    if (!w.getOccupants(position).contains(removedObject)) {
      // failure: removed object isn't there
      return new WorldUpdateResult(false);
    }
    w.removeOccupant(removedObject);
    return new WorldUpdateResult(true);
  }
}
