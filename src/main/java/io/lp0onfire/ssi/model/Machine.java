package io.lp0onfire.ssi.model;

import io.lp0onfire.ssi.TimeConstants;
import io.lp0onfire.ssi.microcontroller.Microcontroller;
import io.lp0onfire.ssi.microcontroller.peripherals.InventoryController;
import io.lp0onfire.ssi.model.reactions.Reaction;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    this.mcu = null;
    this.invController = null;
  }
  
  public Machine(Microcontroller mcu) {
    initializeManipulatorInterface();
    this.mcu = mcu;
    this.invController = null;
  }
  
  private Microcontroller mcu;
  protected Microcontroller getMCU() {
    return this.mcu;
  }
  
  private InventoryController invController;
  
  protected void makeInventoryController(int nObjectBuffers) {
    this.invController = new InventoryController(this, nObjectBuffers);
    mcu.attachInventoryController(invController);
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
  public void preprocess() {
    // run the microcontroller, if there is one
    if (mcu == null) return;
    for (int i = 0; i < TimeConstants.CLOCK_CYCLES_PER_TIMESTEP; ++i) {
      mcu.cycle();
    }
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
      // set up a private buffer
      manipulatorPrivateBuffer.put(i, new LinkedList<Item>());
      manipulatorReactionStartSinceLastMSTAT.put(i, false);
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
    PART_BUILDER, // for Robot Part Builder
    ROBOT_ASSEMBLER,
    FIELD_ASSEMBLY_DEVICE,
  }
  public abstract ManipulatorType getManipulatorType(int mIdx);
  
  private Map<Integer, WorldUpdate> manipulatorCommands = new HashMap<>();
  public boolean canAcceptManipulatorCommand(int mIdx) {
    checkManipulatorIndex(mIdx);
    return !manipulatorCommands.containsKey(mIdx);
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
    return !manipulatorCommands.isEmpty() || !reactionObjectUpdates.isEmpty();
  }
  
  public List<WorldUpdate> getWorldUpdates() {
    List<WorldUpdate> updates = new LinkedList<>();
    updates.addAll(manipulatorCommands.values());
    updates.addAll(reactionObjectUpdates);
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
      if (reactionObjectUpdates.contains(update)) {
        reactionObjectUpdates.remove(update);
        if (!result.wasSuccessful()) {
          // oops, we couldn't place the object, or something.
          // this is very bad and should never happen, so we
          // consider this an internal error
          throw new IllegalStateException("internal error: failed to place object created as a result of a reaction");
        }
      } else {
        // find the manipulator that issued this command
        // TODO this is expensive
        for (int i = 0; i < getNumberOfManipulators(); ++i) {
          if (manipulatorCommands.containsKey(i) && manipulatorCommands.get(i).equals(update)) {
            manipulatorCommands.remove(i);
            if (result.wasSuccessful()) {
              // clear error code
              lastManipulatorError[i] = InventoryController.ErrorCode.NO_ERROR;
              // figure out what type of update just happened, and resolve it accordingly
              if (update instanceof TakeWithManipulatorByUUIDUpdate) {
                TakeWithManipulatorByUUIDUpdate typedUpdate = (TakeWithManipulatorByUUIDUpdate)update;
                Item item = typedUpdate.getTakenItem();
                manipulatorItem.put(i, item);
              } else if (update instanceof PutWithManipulatorUpdate) {
                // no action
              } else if (update instanceof RelativeAddObjectUpdate && manipulator_isBuildingConstructor(i)) {
                // construction successful, no action
              } else {
                throw new UnsupportedOperationException("not yet implemented");
              }
            } else {
              if (update instanceof RelativeAddObjectUpdate && manipulator_isBuildingConstructor(i)) {
                // don't do anything in this case, a race condition was averted
              } else {
                // TODO resolve error code
                throw new UnsupportedOperationException("not yet implemented");
              }
            }
            break;
          }
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
    case PART_BUILDER:
      return true;
    case ROBOT_ASSEMBLER:
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
  
  // non-queue manipulator commands
  private Map<Integer, Reaction> manipulatorReaction = new HashMap<>();
  private Map<Integer, Boolean> manipulatorIsPerformingReaction = new HashMap<>();
  private Map<Integer, Integer> manipulatorReactionTimeRemaining = new HashMap<>();
  private Map<Integer, LinkedList<Item>> manipulatorPrivateBuffer = new HashMap<>();
  private Map<Integer, Boolean> manipulatorReactionStartSinceLastMSTAT = new HashMap<>();
  private Set<WorldUpdate> reactionObjectUpdates = new HashSet<>();
  
  public boolean manipulator_isReacting(int mIdx) {
    Boolean b = manipulatorIsPerformingReaction.get(mIdx);
    return (b != null && b.booleanValue());
  }
  
  public boolean manipulator_canSetReaction(int mIdx) {
    if (mIdx < 0 || mIdx >= getNumberOfManipulators()) {
      return false;
    }
    ManipulatorType mType = getManipulatorType(mIdx);
    switch (mType) {
    case PART_BUILDER:
    case ROBOT_ASSEMBLER:
    case FIELD_ASSEMBLY_DEVICE:
      return true;
    default: return false;
    }
  }
  
  protected boolean manipulator_isBuildingConstructor(int mIdx) {
    if (mIdx < 0 || mIdx >= getNumberOfManipulators()) {
      return false;
    }
    ManipulatorType mType = getManipulatorType(mIdx);
    switch (mType) {
    case FIELD_ASSEMBLY_DEVICE:
      return true;
    default: return false;
    }
  }
  
  protected boolean isReactionLegal(int mIdx, Reaction reaction) {
    ManipulatorType mType = getManipulatorType(mIdx);
    switch (mType) {
    case PART_BUILDER:
      return reaction.getCategories().contains("part-builder-1");
    case ROBOT_ASSEMBLER:
      return reaction.getCategories().contains("robot-assembly-1");
    case FIELD_ASSEMBLY_DEVICE:
      return reaction.getCategories().contains("construction-1");
    default: return false;
    }
  }
  
  protected void tryToStartReaction(int mIdx) {
    Reaction reaction = manipulatorReaction.get(mIdx);
    if (reaction == null) return;
    
    if (reaction.reactantsOK(manipulatorPrivateBuffer.get(mIdx))) {
      manipulatorIsPerformingReaction.put(mIdx, true);
      manipulatorReactionTimeRemaining.put(mIdx, reaction.getTime());
      manipulatorReactionStartSinceLastMSTAT.put(mIdx, true);
    }
  }
  
  @Override
  public void timestep() {
    super.timestep();
    if (mcu != null) {
      mcu.timestep();
    }
    // check reaction progress for all reactors
    for (Map.Entry<Integer, Boolean> entry : manipulatorIsPerformingReaction.entrySet()) {
      Integer mIdx = entry.getKey();
      Boolean isReacting = entry.getValue();
      if (isReacting) {
        int newRxTime = manipulatorReactionTimeRemaining.get(mIdx) - 1;
        if (newRxTime <= 0) {
          Reaction currentReaction = manipulatorReaction.get(mIdx);
          List<Item> currentItems = manipulatorPrivateBuffer.get(mIdx);
          Reaction.Result result = currentReaction.react(currentItems);
          if (!result.successful()) {
            // TODO better error handling
            throw new IllegalStateException("reaction unexpectedly failed");
          }
          List<Item> consumedReactants = result.getConsumedReactants();
          currentItems.removeAll(consumedReactants);
          List<Item> products = result.getOutputProducts();
          for (Item i : products) {
            currentItems.add(i);
          }
          // if the reaction creates any objects, try to place them at (0, 0, 0)
          List<VoxelOccupant> newObjects = result.getCreatedObjects();
          for (VoxelOccupant obj : newObjects) {
            WorldUpdate update = new RelativeAddObjectUpdate(this, new Vector(0, 0, 0), obj);
            reactionObjectUpdates.add(update);
          }
          
          manipulatorIsPerformingReaction.put(mIdx, false);
          manipulatorReactionTimeRemaining.remove(mIdx);
        } else {
          manipulatorReactionTimeRemaining.put(mIdx, newRxTime);
        }
      }
    }
  }
  
  public boolean manipulator_setReaction(int mIdx, Reaction reaction) {
    if (!manipulator_canSetReaction(mIdx)) {
      return false;
    }
    // check whether this reaction type is legal on this manipulator
    if (!isReactionLegal(mIdx, reaction)) {
      return false;
    }
    
    if (manipulator_isBuildingConstructor(mIdx)) {
      // special case: queue a world update that sets up a staging area
      // TODO get real extents
      // TODO don't allow this more than once per timestep
      Vector extents = new Vector(1, 1, 1);
      StagingArea staging = new StagingArea(extents, reaction);
      manipulatorCommands.put(mIdx, new RelativeAddObjectUpdate(this, new Vector(0, 0, 0), staging));
      return true;
    } else {
      // can't change the reaction during a reaction
      if (manipulator_isReacting(mIdx)) {
        return false;
      }
      manipulatorReaction.put(mIdx, reaction);
      tryToStartReaction(mIdx);
      return true;
    }
  }
  
  // all manipulator queue commands return true if the command is legal on that
  // manipulator, and false if the command is not legal
  
  // TODO double-check error handling for all of these, especially the non-world-update ones
  
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
  
  // queue command: take the next available item
  public boolean manipulator_getNextItem(int mIdx) {
    // special check for reactor-type manipulators
    // TODO fast command completion in this case
    if (manipulator_canSetReaction(mIdx)) { // this is such a cheeky way of doing the test...
      if (manipulator_isBuildingConstructor(mIdx)) {
        // TODO some way of taking stuff out of a staging area would be nice
        return false;
      }
      
      if (!manipulator_isReacting(mIdx)) {
        manipulatorItem.put(mIdx, manipulatorPrivateBuffer.get(mIdx).removeFirst());
        return true;
      } else {
        return false; // can't take items out while a reaction is being performed
      }
    } else {
      return false;
    }
  }
  
  // queue command: attempt to output the provided item, if it can be manipulated and output
  public boolean manipulator_putItem(int mIdx, Item item) {
    if (mIdx < 0 || mIdx >= getNumberOfManipulators()) {
      return false;
    }
    
    // special check for reactor-type manipulators
    // TODO fast command completion in this case
    if (manipulator_canSetReaction(mIdx)) { // this is such a cheeky way of doing the test...
      if (manipulator_isBuildingConstructor(mIdx)) {
        // put item into staging area
        if (!canAcceptManipulatorCommand(mIdx)) return true;
        manipulatorCommands.put(mIdx, new AddToStagingAreaUpdate(this, item));
        return true;
      } else {
        // normal reactor
        if (!manipulator_isReacting(mIdx)) {
          manipulatorPrivateBuffer.get(mIdx).addLast(item);
          tryToStartReaction(mIdx);
          return true;
        } else {
          return false; // can't accept new items while a reaction is being performed
        }
      }
    } else {
      // world manipulator
      if (!canAcceptManipulatorCommand(mIdx)) return true;
      manipulatorCommands.put(mIdx, new PutWithManipulatorUpdate(this, mIdx, item));
      return true;
    }
  }
  
  // get manipulator status
  public ByteBuffer manipulator_MSTAT(int mIdx) {
    if (mIdx < 0 || mIdx >= getNumberOfManipulators()) {
      return null;
    }
    
    // total hack for reactor-type manipulators
    if (manipulator_canSetReaction(mIdx)) {
      if (manipulator_isBuildingConstructor(mIdx)) {
        // TODO
        throw new UnsupportedOperationException("not implemented yet");
      } else {
        // normal reactor
        // the report is a single uint32:
        // the high bit is set if a reaction was started since the last time
        // this manipulator received an MSTAT command, and the
        // low 31 bits are the unsigned number of timesteps until 
        // the reaction in progress, if any, is complete
        ByteBuffer response = ByteBuffer.allocate(4);
        response.order(ByteOrder.LITTLE_ENDIAN);
        response.position(0);
        int statusWord = 0;
        Integer i = manipulatorReactionTimeRemaining.get(mIdx);
        if (i != null && i >= 0) {
          statusWord = (i & 0x7FFFFFFF);
        }
        // set bit 31
        Boolean started = manipulatorReactionStartSinceLastMSTAT.get(mIdx);
        if (started != null && started.booleanValue()) {
          statusWord |= 0x80000000;
        }
        // clear "manipulator status checked" flag
        manipulatorReactionStartSinceLastMSTAT.put(mIdx, false);
        response.putInt(statusWord);
        response.position(0);
        return response;
      }
    }
    
    // no support
    return null;
  }
  
}
