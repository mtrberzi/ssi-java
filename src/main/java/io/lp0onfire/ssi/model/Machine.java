package io.lp0onfire.ssi.model;

import io.lp0onfire.ssi.microcontroller.peripherals.InventoryController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A Machine is a voxel occupant that represents a mechanical device or robot.
 * Machines cannot be directly manipulated by robots, but can be manipulated indirectly.
 * Machines may occupy one or more voxels.
 * Machines can interface with inventory controllers in order to manipulate items
 * in their environment.
 */
public abstract class Machine extends VoxelOccupant {

  public Machine() {
    initializeManipulatorInterface();
  }
  
  private Vector position;
  public Vector getPosition() {
    return this.position;
  }
  public void setPosition(Vector p) {
    this.position = p;
  }
  
  @Override
  public boolean requiresPreprocessing() {
    return true;
  }
  
  @Override
  public boolean requiresTimestep() {
    return true;
  }
  
  public short getKind() {
    return (short)3;
  }
  
  // inventory controller interface
  private void initializeManipulatorInterface() {
    this.lastManipulatorError = new InventoryController.ErrorCode[getNumberOfManipulators()];
    for (int i = 0; i < getNumberOfManipulators(); ++i) {
      this.lastManipulatorError[i] = InventoryController.ErrorCode.NO_ERROR;
    }
  }
  
  public abstract int getNumberOfManipulators();
  protected void checkManipulatorIndex(int mIdx) {
    if (mIdx < 0 || mIdx >= getNumberOfManipulators()) {
      throw new IllegalArgumentException("manipulator index " + mIdx + " not valid");
    }
  }
  
  public enum ManipulatorType {
    TRANSPORT_TUBE_ENDPOINT,
    LIGHT_ARM,
  }
  public abstract ManipulatorType getManipulatorType(int mIdx);
  
  private Map<Integer, WorldUpdate> manipulatorCommands = new HashMap<>();
  public boolean canAcceptManipulatorCommand(int mIdx) {
    checkManipulatorIndex(mIdx);
    return manipulatorCommands.containsKey(mIdx);
  }
  
  private InventoryController.ErrorCode[] lastManipulatorError;
  
  // get the error code from the previous command
  public InventoryController.ErrorCode getManipulatorError(int mIdx) {
    checkManipulatorIndex(mIdx);
    return lastManipulatorError[mIdx];
  }
  
  // make sure this always gets called correctly by anything that overrides this method
  @Override
  public boolean hasWorldUpdates() {
    return !manipulatorCommands.isEmpty();
  }
  
  public List<WorldUpdate> getWorldUpdates() {
    List<WorldUpdate> updates = new LinkedList<>();
    updates.addAll(manipulatorCommands.values());
    updates.addAll(super.getWorldUpdates());
    return updates;
  }
  
  // make sure this always gets called by anything that overrides this method
  @Override
  public void collectUpdateResults(Map<WorldUpdate, WorldUpdateResult> results) {
    super.collectUpdateResults(results);
    for (Map.Entry<WorldUpdate, WorldUpdateResult> entry : results.entrySet()) {
      WorldUpdate update = entry.getKey();
      WorldUpdateResult result = entry.getValue();
      // find the manipulator that issued this command
      // TODO this is expensive
      for (int i = 0; i < getNumberOfManipulators(); ++i) {
        if (manipulatorCommands.containsKey(i) && manipulatorCommands.get(i).equals(update)) {
          manipulatorCommands.remove(i);
          if (result.wasSuccessful()) {
            // clear error code
            lastManipulatorError[i] = InventoryController.ErrorCode.NO_ERROR;
            // figure out what type of update just happened, and resolve it accordingly
            // TODO
          } else {
            // TODO resolve error code
          }
          break;
        }
      }
    }
  }
  
  // returns: true iff the specified manipulator can manipulate the given item
  public boolean canManipulate(int mIdx, Item item) {
    checkManipulatorIndex(mIdx);
    ManipulatorType mType = getManipulatorType(mIdx);
    switch (mType) {
    case LIGHT_ARM:
      // TODO
      return true;
    case TRANSPORT_TUBE_ENDPOINT:
      return true;
    default:
      throw new UnsupportedOperationException("unknown manipulator type " + mType);
    }
  }
  
  private Map<Integer, Item> manipulatorItem = new HashMap<>();
  // get the item that was picked up by the manipulator
  // as a result of executing the last command, if it exists
  // and has not already been taken
  public Item takeManipulatorItem(int mIdx) {
    checkManipulatorIndex(mIdx);
    if (manipulatorItem.containsKey(mIdx)) {
      Item i = manipulatorItem.get(mIdx);
      manipulatorItem.remove(mIdx);
      return i;
    } else {
      return null;
    }
  }
  
  // all manipulator queue commands return true if the command is legal on that
  // manipulator, and false if the command is not legal
  
  // queue command: take the available item matching the given UUID
  public boolean manipulator_getItemByUUID(int mIdx, UUID uuid) {
    if (mIdx < 0 || mIdx >= getNumberOfManipulators()) {
      return false;
    }
    // transport tube endpoints can't accept this command
    if (getManipulatorType(mIdx) == ManipulatorType.TRANSPORT_TUBE_ENDPOINT) {
      return false;
    }
    if (!canAcceptManipulatorCommand(mIdx)) return true;
    manipulatorCommands.put(mIdx, new TakeWithManipulatorByUUIDUpdate(this, mIdx, uuid));
    return true;
  }
  
  // queue command: take the first available item that can be manipulated
  public boolean manipulator_getAnyItem(int mIdx) {
    throw new UnsupportedOperationException("not yet implemented");
  }
  
  // queue command: attempt to output the provided item, if it can be manipulated and output
  public boolean manipulator_putItem(int mIdx, Item item) {
    throw new UnsupportedOperationException("not yet implemented");
  }
  
}
