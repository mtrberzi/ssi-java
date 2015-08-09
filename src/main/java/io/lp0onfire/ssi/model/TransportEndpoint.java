package io.lp0onfire.ssi.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A TransportEndpoint is a kind of machine that connects to transport tubes.
 * It is always stationary.
 */
public abstract class TransportEndpoint extends Machine implements TransportDevice {

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
  public boolean needsSupport() {
    return true;
  }

  @Override
  public boolean canMove() {
    return false;
  }

  private Map<String, TransportTube> connectedTransports = new HashMap<>();
  public boolean isConnected(String endpoint) {
    if (!getTransportEndpoints().contains(endpoint)) return false;
    return connectedTransports.containsKey(endpoint);
  }
  
  public TransportTube getConnectedTransport(String endpoint) {
    if (!getTransportEndpoints().contains(endpoint)) return null;
    return connectedTransports.get(endpoint);
  }
  
  public String getEndpoint(TransportTube transport) {
    for (Map.Entry<String, TransportTube> entry : connectedTransports.entrySet()) {
      if (entry.getValue() == transport) {
        return entry.getKey();
      }
    }
    // no endpoint found
    throw new IllegalArgumentException("transport not connected to any endpoint");
  }
  
  public boolean connect(String endpoint, TransportTube transport) {
    if (!getTransportEndpoints().contains(endpoint)) {
      return false;
    }
    if (connectedTransports.containsKey(endpoint)) {
      return false;
    }
    if (connectedTransports.containsValue(transport)) {
      return false;
    }
    connectedTransports.put(endpoint, transport);
    return true;
  }
  
  @Override
  public void disconnect(TransportDevice connected) {
    if (!(connected instanceof TransportTube)) return;
    TransportTube transport = (TransportTube)connected;
    if (!connectedTransports.containsValue(transport)) return;
    for (Map.Entry<String, TransportTube> entry : connectedTransports.entrySet()) {
      if (entry.getValue() == transport) {
        connectedTransports.remove(entry.getKey());
        break;
      }
    }
  }
  
  public boolean receive(TransportDevice neighbour, Item item) {
    if (neighbour instanceof TransportTube) {
      // if the item is null (empty slot), absorb it
      if (item == null) {
        return true;
      } else {
        String endpoint = getEndpoint((TransportTube)neighbour);
        return receiveToEndpoint(endpoint, item);
      }
    } else {
      throw new IllegalArgumentException("cannot receive from non-tube transport");
    }
  }
  
  protected boolean send(String endpoint, Item item) {
    TransportTube tx = getConnectedTransport(endpoint);
    if (tx == null) return false;
    return tx.receive(this, item);
  }
  
  @Override
  public void timestep() {
    preSendTimestep();
    // now endpointOutputs is populated...
    Map<String, Boolean> sendResults = new HashMap<>();
    for (String endpoint : getTransportEndpoints()) {
      if (endpointOutputs.containsKey(endpoint)) {
        boolean couldSend = send(endpoint, endpointOutputs.get(endpoint));
        sendResults.put(endpoint, couldSend);
      } else {
        // push an empty slot to advance the pipeline
        send(endpoint, null);
      }
    }
    endpointOutputs.clear();
    postSendTimestep(sendResults);
  }
  
  private Map<String, Item> endpointOutputs = new HashMap<>();
  protected Map<String, Item> getEndpointOutputs() {
    return this.endpointOutputs;
  }
  protected void setEndpointOutput(String endpoint, Item item) {
    endpointOutputs.put(endpoint, item);
  }
  
  // returns a list of the names of all transport endpoints supported
  public abstract Set<String> getTransportEndpoints();
  /**
   *  Receives an item inbound to a given endpoint
   *  @returns true iff the item could be successfully received
   */
  public abstract boolean receiveToEndpoint(String endpoint, Item item);
  protected abstract void preSendTimestep();
  protected abstract void postSendTimestep(Map<String, Boolean> sendResults);
  
}
