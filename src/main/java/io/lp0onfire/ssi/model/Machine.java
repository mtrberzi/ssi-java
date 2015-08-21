package io.lp0onfire.ssi.model;

/**
 * A Machine is a voxel occupant that represents a mechanical device or robot.
 * Machines cannot be directly manipulated by robots, but can be manipulated indirectly.
 * Machines may occupy one or more voxels.
 */
public abstract class Machine extends VoxelOccupant {

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
  
}
