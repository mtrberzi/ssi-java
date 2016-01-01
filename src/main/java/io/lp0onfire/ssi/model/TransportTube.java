package io.lp0onfire.ssi.model;

import java.util.HashSet;
import java.util.Set;

public class TransportTube extends VoxelOccupant implements TransportDevice {
  
  private String transportID = "";
  public String getTransportID() {
    return this.transportID;
  }
  public void setTransportID(String txID) {
    this.transportID = txID;
  }
  
  public TransportTube(String transportID) {
    this.transportID = transportID;
  }
  
  public short getKind() {
    return (short)6;
  }
  
  public int getType() {
    // TODO what do we even put in this field?
    return 0;
  }
  
  private TransportDevice connectionA = null;
  public TransportDevice getConnectionA() {
    return this.connectionA;
  }
  private TransportDevice connectionB = null;
  public TransportDevice getConnectionB() {
    return this.connectionB;
  }
  
  public int getNumberOfConnectedDevices() {
    int n = 0;
    if (connectionA != null) n += 1;
    if (connectionB != null) n += 1;
    return n;
  }
  
  public void connect(TransportDevice other) {
    if (connectionA == null) {
      connectionA = other;
    } else if (connectionB == null) {
      connectionB = other;
    } else {
      throw new IllegalStateException("transport tube cannot be connected to more than two devices");
    }
  }
  
  @Override
  public void disconnect(TransportDevice other) {
    if (connectionA == other) {
      connectionA = null;
    } else if (connectionB == other) {
      connectionB = null;
    } else {
      throw new IllegalStateException("attempt to disconnect a non-connected device");
    }
  }
  
  private Item outgoingToB = null;
  public Item getOutgoingToB() {
    return this.outgoingToB;
  }
  
  private Item outgoingToA = null;
  public Item getOutgoingToA() {
    return this.outgoingToA;
  }
  
  public Set<Item> getContents() {
    Set<Item> contents = new HashSet<>();
    if (outgoingToA != null) {
      contents.add(outgoingToA);
    }
    if (outgoingToB != null) {
      contents.add(outgoingToB);
    }
    return contents;
  }
  
  @Override
  public boolean receive(TransportDevice neighbour, Item item) {
    if (connectionA != null && neighbour == connectionA) {
      // first try to send outgoingToB to connectionB
      if (connectionB == null) {
        if (item == null) {
          // absorb the empty slot
          return true;
        } else {
          if (outgoingToB == null) {
            // can't send, but we can receive it for now
            outgoingToB = item;
            return true;
          } else {
            // cannot replace existing outgoing item
            return false;
          }
        }
      } else {
        // first try to send whatever we have to B
        boolean sendResult = connectionB.receive(this, outgoingToB);
        if (sendResult) {
          // now our slot is empty so we can always receive
          outgoingToB = item;
          return true;
        } else {
          if (item == null) {
            // absorb the empty slot
            return true;
          } else {
            if (outgoingToB == null) {
              // can't send, but we can receive it for now
              outgoingToB = item;
              return true;
            } else {
              // cannot replace existing outgoing item
              return false;
            }
          }
        }
      }
    } else if (connectionB != null && neighbour == connectionB) {
      // first try to send outgoingToA to connectionA
      if (connectionA == null) {
        if (item == null) {
          // absorb the empty slot
          return true;
        } else {
          if (outgoingToA == null) {
            // can't send, but we can receive it for now
            outgoingToA = item;
            return true;
          } else {
            // cannot replace existing outgoing item
            return false;
          }
        }
      } else {
        // first try to send whatever we have to A
        boolean sendResult = connectionA.receive(this, outgoingToA);
        if (sendResult) {
          // now our slot is empty so we can always receive
          outgoingToA = item;
          return true;
        } else {
          if (item == null) {
            // absorb the empty slot
            return true;
          } else {
            if (outgoingToA == null) {
              // can't send, but we can receive it for now
              outgoingToA = item;
              return true;
            } else {
              // cannot replace existing outgoing item
              return false;
            }
          }
        }
      }
    } else {
      throw new IllegalArgumentException("attempt to receive from an unconnected neighbour");
    }
  }
  
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
    return false;
  }

  @Override
  public boolean canMove() {
    return false;
  }

  @Override
  public Vector getExtents() {
    return new Vector(1, 1, 1);
  }

  /**
   * Transport tubes are handled specially by the World, so do not receive timesteps.
   */
  @Override
  public boolean requiresTimestep() {
    return false;
  }

  @Override
  public boolean hasWorldUpdates() {
    return false;
  }

}
