package io.lp0onfire.ssi.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class VoxelOccupant {
  
  // when UUIDs are transferred to and from the microcontroller,
  // the least significant long always appears at the lower address in memory
  private UUID uuid;
  public UUID getUUID() {
    return this.uuid;
  }
  public void setUUID(UUID id) {
    this.uuid = id;
  }
  
  public VoxelOccupant() {
    this.uuid = UUID.randomUUID();
  }
  
  public VoxelOccupant(UUID uuid) {
    this.uuid = uuid;
  }
  
  public abstract boolean impedesXYMovement();
  public abstract boolean impedesZMovement();
  public abstract boolean impedesXYFluidFlow();
  public abstract boolean impedesZFluidFlow();
  public abstract boolean supportsOthers();
  public abstract boolean needsSupport();
  public abstract boolean canMove();
  
  public abstract short getKind();
  public abstract int getType(); // more specific than kind
  
  private Vector position;
  public Vector getPosition() {
    return this.position;
  }
  public void setPosition(Vector p) {
    this.position = p;
  }
  // gets the extent of space occupied by this thing
  public abstract Vector getExtents();
  
  // invariant: each component of subvoxelPosition is at least 0
  // and smaller than TimeConstants.SUBVOXELS_PER_VOXEL
  private Vector subvoxelPosition;
  public Vector getSubvoxelPosition() {
    return this.subvoxelPosition;
  }
  public void setSubvoxelPosition(Vector p) {
    this.subvoxelPosition = p;
  }
  
  private Vector velocity = new Vector(0, 0, 0);
  public Vector getVelocity() {
    return this.velocity;
  }
  public void setVelocity(Vector v) {
    this.velocity = v;
  }
  // invariant: the absolute value of each component of
  // subvoxelVelocity is no larger than TimeConstants.SUBVOXELS_PER_VOXEL
  private Vector subvoxelVelocity = new Vector(0, 0, 0);
  public Vector getSubvoxelVelocity() {
    return this.subvoxelVelocity;
  }
  public void setSubvoxelVelocity(Vector v) {
    this.subvoxelVelocity = v;
  }
  
  // true iff the object has stuff to do before the timestep update,
  // e.g. running a processor
  public boolean requiresPreprocessing() {
    return false;
  }
  public void preprocess() {}
  
  public abstract boolean requiresTimestep();
  public void timestep() {}
  
  public abstract boolean hasWorldUpdates();
  // this method is always called after the call to timestep(),
  // but only if hasWorldUpdates() is true
  public List<WorldUpdate> getWorldUpdates() {
    return new ArrayList<>();
  }
  public void collectUpdateResults(Map<WorldUpdate, WorldUpdateResult> results) {}
  
  // really, really simplistic "damage" system
  private int durability = 1;
  public int getCurrentDurability() { return this.durability; }
  public void setCurrentDurability(int d) { this.durability = d; }
  
  public abstract int getMaximumDurability();
  
}
