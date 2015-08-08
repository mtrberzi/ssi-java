package io.lp0onfire.ssi.model;

public class CreateTransportTubeUpdate extends WorldUpdate {

  private final String transportID;
  private final Vector position;
  
  public CreateTransportTubeUpdate(Vector position, String transportID) {
    this.position = position;
    this.transportID = transportID;
  }
  
  @Override
  public WorldUpdateResult apply(World w) {
    TransportTube transport = new TransportTube(transportID);
    if (!w.canOccupy(position, transport)) {
      return new WorldUpdateResult(false);
    }
    boolean result = w.addOccupant(position, new Vector(0,0,0), transport);
    return new WorldUpdateResult(result);
  }

}
