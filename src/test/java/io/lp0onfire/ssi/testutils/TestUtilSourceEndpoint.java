package io.lp0onfire.ssi.testutils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import io.lp0onfire.ssi.model.Item;
import io.lp0onfire.ssi.model.TransportEndpoint;
import io.lp0onfire.ssi.model.Vector;

/**
 * A transport device with a single endpoint, "output",
 * that attempts to send an item from its send queue periodically.
 */
public class TestUtilSourceEndpoint extends TransportEndpoint {

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
    return new HashSet<>(Arrays.asList("output"));
  }

  @Override
  public boolean receiveToEndpoint(String endpoint, Item item) {
    // cannot receive
    return false;
  }

  private Queue<Item> sendQueue = new LinkedList<>();
  public Queue<Item> getSendQueue() {
    return this.sendQueue;
  }
  public void queueSend(Item i) {
    sendQueue.add(i);
  }
  
  int sendPeriod = 1;
  int sendCounter = 0;
  public int getSendPeriod() {
    return this.sendPeriod;
  }
  public void setSendPeriod(int p) {
    this.sendPeriod = p;
  }
  public void resetSendCounter() {
    this.sendCounter = 0;
  }
  
  @Override
  protected void preSendTimestep() {
    this.sendCounter += 1;
    if (this.sendCounter == this.sendPeriod) {
      this.sendCounter = 0;
      if (!sendQueue.isEmpty()) {
        this.setEndpointOutput("output", sendQueue.peek());
      }
    }
  }

  @Override
  protected void postSendTimestep(Map<String, Boolean> sendResults) {
    for (Boolean result : sendResults.values()) {
      if (result) {
        sendQueue.poll();
      }
    }
  }
  
  @Override
  public int getType() {
    return 0;
  }

  @Override
  public int getNumberOfManipulators() {
    return 0;
  }

  @Override
  public ManipulatorType getManipulatorType(int mIdx) {
    return null;
  }

}
