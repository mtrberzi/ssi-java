package io.lp0onfire.ssi.model;

import java.util.UUID;

public class TakeWithManipulatorByUUIDUpdate extends WorldUpdate {

  private final Machine machine;
  private final Integer manipulatorIndex;
  private final UUID uuid;
  
  private Item takenItem = null;
  public Item getTakenItem() {
    return this.takenItem;
  }
  
  public TakeWithManipulatorByUUIDUpdate(Machine machine, Integer manipulatorIndex, UUID uuid) {
    this.machine = machine;
    this.manipulatorIndex = manipulatorIndex;
    this.uuid = uuid;
  }
  
  @Override
  public WorldUpdateResult apply(World w) {
    // TODO where the item can be taken from might depend on manipulator type...revisit this
    for (VoxelOccupant occ : w.getOccupants(machine.getPosition(), machine.getExtents())) {
      if (!(occ instanceof Item)) continue;
      Item i = (Item)occ;
      if (!(machine.canManipulate(manipulatorIndex, i))) continue;
      if (i.getUUID().equals(uuid)) {
        // match found
        takenItem = i;
        break;
      }
    }
    if (takenItem == null) {
      // failure
      return new WorldUpdateResult(false);
    } else {
      // success
      w.removeOccupant(takenItem);
      return new WorldUpdateResult(true);
    }
  }
  
}
