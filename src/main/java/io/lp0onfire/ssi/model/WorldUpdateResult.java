package io.lp0onfire.ssi.model;

public class WorldUpdateResult {

  private final boolean succeeded;
  public boolean wasSuccessful() {
    return this.succeeded;
  }
  
  public WorldUpdateResult(boolean success) {
    this.succeeded = success;
  }
  
}
