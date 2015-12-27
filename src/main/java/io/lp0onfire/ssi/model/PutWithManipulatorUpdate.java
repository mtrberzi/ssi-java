package io.lp0onfire.ssi.model;

public class PutWithManipulatorUpdate extends WorldUpdate {

  private final Machine machine;
  private final Integer manipulatorIndex;
  private final Item item;
  
  public PutWithManipulatorUpdate(Machine machine, Integer manipulatorIndex, Item item) {
    this.machine = machine;
    this.manipulatorIndex = manipulatorIndex;
    this.item = item;
  }
  
  @Override
  public WorldUpdateResult apply(World w) {
    // TODO where the item can be placed might depend on manipulator type and relative position...revisit this
    Vector targetPosition = machine.getPosition();
    boolean fail = false;
    if (!machine.canManipulate(manipulatorIndex, item)) {
      // cannot manipulate that item
      fail = true;
    } else if (!w.canOccupy(targetPosition, item)) {
      // cannot place the item in the target
      fail = true;
    }
    if (fail) {
      return new WorldUpdateResult(false);
    } else {
      // update the world, success
      w.addOccupant(targetPosition, new Vector(0,0,0), item);
      return new WorldUpdateResult(true);
    }
  }
  
}
