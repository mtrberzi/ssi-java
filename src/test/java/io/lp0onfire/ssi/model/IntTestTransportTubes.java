package io.lp0onfire.ssi.model;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

/**
 * Test suite for functionality related to the transport tube network.
 */
public class IntTestTransportTubes {

  class TestEndpoint extends TransportEndpoint {

    @Override
    public Set<String> getTransportEndpoints() {
      return new HashSet<>(Arrays.asList("input", "output"));
    }

    private List<Item> itemsReceived = new LinkedList<>();
    public List<Item> getItemsReceived() {
      return this.itemsReceived;
    }
    
    @Override
    public boolean receiveToEndpoint(String endpoint, Item item) {
      if (endpoint.equals("input")) {
        itemsReceived.add(item);
        return true;
      } else {
        return false;
      }
    }

    private Queue<Item> sendQueue = new LinkedList<>();
    public Queue<Item> getSendQueue() {
      return this.sendQueue;
    }
    public void queueOutput(Item item) {
      sendQueue.add(item);
    }
    
    @Override
    protected void preSendTimestep() {
      if (!sendQueue.isEmpty()) {
        Item i = sendQueue.peek();
        setEndpointOutput("output", i);
      }
    }

    @Override
    protected void postSendTimestep(Map<String, Boolean> sendResults) {
      if (sendResults.containsKey("output")) {
        boolean res = sendResults.get("output");
        if (res) {
          // remove the item that was sent
          sendQueue.poll();
        }
      }
    }

    @Override
    public Vector getExtents() {
      return new Vector(1, 1, 1);
    }

    @Override
    public boolean hasWorldUpdates() {
      return false;
    }
    
  }
  
  class TestItem extends Item {
    public TestItem() {
      super(MaterialLibrary.getInstance().getMaterial("bedrock"));
    }
  }
  
  private void createTransportTube(World w, Vector position, String transportID) {
    CreateTransportTubeUpdate u = new CreateTransportTubeUpdate(position, transportID);
    WorldUpdateResult result = u.apply(w);
    assertTrue(result.wasSuccessful());
  }
  
  private void connectTransportTubes(World w, String transportID, Vector pos1, Vector pos2) {
    ConnectTransportTubeUpdate u = new ConnectTransportTubeUpdate(transportID, pos1, pos2);
    WorldUpdateResult result = u.apply(w);
    assertTrue(result.wasSuccessful());
  }
  
  private void connectEndpoint(World w, String transportID, Vector pos, TransportEndpoint ept, String eptName) {
    ConnectTransportEndpointUpdate u = new ConnectTransportEndpointUpdate(transportID, pos, ept, eptName);
    WorldUpdateResult result = u.apply(w);
    assertTrue(result.wasSuccessful());
  }
  
  @Test
  public void testUnconnectedEndpoint_CannotSend() {
    World w = new World(5, 10);
    TestEndpoint ept1 = new TestEndpoint();
    assertTrue(w.addOccupant(new Vector(0, 0, 1), new Vector(0, 0, 0), ept1));
    ept1.queueOutput(new TestItem());
    w.timestep();
    assertFalse(ept1.sendQueue.isEmpty());
  }
  
  @Test
  public void testUnconnectedEndpoint_CannotReceive() {
    World w = new World(5, 10);
    TestEndpoint ept1 = new TestEndpoint();
    assertTrue(w.addOccupant(new Vector(0, 0, 1), new Vector(0, 0, 0), ept1));
    w.timestep();
    assertTrue(ept1.itemsReceived.isEmpty());
  }
  
  @Test
  public void testCreateTransport() {
    World w = new World(5, 10);
    createTransportTube(w, new Vector(0, 0, 1), "a");
    assertTrue(w.getOccupants(new Vector(0, 0, 1)).stream()
        .filter((o -> o instanceof TransportTube)).map((o -> (TransportTube)o))
        .anyMatch((o -> ((TransportTube)o).getTransportID().equals("a"))));
  }
  
  @Test
  public void testConnectTransport() {
    World w = new World(5, 10);
    createTransportTube(w, new Vector(0, 0, 1), "a");
    createTransportTube(w, new Vector(1, 0, 1), "a");
    connectTransportTubes(w, "a", new Vector(0, 0, 1), new Vector(1, 0, 1));
    List<TransportTube> tubes1 = w.getOccupants(new Vector(0, 0, 1)).stream()
        .filter((o -> o instanceof TransportTube)).map((o -> (TransportTube)o)).collect(Collectors.toList());
    List<TransportTube> tubes2 = w.getOccupants(new Vector(1, 0, 1)).stream()
        .filter((o -> o instanceof TransportTube)).map((o -> (TransportTube)o)).collect(Collectors.toList());
    assertEquals(1, tubes1.size());
    assertEquals(1, tubes2.size());
    
    TransportTube t1 = tubes1.get(0);
    TransportTube t2 = tubes2.get(0);
    assertEquals(1, t1.getNumberOfConnectedDevices());
    assertEquals(1, t2.getNumberOfConnectedDevices());
    assertTrue(t1.getConnectionA() == t2 || t1.getConnectionB() == t2);
    assertTrue(t2.getConnectionA() == t1 || t2.getConnectionB() == t1);
  }
  
  @Test
  public void testConnectEndpoint() {
    World w = new World(5, 10);
    TestEndpoint ept1 = new TestEndpoint();
    assertTrue(w.addOccupant(new Vector(0, 0, 1), new Vector(0, 0, 0), ept1));
    createTransportTube(w, new Vector(0, 0, 1), "a");
    connectEndpoint(w, "a", new Vector(0, 0, 1), ept1, "output");
    
    List<TransportTube> tubes = w.getOccupants(new Vector(0, 0, 1)).stream()
        .filter((o -> o instanceof TransportTube)).map((o -> (TransportTube)o)).collect(Collectors.toList());
    assertEquals(1, tubes.size());
    TransportTube t1 = tubes.get(0);
    assertEquals(1, t1.getNumberOfConnectedDevices());
    assertTrue(t1.getConnectionA() == ept1 || t1.getConnectionB() == ept1);
    
    assertEquals(t1, ept1.getConnectedTransport("output"));
  }
  
  @Test
  public void testSendHalfDuplex() {
    World w = new World(5, 10);
    TestEndpoint ept1 = new TestEndpoint();
    TestEndpoint ept2 = new TestEndpoint();
    assertTrue(w.addOccupant(new Vector(0, 0, 1), new Vector(0, 0, 0), ept1));
    assertTrue(w.addOccupant(new Vector(1, 0, 1), new Vector(0, 0, 0), ept2));
    
    createTransportTube(w, new Vector(0, 0, 1), "a");
    createTransportTube(w, new Vector(1, 0, 1), "a");
    connectTransportTubes(w, "a", new Vector(0, 0, 1), new Vector(1, 0, 1));
    connectEndpoint(w, "a", new Vector(0, 0, 1), ept1, "output");
    connectEndpoint(w, "a", new Vector(1, 0, 1), ept2, "input");

    List<TransportTube> tubes1 = w.getOccupants(new Vector(0, 0, 1)).stream()
        .filter((o -> o instanceof TransportTube)).map((o -> (TransportTube)o)).collect(Collectors.toList());
    assertEquals(1, tubes1.size());
    TransportTube t1 = tubes1.get(0);
    
    List<TransportTube> tubes2 = w.getOccupants(new Vector(1, 0, 1)).stream()
        .filter((o -> o instanceof TransportTube)).map((o -> (TransportTube)o)).collect(Collectors.toList());
    assertEquals(1, tubes2.size());
    TransportTube t2 = tubes2.get(0);
    
    // now send an item from ept1 to ept2
    TestItem i = new TestItem();
    ept1.queueOutput(i);
    
    w.timestep();
    assertFalse(ept1.getSendQueue().contains(i));
    // check transport t1
    assertTrue(t1.getContents().contains(i));
    assertTrue(t2.getContents().isEmpty());
    assertEquals("ept2 received unexpected item(s): " + ept2.getItemsReceived().toString(),
        0, ept2.getItemsReceived().size());
    
    w.timestep();
    // check transport t2
    assertTrue(t1.getContents().isEmpty());
    assertTrue(t2.getContents().contains(i));
    assertEquals(0, ept2.getItemsReceived().size());
    
    w.timestep();
    // item should arrive at ept2
    assertTrue(t2.getContents().isEmpty());
    assertEquals(1, ept2.getItemsReceived().size());
    assertTrue(ept2.getItemsReceived().contains(i));
  }
  
}
