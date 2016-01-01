package io.lp0onfire.ssi.model;

public class ConnectTransportTubeUpdate extends WorldUpdate {

  private final String transportID;
  private final Vector firstPosition;
  private final Vector secondPosition;
  
  public ConnectTransportTubeUpdate(String transportID, Vector firstPosition, Vector secondPosition) {
    // TODO make sure these vectors are adjacent
    this.transportID = transportID;
    this.firstPosition = firstPosition;
    this.secondPosition = secondPosition;
  }
  
  private TransportTube findTransport(World w, Vector position, String transportID) {
    for (VoxelOccupant occ : w.getOccupants(position)) {
      if (!(occ instanceof TransportTube)) continue;
      TransportTube transport = (TransportTube)occ;
      if (transport.getTransportID().equals(transportID)) {
        return transport;
      }
    }
    return null;
  }
  
  @Override
  public WorldUpdateResult apply(World w) {
    // look for transport tubes with this transportID in first/second positions
    TransportTube first = findTransport(w, firstPosition, transportID);
    TransportTube second = findTransport(w, secondPosition, transportID);
    if (first == null || second == null) {
      return new WorldUpdateResult(false);
    }
    // make sure that <2 connections per transport
    if (first.getNumberOfConnectedDevices() > 1 || second.getNumberOfConnectedDevices() > 1) {
      return new WorldUpdateResult(false);
    }
    // connect each to the other
    first.connect(second);
    second.connect(first);
    return new WorldUpdateResult(true);
  }

}
