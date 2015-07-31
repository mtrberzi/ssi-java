package io.lp0onfire.ssi.microcontroller.peripherals;

import java.util.concurrent.LinkedBlockingQueue;

import io.lp0onfire.ssi.microcontroller.peripherals.SerialTransceiver;
import io.lp0onfire.ssi.microcontroller.peripherals.SerialTransceiver.Packet;

public class SerialCable {

  private SerialTransceiver endA = null;
  private SerialTransceiver endB = null;
  
  // packets transmitted by A
  private LinkedBlockingQueue<Packet> sendQueueA = new LinkedBlockingQueue<>();
  // packets received by B
  private LinkedBlockingQueue<Packet> recvQueueB = new LinkedBlockingQueue<>();
  
  // packets transmitted by B
  private LinkedBlockingQueue<Packet> sendQueueB = new LinkedBlockingQueue<>();
  // packets received by A
  private LinkedBlockingQueue<Packet> recvQueueA = new LinkedBlockingQueue<>();
  
  public void connect(SerialTransceiver xcvr) {
    if (endA == null) {
      endA = xcvr;
      xcvr.setCable(this);
    } else if (endB == null) {
      endB = xcvr;
      xcvr.setCable(this);
    } else {
      throw new IllegalStateException("cannot connect more than two transceivers to one cable");
    }
  }
  
  public boolean hasCarrier() {
    // TOOD check for power
    return (endA != null && endB != null);
  }
  
  public void sendPacket(SerialTransceiver tx, Packet p) {
    if (!hasCarrier()) return;
    if (tx == endA) {
      sendQueueA.offer(p);
    } else if (tx == endB) {
      sendQueueB.offer(p);
    } else {
      throw new IllegalArgumentException("attempt to send from a serial transceiver not connected to this cable");
    }
  }
  
  public Packet recvPacket(SerialTransceiver rx) {
    if (!hasCarrier()) return null;
    if (rx == endA) {
      return recvQueueA.poll();
    } else if (rx == endB) {
      return recvQueueB.poll();
    } else {
      throw new IllegalArgumentException("attempt to receive to a serial transceiver not connected to this cable");
    }
  }
 
  // called exactly once per timestep by each connected transceiver
  public void sync(SerialTransceiver xcvr) {
    if (xcvr == endA) {
      sendQueueA.drainTo(recvQueueB);
    } else if (xcvr == endB) {
      sendQueueB.drainTo(recvQueueA);
    } else {
      throw new IllegalArgumentException("attempt to sync by a serial transceiver not connected to this cable");
    }
  }
  
}
