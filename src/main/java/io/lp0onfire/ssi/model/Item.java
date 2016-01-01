package io.lp0onfire.ssi.model;

/**
 * An Item is a voxel occupant that represents a tool, raw material, or part.
 * They are always made out of a single material.
 * Items can be directly manipulated by robots and machines,
 * and carried by conveyors. Items always occupy
 * exactly one voxel, cannot move on their own, and must be supported.
 */
public abstract class Item extends VoxelOccupant {

  private final Material material;
  public Material getMaterial() {
    return this.material;
  }
  
  public Item(Material material) {
    this.material = material;
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
    return true;
  }

  @Override
  public boolean canMove() {
    return false;
  }

  @Override
  public Vector getExtents() {
    return new Vector(1, 1, 1);
  }

  @Override
  public boolean requiresTimestep() {
    return false;
  }

  @Override
  public boolean hasWorldUpdates() {
    return false;
  }

}
