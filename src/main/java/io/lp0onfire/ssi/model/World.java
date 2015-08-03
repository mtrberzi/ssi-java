package io.lp0onfire.ssi.model;

import io.lp0onfire.ssi.model.structures.Bedrock;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class World {
  
  private Map<Vector, Set<VoxelOccupant>> voxels = new HashMap<>();
  private final int xDim;
  public int getXDim() {
    return this.xDim;
  }
  private final int yDim;
  public int getYDim() {
    return this.yDim;
  }
  
  public World(int xDim, int yDim) {
    this.xDim = xDim;
    this.yDim = yDim;
    createBedrockLayer();
  }
  
  public boolean inBounds(Vector position) {
    if (position.getX() < 0 || position.getY() < 0 || position.getZ() < 0) return false;
    if (position.getX() >= xDim || position.getY() >= yDim) return false;
    return true;
  }
  
  public Set<VoxelOccupant> getOccupants(Vector position) {
    if (!inBounds(position)) return new HashSet<>();
    if (!voxels.containsKey(position)) return new HashSet<>();
    return voxels.get(position);
  }
  
  /**
   * @param position
   * @param obj
   * @return true if the object could be placed there, and false otherwise
   */
  public boolean addOccupant(Vector position, VoxelOccupant obj) {
    if (!inBounds(position)) return false;
    Set<VoxelOccupant> occupants = getOccupants(position);
    
    boolean canMoveThere = true;
    for (VoxelOccupant occ : occupants) {
      if (occ.impedesMovement()) {
        canMoveThere = false;
        break;
      }
    }
    if (!canMoveThere) return false;
    
    // all checks passed, place the object
    if (!voxels.containsKey(position)) {
      voxels.put(position, new HashSet<>());
    }
    // re-fetch occupants in case the key was newly created
    occupants = getOccupants(position);
    occupants.add(obj);
    return true;
  }
  
  private void createBedrockLayer() {
    for (int y = 0; y < yDim; ++y) {
      for (int x = 0; x < xDim; ++x) {
        Vector pos = new Vector(x, y, 0);
        Bedrock bedrock = new Bedrock();
        if (!addOccupant(pos, bedrock)) {
          throw new IllegalStateException("error placing bedrock layer at " + pos.toString());
        }
      }
    }
  }
  
  public void timestep() {
    // build up a list of all objects that require pre-timestep processing
    // TODO maybe cache this?
    List<VoxelOccupant> preprocessList = new LinkedList<>();
    for (Set<VoxelOccupant> occupants : voxels.values()) {
      for (VoxelOccupant occupant : occupants) {
        if (occupant.requiresPreprocessing()) {
          preprocessList.add(occupant);
        }
      }
    }
    // run processing for each occupant
    // TODO these can be run in parallel
    for (VoxelOccupant proc : preprocessList) {
      proc.preprocess();
    }
    
    // perform timestep update
    // TODO maybe cache these too
    for (Set<VoxelOccupant> occupants : voxels.values()) {
      for (VoxelOccupant occupant : occupants) {
        if (occupant.requiresTimestep()) {
          occupant.timestep();
        }
      }
    }
    
    // TODO how to deal with sub-voxel movement?
    Vector zeroVector = new Vector(0, 0, 0);
    List<VoxelOccupant> movingObjects = new LinkedList<>();
    for (Set<VoxelOccupant> occupants : voxels.values()) {
      for (VoxelOccupant occupant : occupants) {
        if (!(occupant.getVelocity().equals(zeroVector))) {
          movingObjects.add(occupant);
        }
      }
    }
    // TODO complete movement code, collision detection and resolution
  }
  
}
