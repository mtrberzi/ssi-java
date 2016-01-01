package io.lp0onfire.ssi.model.robots;

import io.lp0onfire.ssi.TimeConstants;
import io.lp0onfire.ssi.microcontroller.Microcontroller;
import io.lp0onfire.ssi.model.Robot;
import io.lp0onfire.ssi.model.Vector;

/**
 * A Robot class with six axial thrusters, two opposing in each vector direction.
 * Subclasses configure the parameters of the configured thrusters by implementing several methods.
 * Thrusters are assigned to engine indices as follows:
 * #0: positive-X
 * #1: negative-X
 * #2: positive-Y
 * #3: negative-Y
 * #4: positive-Z
 * #5: negative-Z
 * The label indicates the direction in which force is applied by that thruster,
 * so that e.g. firing the positive-X thruster and nothing else creates a net force
 * in the positive-X direction on the robot.
 */
public abstract class AxialThrustRobot extends Robot {

  public AxialThrustRobot(Microcontroller mcu) {
    super(mcu);
    for (int i = 0; i < 6; ++i) {
      engineCommandedPower[i] = 0;
      engineActualPower[i] = 0;
    }
  }

  @Override
  public boolean needsSupport() {
    return false;
  }

  public int getNumberOfEngines() {
    return 6;
  }
  
  private int[] engineCommandedPower = new int[6];
  private int[] engineActualPower = new int[6];
  private double previousAccelerationX = 0.0;
  private double previousAccelerationY = 0.0;
  private double previousAccelerationZ = 0.0;
  
  public abstract int engine_getIdentification(int eIdx);
  
  public int engine_getStatus(int eIdx) {
    // TODO
    return 0;
  }
  
  public void engine_setControl(int eIdx, int powerLevel) {
    if (eIdx < 0 || eIdx >= getNumberOfEngines()) {
      return;
    }
    if (powerLevel > 0 && powerLevel > getThrusterMaxPowerLevel()) {
      engineCommandedPower[eIdx] = getThrusterMaxPowerLevel();
    } else if (powerLevel < 0 && powerLevel < -getThrusterMaxPowerLevel()) {
      engineCommandedPower[eIdx] = -getThrusterMaxPowerLevel();
    } else {
      engineCommandedPower[eIdx] = powerLevel;
    }
  }
  
  protected double calculateThrusterNetForce(int positiveIdx, int negativeIdx) {
    // for both thrusters, calculate the percentage of maximum power, from -1.0 to +1.0
    double positivePower = ((double)engineActualPower[positiveIdx]) / ((double)getThrusterMaxPowerLevel());
    double negativePower = ((double)engineActualPower[negativeIdx]) / ((double)getThrusterMaxPowerLevel());
    
    double positiveForce = getThrusterForce() * positivePower;
    double negativeForce = getThrusterForce() * negativePower;
    
    return positiveForce - negativeForce;
  }
  
  protected void engine_timestep() {
    // adjust actual engine power, limited by slew rate
    for (int i = 0; i < getNumberOfEngines(); ++i) {
      if (engineCommandedPower[i] > engineActualPower[i]) {
        // increase actual power by at most slew rate
        int powerDifference = engineCommandedPower[i] - engineActualPower[i];
        if (powerDifference <= getThrusterPowerSlewRate()) {
          engineActualPower[i] = engineCommandedPower[i];
        } else {
          engineActualPower[i] += getThrusterPowerSlewRate();
        }
      } else if (engineCommandedPower[i] < engineActualPower[i]) {
        // decrease actual power by at most slew rate
        int powerDifference = engineActualPower[i] - engineCommandedPower[i];
        if (powerDifference <= getThrusterPowerSlewRate()) {
          engineActualPower[i] = engineCommandedPower[i];
        } else {
          engineActualPower[i] -= getThrusterPowerSlewRate();
        }
      }
    }
    // calculate net force in each direction
    double netForceX = calculateThrusterNetForce(0, 1);
    double netForceY = calculateThrusterNetForce(2, 3);
    double netForceZ = calculateThrusterNetForce(4, 5);
    
    // now calculate acceleration = force / mass in each direction
    double currentAccelerationX = netForceX / getMass();
    double currentAccelerationY = netForceY / getMass();
    double currentAccelerationZ = netForceZ / getMass();
    
    // integral of (a_prev + (a_curr - a_prev)*t)dt from 0 to 1
    // is (a_prev + a_curr) / 2
    
    double averageAccelerationX = (previousAccelerationX + currentAccelerationX) / 2.0;
    double averageAccelerationY = (previousAccelerationY + currentAccelerationY) / 2.0;
    double averageAccelerationZ = (previousAccelerationZ + currentAccelerationZ) / 2.0;
    
    // now we can just use v_f = v_0 + a*t
    Vector oldVoxelVelocity = getVelocity();
    Vector oldSubvoxelVelocity = getSubvoxelVelocity();
    // all of these are in voxels per timestep
    double oldVx = (double)oldVoxelVelocity.getX() + (double)oldSubvoxelVelocity.getX() * (double)TimeConstants.SUBVOXELS_PER_VOXEL;
    double oldVy = (double)oldVoxelVelocity.getY() + (double)oldSubvoxelVelocity.getY() * (double)TimeConstants.SUBVOXELS_PER_VOXEL;
    double oldVz = (double)oldVoxelVelocity.getZ() + (double)oldSubvoxelVelocity.getZ() * (double)TimeConstants.SUBVOXELS_PER_VOXEL;
    
    double newVx = oldVx + averageAccelerationX;
    double newVy = oldVy + averageAccelerationY;
    double newVz = oldVz + averageAccelerationZ;
    
    // convert back to voxel/subvoxel
    int newVoxelVx = (int)newVx;
    int newVoxelVy = (int)newVy;
    int newVoxelVz = (int)newVz;
    
    int newSubvoxelVx = (int)Math.floor((newVx - (int)newVx) * (double)TimeConstants.SUBVOXELS_PER_VOXEL);
    int newSubvoxelVy = (int)Math.floor((newVy - (int)newVy) * (double)TimeConstants.SUBVOXELS_PER_VOXEL);
    int newSubvoxelVz = (int)Math.floor((newVz - (int)newVz) * (double)TimeConstants.SUBVOXELS_PER_VOXEL);
    
    // set velocities
    setVelocity(new Vector(newVoxelVx, newVoxelVy, newVoxelVz));
    setSubvoxelVelocity(new Vector(newSubvoxelVx, newSubvoxelVy, newSubvoxelVz));
    
    // update previousAccel := currentAccel
    previousAccelerationX = currentAccelerationX;
    previousAccelerationY = currentAccelerationY;
    previousAccelerationZ = currentAccelerationZ;
  }
  
  /**
   * @return maximum thruster force, expressed in units of kilogram-voxels per timestep squared
   */
  protected abstract double getThrusterForce();
  
  /**
   * @return maximum thruster power level, corresponding to maximum force exerted
   */
  protected abstract int getThrusterMaxPowerLevel();
  
  /**
   * @return the maximum rate of change of commanded thruster power per timestep
   */
  protected abstract int getThrusterPowerSlewRate();
  
}
