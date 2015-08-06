package io.lp0onfire.ssi.model;

public class RelativeAddObjectUpdate extends WorldUpdate {

  private final VoxelOccupant baseObject;
  private final Vector displacement;
  private final VoxelOccupant placedObject;
  
  public RelativeAddObjectUpdate(VoxelOccupant baseObject, Vector displacement, VoxelOccupant placedObject) {
    this.baseObject = baseObject;
    this.displacement = displacement;
    this.placedObject = placedObject;
  }
  
  @Override
  public WorldUpdateResult apply(World w) {
    Vector position = baseObject.getPosition().add(displacement);
    if (!w.canOccupy(position, placedObject)) {
      // failure: placed object cannot occupy that position
      return new WorldUpdateResult(false);
    }
    boolean result = w.addOccupant(position, new Vector(0, 0, 0), placedObject);
    if (result) {
      // success
      return new WorldUpdateResult(true);
    } else {
      // (unexpected) failure
      return new WorldUpdateResult(false);
    }
  }

}
