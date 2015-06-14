package io.lp0onfire.ssi.microcontroller;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class TestRV32SystemBus {

  // test peripheral that asserts that it is always being accessed at address 0x120
  class TestPeripheral implements SystemBusPeripheral {

    @Override
    public int getNumberOfPages() {
      return 1;
    }

    @Override
    public int readByte(int address) throws AddressTrapException {
      int localAddr = translateAddress(address);
      assertEquals(0x120, localAddr);
      return 0;
    }

    @Override
    public int readHalfword(int address) throws AddressTrapException {
      int localAddr = translateAddress(address);
      assertEquals(0x120, localAddr);
      return 0;
    }

    @Override
    public int readWord(int address) throws AddressTrapException {
      int localAddr = translateAddress(address);
      assertEquals(0x120, localAddr);
      return 0;
    }

    @Override
    public void writeByte(int address, int value) throws AddressTrapException {
      int localAddr = translateAddress(address);
      assertEquals(0x120, localAddr);
    }

    @Override
    public void writeHalfword(int address, int value)
        throws AddressTrapException {
      int localAddr = translateAddress(address);
      assertEquals(0x120, localAddr);
    }

    @Override
    public void writeWord(int address, int value) throws AddressTrapException {
      int localAddr = translateAddress(address);
      assertEquals(0x120, localAddr);
    }
    
  }
  
  private RV32SystemBus bus;
  private static final int baseAddress = 0xE1234000;
  
  @Before
  public void setup() {
    bus = new RV32SystemBus();
    bus.attachPeripheral(new TestPeripheral(), baseAddress);
  }
  
  @Test
  public void testLoadByte() throws AddressTrapException {
    bus.loadByte(0xE1234120);
  }
  
}
