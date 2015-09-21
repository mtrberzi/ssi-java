package io.lp0onfire.ssi.model.machines;

import io.lp0onfire.ssi.model.Item;
import io.lp0onfire.ssi.model.TransportEndpoint;
import io.lp0onfire.ssi.model.Vector;
import io.lp0onfire.ssi.model.items.Component;
import io.lp0onfire.ssi.model.reactions.Reaction;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * A Rapid Fabricator is a simple machine that can create basic constructions,
 * including machine frames, machine components, and transport tubes,
 * out of simple components.
 * It has a limited degree of automation and can only hold a few items to work on at a time.
 * 
 * Endpoints: input, output
 */
public class RapidFabricator extends TransportEndpoint {

  private static final int MAXIMUM_INPUT_CAPACITY = 5;
  
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
  
  public enum FabricatorState {
    STATE_REACTION_NOT_SET, // reaction not set by user
    STATE_LOAD, // waiting to receive input items
    STATE_FAB, // fabricating products
    STATE_OUTPUT, // waiting to output products
  }
  
  private FabricatorState state;
  public FabricatorState getState() {
    return this.state;
  }
  
  private List<Item> currentItems = new LinkedList<Item>();
  public List<Item> getCurrentItems() {
    return this.currentItems;
  }
  
  private Reaction currentReaction = null;
  public Reaction getReaction() {
    return this.currentReaction;
  }
  public void setReaction(Reaction reaction) {
    // this can only be done in the "load" state when there are no items loaded,
    // or in the "reaction not set" state
    if (state != FabricatorState.STATE_REACTION_NOT_SET) {
      if ( (state == FabricatorState.STATE_LOAD && !currentItems.isEmpty())
          || state != FabricatorState.STATE_LOAD) {
        return;
      }
    }
    
    if (reaction == null) {
      this.currentReaction = reaction;
      state = FabricatorState.STATE_REACTION_NOT_SET;
      return;
    }
    
    // must be a "fabricator-1" reaction
    if (!reaction.getCategories().contains("fabricator-1")) {
      return;
    }
    this.currentReaction = reaction;
    state = FabricatorState.STATE_LOAD;
  }
  
  private int fabTimeRemaining = 0;
  public int getFabTimeRemaining() {
    return this.fabTimeRemaining;
  }
  
  private Queue<Item> outputQueue = new LinkedList<>();
  
  public RapidFabricator() {
    this.state = FabricatorState.STATE_REACTION_NOT_SET;
  }
  
  @Override
  public boolean receiveToEndpoint(String endpoint, Item item) {
    if (!(endpoint.equals("input"))) {
      return false;
    }
    // we can only receive if we're ready to load
    if (this.state != FabricatorState.STATE_LOAD) {
      return false;
    }
    // we can only receive components
    if (!(item instanceof Component)) {
      return false;
    }
    // we have a maximum input capacity that we cannot exceed
    if (currentItems.size() >= MAXIMUM_INPUT_CAPACITY) {
      return false;
    }
    
    currentItems.add(item);
    // see if we can perform the reaction yet
    if (currentReaction.reactantsOK(currentItems)) {
      // start fabrication!
      state = FabricatorState.STATE_FAB;
      fabTimeRemaining = currentReaction.getTime();
    }
    
    return true;
  }
  
  @Override
  protected void preSendTimestep() {
    switch (this.state) {
    case STATE_REACTION_NOT_SET:
    case STATE_LOAD:
      // don't do anything
      break;
    case STATE_FAB:
    {
      // fab one step
      this.fabTimeRemaining -= 1;
      // if we just finished...
      if (this.fabTimeRemaining <= 0) {
        // produce outputs and queue them up
        Reaction.Result result = currentReaction.react(currentItems);
        if (!result.successful()) {
          throw new IllegalStateException("fabrication unexpectedly failed");
        }
        List<Item> consumedReactants = result.getConsumedReactants();
        currentItems.removeAll(consumedReactants);
        
        List<Item> products = result.getOutputProducts();
        for (Item i : products) {
          outputQueue.add(i);
        }
        
        this.state = FabricatorState.STATE_OUTPUT;
      }
    }
      break;
    case STATE_OUTPUT:
    {
      if (outputQueue.isEmpty()) {
        // not sure how we got here, but in order to avoid locking up
        this.state = FabricatorState.STATE_LOAD;
      } else {
        setEndpointOutput("output", outputQueue.peek());
      }
    }
      break;
    default:
      throw new IllegalStateException("unhandled case logic for rapid fabricator state " + this.state.toString());
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
          this.state = FabricatorState.STATE_LOAD;
        }
      }
    }
  }

  @Override
  public int getType() {
    return 2;
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
