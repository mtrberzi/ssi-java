package io.lp0onfire.ssi.model;

/**
 * A Structure is a voxel occupant that typically represents a 
 * terrain feature, building piece, or other non-mechanical large object.
 * Structures are always made out of a single material and cannot move on their own.
 * They also cannot be directly manipulated by robots, but can be manipulated
 * indirectly (e.g. mining out a block or placing a floor tile).
 * Structures always occupy exactly one voxel.
 * Every structure has a base durability, which is modified by the material's durability factor
 * to compute the structure's maximum durability.
 */
public abstract class Structure extends VoxelOccupant {

  private final Material material;
  public Material getMaterial() {
    return this.material;
  }
  
  public Structure(Material material) {
    this.material = material;
    setCurrentDurability(getMaximumDurability());
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
  
  @Override
  public boolean hasWorldUpdates() {
    return false;
  }
  
  public short getKind() {
    return (short)4;
  }
  
  protected abstract int getBaseDurability();
  
  @Override
  public int getMaximumDurability() {
    return (int)Math.floor(getBaseDurability() * getMaterial().getDurabilityModifier());
  }
  
}
