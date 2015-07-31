package io.lp0onfire.ssi.microcontroller.peripherals;

import java.util.LinkedList;
import java.util.Queue;

import io.lp0onfire.ssi.TimeConstants;
import io.lp0onfire.ssi.microcontroller.AddressTrapException;
import io.lp0onfire.ssi.microcontroller.InterruptSource;
import io.lp0onfire.ssi.microcontroller.SystemBusPeripheral;

public class SerialTransceiver implements SystemBusPeripheral, InterruptSource {
  
  // tracks how often we attempt to send and receive packets
  private long transceiverPeriod = 0x00000000FFFFFFFFL;
  private long transceiverPollingCount = 0L;
  
  class Packet {
    private final int data;
    public int getData() {
      return this.data;
    }
    
    public Packet(int data) {
      this.data = data & 0x000000FF; // truncate to 8 bits
    }
  }
  
  private static final Integer BUFFER_CAPACITY = 4096;
  
  private Queue<Packet> transmitBuffer = new LinkedList<>();
  private int transmitBufferCapacity = 0;
  public int getTransmitBufferCapacity() {
    return this.transmitBufferCapacity;
  }
  private Queue<Packet> receiveBuffer = new LinkedList<>();
  private int receiveBufferCapacity = 0;
  public int getReceiveBufferCapacity() {
    return this.receiveBufferCapacity;
  }
  
  private void send(int data) {
    if (getCable() == null) return;
    if (!getCable().hasCarrier()) return;
    if (getTransmitBufferCapacity() >= BUFFER_CAPACITY) {
      return;
    }
    Packet p = new Packet(data);
    if (transmitBuffer.offer(p)) {
      ++transmitBufferCapacity;
    }
  }
  
  private Packet recv() {
    if (getCable() == null) return null;
    if (!getCable().hasCarrier()) return null;
    Packet p = receiveBuffer.poll();
    if (p != null) {
      --receiveBufferCapacity;
    }
    return p;
  }
  
  private SerialCable cable = null;
  public void setCable(SerialCable cable) {
    this.cable = cable;
  }
  public SerialCable getCable() {
    return this.cable;
  }
  
  @Override
  public int getNumberOfPages() {
    return 1;
  }

  @Override
  public int readByte(int address) throws AddressTrapException {
    throw new AddressTrapException(4, address);
  }

  @Override
  public int readHalfword(int address) throws AddressTrapException {
    throw new AddressTrapException(4, address);
  }

  @Override
  public void writeByte(int address, int value) throws AddressTrapException {
    throw new AddressTrapException(6, address);
  }

  @Override
  public void writeHalfword(int address, int value) throws AddressTrapException {
    throw new AddressTrapException(6, address);
  }

  @Override
  public int readWord(int pAddr) throws AddressTrapException {
    int addr = translateAddress(pAddr);
    switch (addr) {
    case 0x0: // Receive Data
    {
      Packet p = recv();
      if (p == null) {
        return 0;
      } else {
        return p.getData();
      }
    }
    case 0x4: // Line Control
    case 0x8: // Line Status
    case 0xC: // Buffer Control
    case 0x10: // Transmit Buffer Status
    case 0x14: // Receive Buffer Status
    case 0x18: // Interrupt Enable
    case 0x1C: // Interrupt Status
    case 0x20: // Transceiver Period
    default:
      throw new AddressTrapException(5, pAddr);
    }
  }

  @Override
  public void writeWord(int pAddr, int value) throws AddressTrapException {
    int addr = translateAddress(pAddr);
    switch (addr) {
    case 0x0: // Transmit Data
      send(value);
      break;
    case 0x4: // Line Control
      break;
    case 0xC: // Buffer Cotnrol
      break;
    case 0x18: // Interrupt Enable
      break;
    case 0x20: // Transceiver Period
      transceiverPeriod = (long)(value) & 0x00000000FFFFFFFFL;
      transceiverPollingCount = 0L;
      break;
    default:
      throw new AddressTrapException(7, pAddr);
    }
  }

  @Override
  public void cycle() {
    if (transceiverPollingCount == transceiverPeriod) {
      transceiverPollingCount = 0L;
      // attempt to send and receive
      if (getCable() != null && getCable().hasCarrier()) {
        Packet pTransmit = transmitBuffer.poll();
        if (pTransmit != null) {
          transmitBufferCapacity--;
          getCable().sendPacket(this, pTransmit);
        }
        Packet pReceive = getCable().recvPacket(this);
        if (pReceive != null) {
          if (receiveBuffer.offer(pReceive)) {
            ++receiveBufferCapacity;
          }
        }
      }
    } else {
      transceiverPollingCount++;
    }
  }
  
  @Override
  public boolean interruptAsserted() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void acknowledgeInterrupt() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void timestep() {
    if (getCable() == null) return;
    getCable().sync(this);
  }

}
