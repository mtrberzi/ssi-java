package io.lp0onfire.ssi.model.machines;

import static org.junit.Assert.*;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.BeforeClass;
import org.junit.Test;

import io.lp0onfire.ssi.model.ConnectTransportEndpointUpdate;
import io.lp0onfire.ssi.model.ConnectTransportTubeUpdate;
import io.lp0onfire.ssi.model.CreateTransportTubeUpdate;
import io.lp0onfire.ssi.model.Item;
import io.lp0onfire.ssi.model.Material;
import io.lp0onfire.ssi.model.MaterialBuilder;
import io.lp0onfire.ssi.model.TransportEndpoint;
import io.lp0onfire.ssi.model.TransportTube;
import io.lp0onfire.ssi.model.Vector;
import io.lp0onfire.ssi.model.World;
import io.lp0onfire.ssi.model.WorldUpdateResult;
import io.lp0onfire.ssi.model.items.Bar;
import io.lp0onfire.ssi.model.items.Ore;
import io.lp0onfire.ssi.testutils.TestUtilSinkEndpoint;
import io.lp0onfire.ssi.testutils.TestUtilSourceEndpoint;

public class TestSmelter {

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
  
  private static final int NUMBER_OF_TIMESTEPS_TO_SMELT = 10;
  private static final int NUMBER_OF_BARS_TO_PRODUCE = 4;
  private static Material testMaterial;
  
  @BeforeClass
  public static void setupClass() {
    MaterialBuilder builder = new MaterialBuilder();
    builder.setCanBeSmelted(true);
    builder.setNumberOfSmeltedBars(NUMBER_OF_BARS_TO_PRODUCE);
    builder.setSmeltingTimesteps(NUMBER_OF_TIMESTEPS_TO_SMELT);
    testMaterial = builder.build();
  }
  
  @Test
  public void testInitialConditions() {
    World w = new World(5, 10);
    Smelter s = new Smelter();
    assertTrue(w.addOccupant(new Vector(1, 0, 1), new Vector(0, 0, 0), s));
    assertEquals(Smelter.SmelterState.STATE_LOAD, s.getState());
    w.timestep();
    assertEquals(Smelter.SmelterState.STATE_LOAD, s.getState());
  }
  
  @Test
  public void testConnectInput() {
    World w = new World(5, 10);
    
    TestUtilSourceEndpoint source = new TestUtilSourceEndpoint();
    assertTrue(w.addOccupant(new Vector(0, 0, 1), new Vector(0, 0, 0), source));
    
    Smelter smelter = new Smelter();
    assertTrue(w.addOccupant(new Vector(1, 0, 1), new Vector(0, 0, 0), smelter));
    assertEquals(Smelter.SmelterState.STATE_LOAD, smelter.getState());
    
    createTransportTube(w, new Vector(0, 0, 1), "ore");
    createTransportTube(w, new Vector(1, 0, 1), "ore");
    connectTransportTubes(w, "ore", new Vector(0, 0, 1), new Vector(1, 0, 1));
    connectEndpoint(w, "ore", new Vector(0, 0, 1), source, "output");
    connectEndpoint(w, "ore", new Vector(1, 0, 1), smelter, "input");
  }
  
  @Test
  public void testInput_InvalidItem_Rejected() {
    World w = new World(5, 10);
    
    TestUtilSourceEndpoint source = new TestUtilSourceEndpoint();
    assertTrue(w.addOccupant(new Vector(0, 0, 1), new Vector(0, 0, 0), source));
    
    Smelter smelter = new Smelter();
    assertTrue(w.addOccupant(new Vector(1, 0, 1), new Vector(0, 0, 0), smelter));
    assertEquals(Smelter.SmelterState.STATE_LOAD, smelter.getState());
    
    createTransportTube(w, new Vector(0, 0, 1), "ore");
    createTransportTube(w, new Vector(1, 0, 1), "ore");
    connectTransportTubes(w, "ore", new Vector(0, 0, 1), new Vector(1, 0, 1));
    connectEndpoint(w, "ore", new Vector(0, 0, 1), source, "output");
    connectEndpoint(w, "ore", new Vector(1, 0, 1), smelter, "input");
    
    Item i = new Bar(testMaterial);
    source.queueSend(i);
    w.timestep();
    w.timestep();
    // the item should be in the tube right before the smelter
    List<TransportTube> tubes = w.getOccupants(new Vector(1, 0, 1)).stream()
        .filter((o -> o instanceof TransportTube)).map((o -> (TransportTube)o)).collect(Collectors.toList());
    assertEquals(1, tubes.size());
    TransportTube t1 = tubes.get(0);
    assertTrue(t1.getContents().contains(i));
    
    // now step again, the item should still be there
    w.timestep();
    assertTrue(t1.getContents().contains(i));
    // and we shouldn't have loaded anything
    assertEquals(Smelter.SmelterState.STATE_LOAD, smelter.getState());
  }
  
  @Test
  public void testInput_ValidItem_Accepted() {
    World w = new World(5, 10);
    
    TestUtilSourceEndpoint source = new TestUtilSourceEndpoint();
    assertTrue(w.addOccupant(new Vector(0, 0, 1), new Vector(0, 0, 0), source));
    
    Smelter smelter = new Smelter();
    assertTrue(w.addOccupant(new Vector(1, 0, 1), new Vector(0, 0, 0), smelter));
    assertEquals(Smelter.SmelterState.STATE_LOAD, smelter.getState());
    
    createTransportTube(w, new Vector(0, 0, 1), "ore");
    createTransportTube(w, new Vector(1, 0, 1), "ore");
    connectTransportTubes(w, "ore", new Vector(0, 0, 1), new Vector(1, 0, 1));
    connectEndpoint(w, "ore", new Vector(0, 0, 1), source, "output");
    connectEndpoint(w, "ore", new Vector(1, 0, 1), smelter, "input");
    
    Item i = new Ore(testMaterial);
    source.queueSend(i);
    w.timestep();
    w.timestep();
    // the item should be in the tube right before the smelter
    List<TransportTube> tubes = w.getOccupants(new Vector(1, 0, 1)).stream()
        .filter((o -> o instanceof TransportTube)).map((o -> (TransportTube)o)).collect(Collectors.toList());
    assertEquals(1, tubes.size());
    TransportTube t1 = tubes.get(0);
    assertTrue(t1.getContents().contains(i));
    
    // now step again, the item should be gone
    w.timestep();
    assertFalse(t1.getContents().contains(i));
    // and we should now be smelting the item
    assertEquals(Smelter.SmelterState.STATE_SMELT, smelter.getState());
    assertEquals(i, smelter.getCurrentOre());
  }
  
  @Test
  public void testInput_ValidItem_Smelted() {
    World w = new World(5, 10);
    
    TestUtilSourceEndpoint source = new TestUtilSourceEndpoint();
    assertTrue(w.addOccupant(new Vector(0, 0, 1), new Vector(0, 0, 0), source));
    
    Smelter smelter = new Smelter();
    assertTrue(w.addOccupant(new Vector(1, 0, 1), new Vector(0, 0, 0), smelter));
    assertEquals(Smelter.SmelterState.STATE_LOAD, smelter.getState());
    
    createTransportTube(w, new Vector(0, 0, 1), "ore");
    createTransportTube(w, new Vector(1, 0, 1), "ore");
    connectTransportTubes(w, "ore", new Vector(0, 0, 1), new Vector(1, 0, 1));
    connectEndpoint(w, "ore", new Vector(0, 0, 1), source, "output");
    connectEndpoint(w, "ore", new Vector(1, 0, 1), smelter, "input");
    
    Item i = new Ore(testMaterial);
    source.queueSend(i);
    w.timestep();
    w.timestep();
    w.timestep();
    assertEquals(Smelter.SmelterState.STATE_SMELT, smelter.getState());
    // now step until we are done smelting
    for (int n = 0; n < NUMBER_OF_TIMESTEPS_TO_SMELT; ++n) {
      w.timestep();
    }
    assertEquals(Smelter.SmelterState.STATE_OUTPUT, smelter.getState());
  }
  
  @Test
  public void testOutput_Correct() {
    World w = new World(5, 10);
    
    TestUtilSourceEndpoint source = new TestUtilSourceEndpoint();
    assertTrue(w.addOccupant(new Vector(0, 0, 1), new Vector(0, 0, 0), source));
    
    Smelter smelter = new Smelter();
    assertTrue(w.addOccupant(new Vector(1, 0, 1), new Vector(0, 0, 0), smelter));
    assertEquals(Smelter.SmelterState.STATE_LOAD, smelter.getState());
    
    createTransportTube(w, new Vector(0, 0, 1), "ore");
    createTransportTube(w, new Vector(1, 0, 1), "ore");
    connectTransportTubes(w, "ore", new Vector(0, 0, 1), new Vector(1, 0, 1));
    connectEndpoint(w, "ore", new Vector(0, 0, 1), source, "output");
    connectEndpoint(w, "ore", new Vector(1, 0, 1), smelter, "input");
    
    TestUtilSinkEndpoint sink = new TestUtilSinkEndpoint();
    assertTrue(w.addOccupant(new Vector(2, 0, 1), new Vector(0, 0, 0), sink));
    
    createTransportTube(w, new Vector(1, 0, 1), "bar");
    createTransportTube(w, new Vector(2, 0, 1), "bar");
    connectTransportTubes(w, "bar", new Vector(1, 0, 1), new Vector(2, 0, 1));
    connectEndpoint(w, "bar", new Vector(1, 0, 1), smelter, "output");
    connectEndpoint(w, "bar", new Vector(2, 0, 1), sink, "input");
    
    Item i = new Ore(testMaterial);
    source.queueSend(i);
    w.timestep();
    w.timestep();
    w.timestep();
    assertEquals(Smelter.SmelterState.STATE_SMELT, smelter.getState());
    // now step until we are done smelting
    for (int n = 0; n < NUMBER_OF_TIMESTEPS_TO_SMELT; ++n) {
      w.timestep();
    }
    assertEquals(Smelter.SmelterState.STATE_OUTPUT, smelter.getState());
    // each bar should take 3 timesteps to arrive at the sink
    for (int n = 0; n < NUMBER_OF_BARS_TO_PRODUCE * 3; ++n) {
      w.timestep();
    }
    // check that we received the correct number of items
    // check that each item is a bar made of our test material
    assertEquals(NUMBER_OF_BARS_TO_PRODUCE, sink.getReceivedItems().size());
    for (Item x : sink.getReceivedItems()) {
      assertTrue(x instanceof Bar);
      Bar b = (Bar)x;
      assertEquals(testMaterial, b.getMaterial());
    }
  }
  
}
