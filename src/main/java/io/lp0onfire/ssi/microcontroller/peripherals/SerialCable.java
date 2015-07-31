package io.lp0onfire.ssi.microcontroller.peripherals;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
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
  
  private CyclicBarrier syncBarrier;
  
  public SerialCable() {
    this.syncBarrier = new CyclicBarrier(2, new SyncAction());
  }
  
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
 
  
  public void sync() {
    if (!hasCarrier()) return;
    try {
      syncBarrier.await();
    } catch (InterruptedException | BrokenBarrierException e) {
      e.printStackTrace();
    }
  }
  
  class SyncAction implements Runnable {

    @Override
    public void run() {
      sendQueueA.drainTo(recvQueueB);
      sendQueueB.drainTo(recvQueueA);
    }
    
  }
  
}
