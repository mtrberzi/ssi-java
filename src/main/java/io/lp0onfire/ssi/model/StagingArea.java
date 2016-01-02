package io.lp0onfire.ssi.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.lp0onfire.ssi.model.reactions.Reaction;

/**
 * A StagingArea is a temporary object that exists to claim an area for construction.
 * It is associated with a reaction that creates some object, and can receive reactants
 * from robots via special manipulators.
 */
public class StagingArea extends VoxelOccupant {

  private final Vector extents;
  @Override
  public Vector getExtents() {
    return extents;
  }
  
  public StagingArea(Vector extents, Reaction reaction) {
    this.extents = extents;
    this.reaction = reaction;
    setCurrentDurability(getMaximumDurability());
  }
  
  @Override
  public boolean impedesXYMovement() {
    return false;
  }

  @Override
  public boolean impedesZMovement() {
    return false;
  }

  @Override
  public boolean impedesXYFluidFlow() {
    return false;
  }

  @Override
  public boolean impedesZFluidFlow() {
    return false;
  }

  @Override
  public boolean supportsOthers() {
    return false;
  }

  @Override
  public boolean needsSupport() {
    return false;
  }

  @Override
  public boolean canMove() {
    return false;
  }

  @Override
  public short getKind() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int getType() {
    // TODO it would be great if the type of the staging area = the type of the object
    return 0;
  }

  @Override
  public int getMaximumDurability() { return 1; }
  
  @Override
  public boolean requiresTimestep() {
    return true;
  }

  private List<WorldUpdate> updates = new LinkedList<>();
  
  @Override
  public boolean hasWorldUpdates() {
    return !updates.isEmpty();
  }
  
  @Override
  public List<WorldUpdate> getWorldUpdates() {
    return updates;
  }
  
  @Override
  public void collectUpdateResults(Map<WorldUpdate, WorldUpdateResult> results) {
    for (Map.Entry<WorldUpdate, WorldUpdateResult> entry : results.entrySet()) {
      WorldUpdate update = entry.getKey();
      WorldUpdateResult result = entry.getValue();
      if (!result.wasSuccessful()) {
        // TODO error-handling, maybe?
        throw new IllegalStateException("construction failed");
      }
    }
  }
  
  private final Reaction reaction;
  public Reaction getReaction() {
    return this.reaction;
  }
  
  private List<Item> reactants = new LinkedList<>();
  private boolean changedReactants = false;

  private boolean acceptsReactants = true;
  public boolean canAcceptReactants() {
    return this.acceptsReactants;
  }
  
  public void addReactant(Item i) {
    reactants.add(i);
    changedReactants = true;
  }
  
  @Override
  public void timestep() {
    super.timestep();
    // only bother checking the reaction if we changed the reactants
    if (changedReactants) {
      changedReactants = false;
      if (reaction.reactantsOK(reactants)) {
        // ignore time. just do the reaction
        Reaction.Result result = reaction.react(reactants);
        if (!result.successful()) {
          throw new IllegalStateException("unexpected reaction failure in staging area");
        }
        List<Item> consumedReactants = result.getConsumedReactants();
        reactants.removeAll(consumedReactants);
        List<VoxelOccupant> newObjs = result.getCreatedObjects();
        // add each new object to the world, as well as all of the items we didn't use
        for (VoxelOccupant obj : newObjs) {
          updates.add(new RelativeAddObjectUpdate(this, new Vector(0, 0, 0), obj));
        }
        for (Item i : reactants) {
          updates.add(new RelativeAddObjectUpdate(this, new Vector(0, 0, 0), i));
        }
        // remove this staging area from the world
        updates.add(new RelativeRemoveObjectUpdate(this, new Vector(0,0,0), this));
        // prevent stuff from being added after we've already tried to start building;
        // this should avert a potentially nasty concurrency issue where stuff
        // can be added to this and lost forever after we've issued these updates
        acceptsReactants = false;
      }
    }
  }

}
