package io.lp0onfire.ssi.model;

public interface TransportDevice {

  void disconnect(TransportDevice connected);
  
  /**
   * Asks this transport device to receive an item from a connected neighbour.
   * @param neighbour the transport device the item is being received from
   * @param item the object to receive
   * @return true if the item was successfully received, or false if unsuccessful
   * (e.g. no space left)
   */
  boolean receive(TransportDevice neighbour, Item item);
  
}
