package io.lp0onfire.ssi.model;

public class ConnectTransportEndpointUpdate extends WorldUpdate {

  private final String transportID;
  private final Vector position;
  private final TransportEndpoint transportEndpoint;
  private final String endpoint;
  
  public ConnectTransportEndpointUpdate(String transportID, Vector position, TransportEndpoint transportEndpoint, String endpoint) {
    this.transportID = transportID;
    this.position = position;
    this.transportEndpoint = transportEndpoint;
    this.endpoint = endpoint;
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
    // make sure there is a transport tube with the correct ID in this position
    TransportTube transport = findTransport(w, position, transportID);
    if (transport == null) {
      return new WorldUpdateResult(false);
    }
    // make sure that endpoint exists on the transport endpoint and is not connected
    if (!transportEndpoint.getTransportEndpoints().contains(endpoint)) {
      return new WorldUpdateResult(false);
    }
    if (transportEndpoint.isConnected(endpoint)) {
      return new WorldUpdateResult(false);
    }
    // make sure that the transport tube has at most one connection
    if (transport.getNumberOfConnectedDevices() > 1) {
      return new WorldUpdateResult(false);
    }
    // connect both devices
    transport.connect(transportEndpoint);
    transportEndpoint.connect(endpoint, transport);
    return new WorldUpdateResult(true);
  }
  
}
