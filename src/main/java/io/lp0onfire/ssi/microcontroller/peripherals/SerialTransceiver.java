package io.lp0onfire.ssi.microcontroller.peripherals;

import java.util.LinkedList;
import java.util.Queue;

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
  
  public static final Integer BUFFER_CAPACITY = 4096;
  
  private Queue<Packet> transmitBuffer = new LinkedList<>();
  private int transmitBufferCapacity = 0;
  public int getTransmitBufferCapacity() {
    return this.transmitBufferCapacity;
  }
  private int transmitBufferThreshold = 0;
  public int getTransmitBufferThreshold() {
    return this.transmitBufferThreshold;
  }
  
  private Queue<Packet> receiveBuffer = new LinkedList<>();
  private int receiveBufferCapacity = 0;
  public int getReceiveBufferCapacity() {
    return this.receiveBufferCapacity;
  }
  private int receiveBufferThreshold = 0;
  public int getReceiveBufferThreshold() {
    return this.receiveBufferThreshold;
  }
  
  private boolean transmitThresholdInterruptEnabled = false;
  private boolean receiveThresholdInterruptEnabled = false;
  private boolean modemStatusInterruptEnabled = false;
  
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
    {
      // recv buffer threshold: bits 11-0
      int result = receiveBufferThreshold-1;
      // xmit buffer threshold: bits 27-16
      result |= transmitBufferThreshold << 16;
      return result;
    }
    case 0x10: // Transmit Buffer Status
      return transmitBufferCapacity;
    case 0x14: // Receive Buffer Status
      return receiveBufferCapacity;
    case 0x18: // Interrupt Enable
    {
      int result = 0;
      if (receiveThresholdInterruptEnabled) result |= 1;
      if (transmitThresholdInterruptEnabled) result |= 2;
      if (modemStatusInterruptEnabled) result |= 4;
      return result;
    }
    case 0x1C: // Interrupt Status
    {
      int result = 0;
      if (receiveThresholdInterruptAsserted()) result |= 1;
      if (transmitThresholdInterruptAsserted()) result |= 2;
      // TODO modem status
      return result;
    }
    case 0x20: // Transceiver Period
      return (int)(transceiverPeriod & 0x00000000FFFFFFFFL);
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
    case 0xC: // Buffer Control
    {
      boolean flushTransmitBuffer = (value & (1<<31)) != 0;
      if (flushTransmitBuffer) {
        transmitBuffer.clear();
        transmitBufferCapacity = 0;
      }
      // xmit buffer threshold: bits 27-16
      this.transmitBufferThreshold = (value & 0x0FFF0000) >>> 16;
      boolean flushReceiveBuffer = (value & (1<<15)) != 0;
      if (flushReceiveBuffer) {
        receiveBuffer.clear();
        receiveBufferCapacity = 0;
      }
      // recv buffer threshold: bits 11-0
      this.receiveBufferThreshold = (value & 0x00000FFF) + 1;
    }
      break;
    case 0x18: // Interrupt Enable
    {
      receiveThresholdInterruptEnabled = (value & 0x00000001) != 0;
      transmitThresholdInterruptEnabled = (value & 0x00000002) != 0;
      modemStatusInterruptEnabled = (value & 0x00000004) != 0;
    }
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
  
  public boolean transmitThresholdInterruptAsserted() {
    return transmitBufferCapacity <= transmitBufferThreshold;
  }
  
  public boolean receiveThresholdInterruptAsserted() {
    return receiveBufferCapacity >= receiveBufferThreshold;
  }
  
  @Override
  public boolean interruptAsserted() {
    return (transmitThresholdInterruptEnabled && transmitThresholdInterruptAsserted())
        || (receiveThresholdInterruptEnabled && receiveThresholdInterruptAsserted())
        // TODO modem status interrupt
        ;
  }

  @Override
  public void acknowledgeInterrupt() {
    // we don't need to do anything special here as
    // interrupts are acknowledged by clearing the condition
    // that caused the interrupt
  }

  @Override
  public void timestep() {
    if (getCable() == null) return;
    getCable().sync(this);
  }

}
