package io.lp0onfire.ssi.model;

import java.util.HashMap;
import java.util.Map;

/**
 * A ControlledTransportEndpoint is a specialized TransportEndpoint
 * whose endpoints are bridged with an inventory controller.
 */
public abstract class ControlledTransportEndpoint extends TransportEndpoint {
  
  /**
   * Gets the manipulator index that corresponds with the given endpoint name.
   */
  public abstract Integer getManipulatorIndexOfEndpoint(String endpoint);
  
  /**
   * Gets the endpoint name that corresponds with the given manipulator index.
   * Must return NULL if the manipulator index doesn't correspond to an endpoint.
   */
  public abstract String getEndpointOfManipulatorIndex(Integer mIdx);
  
  /**
   * Returns true iff the given endpoint can receive items.
   */
  public abstract boolean endpointCanReceive(String endpoint);
  
  /**
   * Returns true iff the given endpoint can send items.
   */
  public abstract boolean endpointCanSend(String endpoint);
  
  enum EndpointCommand {
    CMD_NONE,
    CMD_RECV,
    CMD_SEND,
  }
  
  // tracks the current operation being performed by each endpoint
  protected Map<String, EndpointCommand> endpointState = new HashMap<>();
  
  // 1-object buffer for items received by endpoints
  protected Map<String, Item> endpointInputBuffer = new HashMap<>();
  // 1-object buffer for items to be sent by endpoints
  protected Map<String, Item> endpointOutputBuffer = new HashMap<>();
  
  public ControlledTransportEndpoint() {
    for (String endpoint : getTransportEndpoints()) {
      endpointState.put(endpoint, EndpointCommand.CMD_NONE);
    }
  }
  
  @Override
  public boolean receiveToEndpoint(String endpoint, Item item) {
    // check: endpoint capable of receiving items
    if (!endpointCanReceive(endpoint)) {
      return false;
    }
    // check: endpoint doesn't already have an item in the buffer
    if (endpointInputBuffer.get(endpoint) != null) {
      return false;
    }
    // successfully receive
    endpointInputBuffer.put(endpoint, item);
    if (endpointState.get(endpoint) == EndpointCommand.CMD_RECV) {
      endpointState.put(endpoint, EndpointCommand.CMD_NONE);
    }
    return true;
  }
  
  @Override
  public void preSendTimestep() {
    // check whether we've got any items to send
    for (String endpoint : getTransportEndpoints()) {
      Item i = endpointOutputBuffer.get(endpoint);
      if (i != null) {
        setEndpointOutput(endpoint, i);
      }
    }
  }
  
  @Override
  public void postSendTimestep(Map<String, Boolean> sendResults) {
    for (Map.Entry<String, Boolean> result : sendResults.entrySet()) {
      String endpoint = result.getKey();
      boolean success = result.getValue();
      if (success) {
        endpointOutputBuffer.remove(endpoint);
        endpointState.put(endpoint, EndpointCommand.CMD_NONE);
      } else {
        // TODO command failed, mark an error code
      }
    }
  }
  
  @Override
  public boolean canAcceptManipulatorCommand(int mIdx) {
    String endpoint = getEndpointOfManipulatorIndex(mIdx);
    if (endpoint == null) {
      return super.canAcceptManipulatorCommand(mIdx);
    } else {
      return endpointState.get(endpoint) == EndpointCommand.CMD_NONE;
    }
  }
  
  @Override
  public Item takeManipulatorItem(int mIdx) {
    String endpoint = getEndpointOfManipulatorIndex(mIdx);
    if (endpoint == null) {
      return super.takeManipulatorItem(mIdx);
    } else {
      Item i = endpointInputBuffer.get(endpoint);
      if (i == null) {
        return null;
      } else {
        endpointInputBuffer.remove(endpoint);
        endpointState.put(endpoint, EndpointCommand.CMD_NONE);
        return i;
      }
    }
  }
  
  @Override
  public boolean manipulator_getNextItem(int mIdx) {
    String endpoint = getEndpointOfManipulatorIndex(mIdx);
    if (endpoint == null) {
      return super.manipulator_getNextItem(mIdx);
    } else {
      if (!endpointCanReceive(endpoint)) {
        return false;
      }
      // if we're in the middle of a command, ignore this one
      if (endpointState.get(endpoint) != EndpointCommand.CMD_NONE) {
        return true;
      }
      // if we already have an item to receive, the command completes instantly
      if (endpointInputBuffer.get(endpoint) != null) {
        return true;
      }
      endpointState.put(endpoint, EndpointCommand.CMD_RECV);
      return true;
    }
  }
  
  @Override
  public boolean manipulator_putItem(int mIdx, Item item) {
    // TODO
    throw new UnsupportedOperationException("not yet implemented");
  }
  
}
