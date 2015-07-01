package io.lp0onfire.ssi.microcontroller;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestSystemBusPeripheral {

  class TestPeripheral implements SystemBusPeripheral {

    private final int nPages;
    public TestPeripheral(int nPages) {
      this.nPages = nPages;
    }
    
    @Override
    public int getNumberOfPages() {
      return this.nPages;
    }

    @Override
    public int readByte(int address) throws AddressTrapException {
      return 0;
    }

    @Override
    public int readHalfword(int address) throws AddressTrapException {
      return 0;
    }

    @Override
    public int readWord(int address) throws AddressTrapException {
      return 0;
    }

    @Override
    public void writeByte(int address, int value) throws AddressTrapException {
    }

    @Override
    public void writeHalfword(int address, int value) throws AddressTrapException {
    }

    @Override
    public void writeWord(int address, int value) throws AddressTrapException {
    }
    
    @Override
    public void cycle() {
    }
    
  }
  
  @Test
  public void testTranslateAddress_OnePage() {
    TestPeripheral p = new TestPeripheral(1);
    int pAddress = 0x56789B45;
    int expected = 0x00000345;
    int actual = p.translateAddress(pAddress);
    assertEquals(expected, actual);
  }
  
}
