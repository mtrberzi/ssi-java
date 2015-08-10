package io.lp0onfire.ssi.testutils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.lp0onfire.ssi.model.Item;
import io.lp0onfire.ssi.model.TransportEndpoint;
import io.lp0onfire.ssi.model.Vector;

/**
 * A transport device with a single endpoint, "input",
 * that accepts any item it is asked to receive and keeps
 * a list of all items it has received so far.
 */
public class TestUtilSinkEndpoint extends TransportEndpoint {


  @Override
  public Vector getExtents() {
    return new Vector(1, 1, 1);
  }
  
  @Override
  public boolean hasWorldUpdates() {
    return false;
  }
  
  @Override
  public Set<String> getTransportEndpoints() {
    return new HashSet<>(Arrays.asList("input"));
  }

  private List<Item> receivedItems = new LinkedList<>();
  public List<Item> getReceivedItems() {
    return this.receivedItems;
  }
  
  @Override
  public boolean receiveToEndpoint(String endpoint, Item item) {
    receivedItems.add(item);
    return true;
  }

  @Override
  protected void preSendTimestep() {}

  @Override
  protected void postSendTimestep(Map<String, Boolean> sendResults) {}

}
