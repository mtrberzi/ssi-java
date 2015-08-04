package io.lp0onfire.ssi.model;

import io.lp0onfire.ssi.TimeConstants;
import io.lp0onfire.ssi.model.structures.Bedrock;

import java.util.ArrayList;
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
  
  public boolean canOccupy(Vector position, VoxelOccupant obj) {
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
    return true;
  }
  
  /**
   * @param position
   * @param obj
   * @return true if the object could be placed there, and false otherwise
   */
  public boolean addOccupant(Vector position, Vector subvoxelPosition, VoxelOccupant obj) {
    if (!canOccupy(position, obj)) return false;
    
    // all checks passed, place the object
    if (!voxels.containsKey(position)) {
      voxels.put(position, new HashSet<>());
    }
    // re-fetch occupants in case the key was newly created
    Set<VoxelOccupant> occupants = getOccupants(position);
    occupants.add(obj);
    obj.setPosition(position);
    obj.setSubvoxelPosition(subvoxelPosition);
    return true;
  }
  
  private void createBedrockLayer() {
    Vector subvoxelPos = new Vector(0, 0, 0);
    for (int y = 0; y < yDim; ++y) {
      for (int x = 0; x < xDim; ++x) {
        Vector pos = new Vector(x, y, 0);
        Bedrock bedrock = new Bedrock();
        if (!addOccupant(pos, subvoxelPos, bedrock)) {
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
    
    Vector zeroVector = new Vector(0, 0, 0);
    Set<VoxelOccupant> movingObjects = new HashSet<>();
    for (Set<VoxelOccupant> occupants : voxels.values()) {
      for (VoxelOccupant occupant : occupants) {
        // if velocity component is non-zero, the object is moving
        if (!occupant.getSubvoxelVelocity().equals(zeroVector)) {
          movingObjects.add(occupant);
        }
      }
    }
    Map<VoxelOccupant, Vector> newPositions = new HashMap<>();
    Map<VoxelOccupant, Vector> newSubvoxelPositions = new HashMap<>();
    Set<VoxelOccupant> changedPositionOccupants = new HashSet<>();
    for (VoxelOccupant obj : movingObjects) {
      // calculate new position
      int newX = obj.getPosition().getX();
      int newY = obj.getPosition().getY();
      int newZ = obj.getPosition().getZ();
      Vector newSVPos = obj.getSubvoxelPosition().add(obj.getSubvoxelVelocity());
      int newX_sv = newSVPos.getX();
      int newY_sv = newSVPos.getY();
      int newZ_sv = newSVPos.getZ();
      
      // determine whether we've ended up in a new voxel
      
      if (newX_sv <= -TimeConstants.SUBVOXELS_PER_VOXEL) {
        newX_sv += TimeConstants.SUBVOXELS_PER_VOXEL;
        --newX;
      } else if (newX_sv >= TimeConstants.SUBVOXELS_PER_VOXEL) {
        newX_sv -= TimeConstants.SUBVOXELS_PER_VOXEL;
        ++newX;
      }
      
      if (newY_sv <= -TimeConstants.SUBVOXELS_PER_VOXEL) {
        newY_sv += TimeConstants.SUBVOXELS_PER_VOXEL;
        --newY;
      } else if (newY_sv >= TimeConstants.SUBVOXELS_PER_VOXEL) {
        newY_sv -= TimeConstants.SUBVOXELS_PER_VOXEL;
        ++newY;
      }
      
      if (newZ_sv <= -TimeConstants.SUBVOXELS_PER_VOXEL) {
        newZ_sv += TimeConstants.SUBVOXELS_PER_VOXEL;
        --newZ;
      } else if (newZ_sv >= TimeConstants.SUBVOXELS_PER_VOXEL) {
        newZ_sv -= TimeConstants.SUBVOXELS_PER_VOXEL;
        ++newZ;
      }
      
      Vector newPos = new Vector(newX, newY, newZ);
      newSVPos = new Vector(newX_sv, newY_sv, newZ_sv);
      // if the new position is out of bounds, we stay put and stop moving
      if (!inBounds(newPos)) {
        obj.setSubvoxelVelocity(new Vector(0,0,0));
        continue;
      }
      // TODO determine final location of object
    }
    // TODO update positions and check collisions
  }
  
}
