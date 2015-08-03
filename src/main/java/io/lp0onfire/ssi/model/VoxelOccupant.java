package io.lp0onfire.ssi.model;

public interface VoxelOccupant {

  boolean impedesMovement();
  boolean impedesXYFluidFlow();
  boolean impedesZFluidFlow();
  boolean supportsOthers();
  boolean needsSupport();
  
}
