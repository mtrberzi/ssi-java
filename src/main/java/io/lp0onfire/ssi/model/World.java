package io.lp0onfire.ssi.model;

import io.lp0onfire.ssi.model.structures.Bedrock;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class World {
  
  private Map<Vector, List<VoxelOccupant>> voxels = new HashMap<>();
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
  
  public List<VoxelOccupant> getOccupants(Vector position) {
    if (!inBounds(position)) return new LinkedList<>();
    if (!voxels.containsKey(position)) return new LinkedList<>();
    return voxels.get(position);
  }
  
  /**
   * @param position
   * @param obj
   * @return true if the object could be placed there, and false otherwise
   */
  public boolean addOccupant(Vector position, VoxelOccupant obj) {
    if (!inBounds(position)) return false;
    List<VoxelOccupant> occupants = getOccupants(position);
    
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
      voxels.put(position, new LinkedList<>());
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
  
}
