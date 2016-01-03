package io.lp0onfire.ssi.model;

import java.util.List;

import io.lp0onfire.ssi.microcontroller.Microcontroller;

public abstract class Robot extends Machine {

  public Robot(Microcontroller mcu) {
    super(mcu);
  }

  @Override
  public boolean impedesXYMovement() {
    return false;
  }

  @Override
  public boolean impedesZMovement() {
    return false;
  }

  @Override
  public boolean impedesXYFluidFlow() {
    return false;
  }

  @Override
  public boolean impedesZFluidFlow() {
    return false;
  }

  @Override
  public boolean supportsOthers() {
    return false;
  }

  @Override
  public boolean canMove() {
    return true;
  }

  @Override
  public short getKind() {
    return (short)5;
  }

  @Override
  public Vector getExtents() {
    return new Vector(1, 1, 1);
  }
  
  // Make 100% sure that everything that overrides this calls super to this method.
  @Override
  public void timestep() {
    super.timestep();
    engine_timestep();
  }
  
  // TODO this should be on all items
  public abstract double getMass();
  
  @Override
  public boolean hasWorldUpdates() {
    return super.hasWorldUpdates() || miningLaserFired;
  }
  
  @Override
  public List<WorldUpdate> getWorldUpdates() {
    List<WorldUpdate> updates = super.getWorldUpdates();
    if (miningLaserFired) {
      miningLaserFired = false;
      updates.add(miningLaserUpdate);
    }
    return updates;
  }
  
  // engine control interface
  
  public abstract int getNumberOfEngines();
  public abstract int engine_getIdentification(int eIdx);
  public abstract int engine_getStatus(int eIdx);
  public abstract void engine_setControl(int eIdx, int powerLevel);
  protected abstract void engine_timestep(); 
  
  // mining laser interface
  
  public boolean hasMiningLaser() {
    return false;
  }
  public int miningLaser_getMaximumPowerLevel() { return 1; }
  // number of durability points removed at 100% power
  public int miningLaser_getMaximumDamage() { return 0; }
  
  private boolean miningLaserFired = false;
  private WorldUpdate miningLaserUpdate = null;
  
  public void miningLaser_fire(Vector target, int powerLevel) {
    if (!hasMiningLaser()) {
      return;
    }
    if (powerLevel == 0) {
      return;
    }
    // compute laser power as a fraction of maximum power
    if (powerLevel > miningLaser_getMaximumPowerLevel()) {
      powerLevel = miningLaser_getMaximumPowerLevel();
    }
    int damage = (int)Math.round( (double)miningLaser_getMaximumDamage() * ((double)powerLevel / (double)miningLaser_getMaximumPowerLevel()) );
    miningLaserUpdate = new MiningLaserUpdate(this, target, damage);
    miningLaserFired = true;
  }
  
}
