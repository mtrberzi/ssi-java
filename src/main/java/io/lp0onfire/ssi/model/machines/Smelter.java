package io.lp0onfire.ssi.model.machines;

import io.lp0onfire.ssi.model.Item;
import io.lp0onfire.ssi.model.TransportEndpoint;
import io.lp0onfire.ssi.model.Vector;
import io.lp0onfire.ssi.model.items.Bar;
import io.lp0onfire.ssi.model.items.Ore;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * A Smelter is a machine that refines smeltable ore into bars of material
 * that can then be used to produce other items.
 * 
 * Endpoints: input, output
 */
public class Smelter extends TransportEndpoint {

  @Override
  public Set<String> getTransportEndpoints() {
    return new HashSet<>(Arrays.asList("input", "output"));
  }

  @Override
  public Vector getExtents() {
    return new Vector(1, 1, 1);
  }

  @Override
  public boolean hasWorldUpdates() {
    return false;
  }
  
  public enum SmelterState {
    STATE_LOAD, // waiting to receive ore
    STATE_SMELT, // smelting ore
    STATE_OUTPUT, // waiting to output produced bars
  }
  
  private SmelterState state;
  public SmelterState getState() {
    return this.state;
  }
  
  private Ore currentOre = null;
  public Ore getCurrentOre() {
    return this.currentOre;
  }
  
  private int smeltingTimeRemaining = 0;
  public int getSmeltingTimeRemaining() {
    return this.smeltingTimeRemaining;
  }
  
  private Queue<Bar> outputQueue = new LinkedList<>();
  
  public Smelter() {
    this.state = SmelterState.STATE_LOAD;
  }
  
  @Override
  public boolean receiveToEndpoint(String endpoint, Item item) {
    if (endpoint != "input") {
      return false;
    }
    // we can only receive if we're ready to load
    if (this.state != SmelterState.STATE_LOAD) {
      return false;
    }
    // we can only receive ore
    if (!(item instanceof Ore)) {
      return false;
    }
    Ore ore = (Ore)item;
    // the ore has to be something we can smelt
    if (!ore.getMaterial().getCanBeSmelted()) {
      return false;
    }
    // receive input and start smelting
    currentOre = ore;
    this.state = SmelterState.STATE_SMELT;
    this.smeltingTimeRemaining = ore.getMaterial().getSmeltingTimesteps();
    return true;
  }

  @Override
  protected void preSendTimestep() {
    switch (this.state) {
    case STATE_LOAD:
      // don't do anything
      break;
    case STATE_SMELT:
    {
      // smelt one step
      this.smeltingTimeRemaining -= 1;
      // if we just finished...
      if (this.smeltingTimeRemaining <= 0) {
        // produce bars and queue them for output
        for (int i = 0; i < this.currentOre.getMaterial().getNumberOfSmeltedBars(); ++i) {
          outputQueue.add(new Bar(this.currentOre.getMaterial()));
        }
        this.currentOre = null;
        this.state = SmelterState.STATE_OUTPUT;
      }
    }
      break;
    case STATE_OUTPUT:
    {
      if (outputQueue.isEmpty()) {
        // not sure how we got here, but in order to avoid locking up
        this.state = SmelterState.STATE_LOAD;
      } else {
        setEndpointOutput("output", outputQueue.peek());
      }
    }
      break;
    default:
      throw new IllegalStateException("unhandled case logic for smelter state " + this.state.toString());
    }
  }

  @Override
  protected void postSendTimestep(Map<String, Boolean> sendResults) {
    for (Boolean result : sendResults.values()) {
      // if we successfully sent, remove an item from the queue
      if (result) {
        outputQueue.poll();
        // if the queue became empty because of this, we can load again
        if (outputQueue.isEmpty()) {
          this.state = SmelterState.STATE_LOAD;
        }
      }
    }
  }

}
