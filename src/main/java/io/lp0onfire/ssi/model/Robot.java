package io.lp0onfire.ssi.model;

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
  
  // engine control interface
  
  public abstract int getNumberOfEngines();
  public abstract int engine_getIdentification(int eIdx);
  public abstract int engine_getStatus(int eIdx);
  public abstract void engine_setControl(int eIdx, int powerLevel);
  protected abstract void engine_timestep(); 
  
}
