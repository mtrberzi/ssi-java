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
  
  @Test
  public void testLoadByte_UnmappedPage() {
    try {
      bus.loadByte(0xABADD00D);
      fail("access to unmapped page not detected");
    } catch (AddressTrapException e) {
      assertEquals(5, e.getMCause());
      assertEquals(0xABADD00D, e.getBadAddr());
    }
  }
  
  @Test
  public void testLoadHalfword() throws AddressTrapException {
    bus.loadHalfword(0xE1234120);
  }
  
  @Test
  public void testLoadHalfword_UnmappedPage() {
    try {
      bus.loadHalfword(0xABADD00E);
      fail("access to unmapped page not detected");
    } catch (AddressTrapException e) {
      assertEquals(5, e.getMCause());
      assertEquals(0xABADD00E, e.getBadAddr());
    }
  }
  
  @Test
  public void testLoadHalfword_UnalignedAddress() {
    try {
      bus.loadHalfword(0xABADD00D);
      fail("unaligned access not detected");
    } catch (AddressTrapException e) {
      assertEquals(4, e.getMCause());
      assertEquals(0xABADD00D, e.getBadAddr());
    }
  }
  
  @Test
  public void testLoadWord() throws AddressTrapException {
    bus.loadWord(0xE1234120);
  }
  
  @Test
  public void testLoadWord_UnmappedPage() {
    try {
      bus.loadWord(0xABADD00C);
      fail("access to unmapped page not detected");
    } catch (AddressTrapException e) {
      assertEquals(5, e.getMCause());
      assertEquals(0xABADD00C, e.getBadAddr());
    }
  }
  
  @Test
  public void testLoadWord_UnalignedAccess() {
    try {
      bus.loadWord(0xABADD00D);
      fail("unaligned access not detected");
    } catch (AddressTrapException e) {
      assertEquals(4, e.getMCause());
      assertEquals(0xABADD00D, e.getBadAddr());
    }
  }
  
  @Test
  public void testStoreByte() throws AddressTrapException {
    bus.storeByte(0xE1234120, 0xFF);
  }
  
  @Test
  public void testStoreByte_UnmappedPage() {
    try {
      bus.storeByte(0xABADD00D, 0xFF);
      fail("access to unmapped page not detected");
    } catch (AddressTrapException e) {
      assertEquals(7, e.getMCause());
      assertEquals(0xABADD00D, e.getBadAddr());
    }
  }
  
  @Test
  public void testStoreHalfword() throws AddressTrapException {
    bus.storeHalfword(0xE1234120, 0xFFFF);
  }
  
  @Test
  public void testStoreHalfword_UnmappedPage() {
    try {
      bus.storeHalfword(0xABADD00E, 0xFFFF);
      fail("access to unmapped page not detected");
    } catch (AddressTrapException e) {
      assertEquals(7, e.getMCause());
      assertEquals(0xABADD00E, e.getBadAddr());
    }
  }
  
  @Test
  public void testStoreHalfword_UnalignedAddress() {
    try {
      bus.storeHalfword(0xABADD00D, 0xFFFF);
      fail("unaligned access not detected");
    } catch (AddressTrapException e) {
      assertEquals(6, e.getMCause());
      assertEquals(0xABADD00D, e.getBadAddr());
    }
  }
  
  @Test
  public void testStoreWord() throws AddressTrapException {
    bus.storeWord(0xE1234120, 0xFFFFFFFF);
  }
  
  @Test
  public void testStoreWord_UnmappedPage() {
    try {
      bus.storeWord(0xABADD00C, 0xFFFFFFFF);
      fail("access to unmapped page not detected");
    } catch (AddressTrapException e) {
      assertEquals(7, e.getMCause());
      assertEquals(0xABADD00C, e.getBadAddr());
    }
  }
  
  @Test
  public void testStoreWord_UnalignedAccess() {
    try {
      bus.storeWord(0xABADD00D, 0xFFFFFFFF);
      fail("unaligned access not detected");
    } catch (AddressTrapException e) {
      assertEquals(6, e.getMCause());
      assertEquals(0xABADD00D, e.getBadAddr());
    }
  }
  
  @Test
  public void testFetchInstruction() throws AddressTrapException {
    RV32Instruction insn = bus.fetchInstruction(0xE1234120);
    assertNotNull(insn);
  }
  
  @Test
  public void testFetchInstruction_UnmappedPage() {
    try {
      bus.fetchInstruction(0xABADD00C);
      fail("access to unmapped page not detected");
    } catch (AddressTrapException e) {
      assertEquals(1, e.getMCause());
      assertEquals(0xABADD00C, e.getBadAddr());
    }
  }
  
  @Test
  public void testFetchInstruction_UnalignedAccess() {
    try {
      bus.fetchInstruction(0xABADD00D);
      fail("unaligned access not detected");
    } catch (AddressTrapException e) {
      assertEquals(0, e.getMCause());
      assertEquals(0xABADD00D, e.getBadAddr());
    }
  }
  
}
