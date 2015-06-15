package io.lp0onfire.ssi.microcontroller.peripherals;

import static org.junit.Assert.assertEquals;
import io.lp0onfire.ssi.microcontroller.AddressTrapException;

import org.junit.Before;
import org.junit.Test;

public class TestROM {

  private static byte[] testContents;
  private ROM rom;
  
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
    
    rom = new ROM(1);
    rom.setContents(testContents);
  }
  
  @Test
  public void testReadByte() throws AddressTrapException {
    int expected = 0x42;
    int actual = rom.readByte(1);
    assertEquals(expected, actual);
  }
  
  @Test
  public void testReadHalfword() throws AddressTrapException {
    int expected = 0x42A5;
    int actual = rom.readHalfword(0);
    assertEquals(expected, actual);
  }
  
  @Test
  public void testReadWord() throws AddressTrapException {
    int expected = 0x3B8042A5;
    int actual = rom.readWord(0);
    assertEquals(expected, actual);
  }
  
}
