package io.lp0onfire.ssi.model;

/**
 * A Structure is a voxel occupant that typically represents a 
 * terrain feature, building piece, or other non-mechanical large object.
 * Structures are always made out of a single material and cannot move on their own.
 * They also cannot be directly manipulated by robots, but can be manipulated
 * indirectly (e.g. mining out a block or placing a floor tile).
 * Structures always occupy exactly one voxel.
 */
public abstract class Structure extends VoxelOccupant {

  private final Material material;
  public Material getMaterial() {
    return this.material;
  }
  
  public Structure(Material material) {
    this.material = material;
  }
  
  public Vector getExtents() {
    return new Vector(1, 1, 1);
  }
  
  public boolean canMove() {
    return false;
  }
  
  @Override
  public boolean requiresTimestep() {
    return false;
  }
  @Override
  public void timestep(){}
  
}
