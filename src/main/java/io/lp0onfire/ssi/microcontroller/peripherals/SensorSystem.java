package io.lp0onfire.ssi.microcontroller.peripherals;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import io.lp0onfire.ssi.microcontroller.AddressTrapException;
import io.lp0onfire.ssi.microcontroller.InterruptSource;
import io.lp0onfire.ssi.microcontroller.RV32SystemBus;
import io.lp0onfire.ssi.microcontroller.SystemBusPeripheral;

public class SensorSystem implements SystemBusPeripheral, InterruptSource {

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

  private enum SensorState {
    STATE_IDLE,
    STATE_DMA_READ,
    STATE_EXECUTE_QUERY,
    STATE_DMA_WRITE,
  }
  
  private SensorState state = SensorState.STATE_IDLE;
  
  @Override
  public int readWord(int pAddr) throws AddressTrapException {
    int addr = translateAddress(pAddr);
    switch (addr) {
    default:
      throw new AddressTrapException(5, pAddr);
    }
  }

  @Override
  public void writeWord(int pAddr, int value) throws AddressTrapException {
    int addr = translateAddress(pAddr);
    switch (addr) {
    case 0: // Query Transfer Register
      if (state == SensorState.STATE_IDLE) {
        // okay, start reading
        this.queryBufferAddress = value;
        this.state = SensorState.STATE_DMA_READ;
        this.dmaCycle = 0;
        this.queryError = false;
        // make a new query buffer
        this.queryBuffer.order(ByteOrder.LITTLE_ENDIAN);
      } else {
        // ignore, we aren't ready to receive a query
      }
      break;
    default:
      throw new AddressTrapException(7, pAddr);
    }
  }

  private RV32SystemBus bus;
  
  @Override
  public void setSystemBus(RV32SystemBus bus) {
    this.bus = bus;
  }
  
  private boolean queryError = false;
  private int queryBufferAddress = 0;
  private int dmaCycle = 0;
  private ByteBuffer queryBuffer;
  private short queryType;
  
  @Override
  public void cycle() {
    try {
      switch (state) {
      case STATE_IDLE:
        break;
      case STATE_DMA_READ:
      {
        int tmp = bus.loadWord(queryBufferAddress);
        if (dmaCycle == 0) {
          // grab query type
          int qtype = tmp & 0x0000FFFF;
          queryType = (short)qtype;
          // and allocate buffer now
          queryBuffer = ByteBuffer.allocate(queryNWords() * 4);
          queryBuffer.putInt(tmp);
        } else {
          queryBuffer.putInt(tmp);
        }
        ++dmaCycle;
        if (dmaCycle == queryNWords()) {
            this.state = SensorState.STATE_EXECUTE_QUERY;
        }
      }
        break;
      case STATE_EXECUTE_QUERY:
        break;
      case STATE_DMA_WRITE:
        break;
      default:
        throw new IllegalStateException("sensor system state " + state.toString() + " not yet implemented");
      }
    } catch (AddressTrapException e) {
      // DMA error, abort query
      this.state = SensorState.STATE_IDLE;
      this.queryError = true;
    }
  }
  
  protected int queryNWords() {
    // queries require 3 + the number of extra words of query-specific data
    // dma cycles to complete
    switch (queryType) {
    case 0:
      return 4;
    case 1:
      return 3;
    default:
      // unknown query type, abort query
      this.state = SensorState.STATE_IDLE;
      this.queryError = true;
      return Integer.MAX_VALUE;
    }
  }
  
  @Override
  public void timestep() {
    // TODO Auto-generated method stub
    
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

  
}
