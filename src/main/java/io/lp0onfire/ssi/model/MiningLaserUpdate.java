package io.lp0onfire.ssi.model;

import io.lp0onfire.ssi.model.structures.Block;

import java.util.List;
import java.util.stream.Collectors;

/** 
 * Helper function for mining lasers; reduces durability at a target relative to a robot's position.
 */
public class MiningLaserUpdate extends WorldUpdate {

  private final Robot robot;
  private final Vector displacement;
  private final int damage;
  
  public MiningLaserUpdate(Robot robot, Vector displacement, int damage) {
    this.robot = robot;
    this.displacement = displacement;
    this.damage = damage;
  }
  
  @Override
  public WorldUpdateResult apply(World w) {
    // find the first block along this ray
    List<Vector> ray = w.raycast(robot.getPosition(), displacement);
    Block block = null;
    for (Vector pos : ray) {
      List<Block> blocks = w.getOccupants(pos).stream()
          .filter((o -> o instanceof Block)).map((o -> (Block)o)).collect(Collectors.toList());
      if (!blocks.isEmpty()) {
        block = blocks.get(0);
        break;
      }
    }
    
    if (block != null) {
      // now that we've found our target, reduce its durability
      WorldUpdate chain = new ReduceDurabilityUpdate(block, damage);
      chain.apply(w);
    }
    
    // always successful
    return new WorldUpdateResult(true);
  }

}
