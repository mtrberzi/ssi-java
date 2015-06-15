package io.lp0onfire.ssi.microcontroller.peripherals;

import static org.junit.Assert.assertEquals;
import io.lp0onfire.ssi.microcontroller.AddressTrapException;

import org.junit.Before;
import org.junit.Test;

public class TestRAM {

  private static byte[] testContents;
  private RAM ram;
  
  @Before
  public void setupTest() {
    testContents = new byte[1024];
    for (int i = 0; i < 1024; ++i) {
      testContents[i] = 0;
    }
    testContents[0] = (byte)0xA5;
    testContents[1] = (byte)0x42;
    testContents[2] = (byte)0x80;
    testContents[3] = (byte)0x3B;
    
    ram = new RAM(1);
    ram.setContents(testContents);
  }
  
  @Test
  public void testReadByte() throws AddressTrapException {
    int expected = 0x42;
    int actual = ram.readByte(1);
    assertEquals(expected, actual);
  }
  
  @Test
  public void testReadHalfword() throws AddressTrapException {
    int expected = 0x42A5;
    int actual = ram.readHalfword(0);
    assertEquals(expected, actual);
  }
  
  @Test
  public void testReadWord() throws AddressTrapException {
    int expected = 0x3B8042A5;
    int actual = ram.readWord(0);
    assertEquals(expected, actual);
  }
  
  @Test
  public void testWriteByte() throws AddressTrapException {
    int expected = 0x000000F3;
    ram.writeByte(1, expected);
    int actual = ram.readByte(1);
    assertEquals(expected, actual);
  }
  
  @Test
  public void testWriteHalfword() throws AddressTrapException {
    int expected = 0x0000D5F3;
    ram.writeHalfword(0, expected);
    int actual = ram.readHalfword(0);
    assertEquals(expected, actual);
  }
  
  @Test
  public void testWriteWord() throws AddressTrapException {
    int expected = 0x907BD5F3;
    ram.writeWord(0, expected);
    int actual = ram.readWord(0);
    assertEquals(expected, actual);
  }
  
}
