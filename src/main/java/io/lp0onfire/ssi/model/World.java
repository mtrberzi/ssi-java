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
    
    boolean canMoveThere = true;
    for (int x = position.getX(); x < position.getX() + obj.getExtents().getX(); ++x) {
      for (int y = position.getY(); y < position.getY() + obj.getExtents().getY(); ++y) {
        for (int z = position.getZ(); z < position.getZ() + obj.getExtents().getZ(); ++z) {
          Vector v = new Vector(x, y, z);
          if (!inBounds(v)) return false;
          Set<VoxelOccupant> occupants = getOccupants(v);
          for (VoxelOccupant occ : occupants) {
            if (occ.impedesMovement()) {
              canMoveThere = false;
              break;
            }
          }
        } 
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
    // add the object to every voxel that it occupies
    for (int x = position.getX(); x < position.getX() + obj.getExtents().getX(); ++x) {
      for (int y = position.getY(); y < position.getY() + obj.getExtents().getY(); ++y) {
        for (int z = position.getZ(); z < position.getZ() + obj.getExtents().getZ(); ++z) {
          if (!voxels.containsKey(position)) {
            voxels.put(position, new HashSet<>());
          }
          Set<VoxelOccupant> occupants = getOccupants(new Vector(x, y, z));
          occupants.add(obj);
        } 
      } 
    }

    obj.setPosition(position);
    obj.setSubvoxelPosition(subvoxelPosition);
    return true;
  }
  
  public void removeOccupant(VoxelOccupant obj) {
    for (int x = obj.getPosition().getX(); x < obj.getPosition().getX() + obj.getExtents().getX(); ++x) {
      for (int y = obj.getPosition().getY(); y < obj.getPosition().getY() + obj.getExtents().getY(); ++y) {
        for (int z = obj.getPosition().getZ(); z < obj.getPosition().getZ() + obj.getExtents().getZ(); ++z) {
          Set<VoxelOccupant> occupants = getOccupants(new Vector(x, y, z));
          if (occupants.contains(obj)) {
            occupants.remove(obj);
          }
        } 
      } 
    }
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
  
  // Find the smallest positive t such that s + t*ds is an integer
  private double intbound(double s, double ds) {
    if (ds < 0.0) {
      return intbound(-s, -ds);
    } else {
      s = s % 1;
      // now we have s + t*ds = 1
      return (1 - s) / ds;
    }
  }
  
  /**
   * Traces a ray from the center of an origin voxel in a given direction
   * (with distances measured in voxels),
   * stopping when the voxel at (origin + direction) is reached
   * or when the resulting position would be outside of the world.
   * 
   * This algorithm is based on an algorithm presented in the paper
   * "A Fast Voxel Traversal Algorithm for Ray Tracing",
   * by J. Amanatides and A. Woo, 1987
   * (http://www.cse.yorku.ca/~amana/research/grid.pdf) 
   * 
   * @param origin The position from which to start the trace
   * @param direction The translation vector along which to trace
   * @return A list of all voxels passed through by the ray,
   * in the order in which they are visited starting at origin,
   * not including the origin itself but including the endpoint
   * (unless it coincides with the origin)
   */
  public List<Vector> raycast(Vector origin, Vector direction) {
    List<Vector> visitedVoxels = new LinkedList<>();
    
    int x = origin.getX();
    int y = origin.getY();
    int z = origin.getZ();
    
    int dx = direction.getX();
    int dy = direction.getY();
    int dz = direction.getZ();
    
    int stepX = Integer.signum(dx);
    int stepY = Integer.signum(dy);
    int stepZ = Integer.signum(dz);
    
    double tMaxX = intbound(x + 0.5, dx);
    double tMaxY = intbound(y + 0.5, dy);
    double tMaxZ = intbound(z + 0.5, dz);
    
    double tDeltaX = (double)(stepX) / (double)(dx);
    double tDeltaY = (double)(stepY) / (double)(dy);
    double tDeltaZ = (double)(stepZ) / (double)(dz);
    
    if (dx == 0 && dy == 0 && dz == 0) {
      return visitedVoxels;
    }
    
    Vector destination = origin.add(direction);
    while (true) {
      // find the next voxel we enter
      if (tMaxX < tMaxY) {
        if (tMaxX < tMaxZ) {
          x += stepX;
          tMaxX += tDeltaX;
        } else {
          z += stepZ;
          tMaxZ += tDeltaZ;
        }
      } else {
        if (tMaxY < tMaxZ) {
          y += stepY;
          tMaxY += tDeltaY;
        } else {
          z += stepZ;
          tMaxZ += tDeltaZ;
        }
      }
      Vector nextVoxel = new Vector(x, y, z);
      if (!inBounds(nextVoxel)) break;
      visitedVoxels.add(nextVoxel);
      if (nextVoxel.equals(destination)) break;
    } // while()
    
    return visitedVoxels;
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
        if (!occupant.getSubvoxelVelocity().equals(zeroVector)
            || !occupant.getVelocity().equals(zeroVector)) {
          movingObjects.add(occupant);
        }
      }
    }

    // perform movement updates
    // TODO this could potentially be parallelized, but updating the map would require locking/concurrent data structures
    for (VoxelOccupant obj : movingObjects) {
      // calculate new position
      Vector newPos = obj.getPosition().add(obj.getVelocity());
      int newX = newPos.getX();
      int newY = newPos.getY();
      int newZ = newPos.getZ();
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
      
      newPos = new Vector(newX, newY, newZ);
      newSVPos = new Vector(newX_sv, newY_sv, newZ_sv);

      // if the new voxel position is different from the old one,
      // we do a second check to make sure we don't clip through any solid objects
      if (!newPos.equals(obj.getPosition())) {
        List<Vector> visitedPositions = raycast(obj.getPosition(), newPos.subtract(obj.getPosition()));
        for (Vector candidatePosition : visitedPositions) {
          if (canOccupy(candidatePosition, obj)) {
            newPos = candidatePosition;
          } else {
            // TODO collision logic
            // stop before we enter this voxel
            break;
          }
        }
      }
      // now newPos and newSVPos are correct;
      // we remove the object from its old position and add it to its new one
      // TODO check whether the object is supported
      removeOccupant(obj);
      addOccupant(newPos, newSVPos, obj);
    }
  }
  
}
