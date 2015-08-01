package io.lp0onfire.ssi.microcontroller.peripherals;

import org.junit.Test;
import org.junit.Before;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import io.lp0onfire.ssi.microcontroller.AddressTrapException;

public class TestSerialTransceiver {

  private static final boolean tracing = false;
  
  private SerialTransceiver com1;
  private SerialTransceiver com2;
  private SerialCable cable;

  @Before
  public void setup() {
    com1 = new SerialTransceiver();
    com2 = new SerialTransceiver();
    cable = new SerialCable();
    cable.connect(com1);
    cable.connect(com2);
  }

  private void setTransceiverPeriod(int period) throws AddressTrapException {
    com1.writeWord(0x20, period);
    com2.writeWord(0x20, period);
  }

  private String getStatusLine(SerialTransceiver st) {
    StringBuilder statusLine = new StringBuilder();
    statusLine.append("[ ");
    statusLine.append("txBuffer=").append(st.getTransmitBufferCapacity()).append(" ");
    statusLine.append("rxBuffer=").append(st.getReceiveBufferCapacity()).append(" ");
    statusLine.append("threshold=").append(st.getTransmitBufferThreshold())
      .append("/").append(st.getReceiveBufferThreshold()).append(" ");
    statusLine.append("]");
    return statusLine.toString();
  }
  
  private void trace() {
    if (!tracing) return;
    StringBuilder statusLine = new StringBuilder();
    statusLine.append("com1=").append(getStatusLine(com1)).append(" ");
    statusLine.append("com2=").append(getStatusLine(com2)).append(" ");
    System.out.println(statusLine.toString());
  }
  
  private void cycle() {
    com1.cycle();
    com2.cycle();
    
    trace();
  }
  
  private void timestep() {
    com1.timestep();
    com2.timestep();
  }
  
  @Test
  public void testInitialState_InterruptsDisabled() throws AddressTrapException {
    assertEquals(0, com1.readWord(0x18));
  }
  
  @Test
  public void testConnectToCable() {
    assertEquals(cable, com1.getCable());
  }

  @Test
  public void testTransmitBufferCapacity() throws AddressTrapException {
    assertEquals(0, com1.getTransmitBufferCapacity());
    com1.writeWord(0x0, (int)'a');
    assertEquals(1, com1.getTransmitBufferCapacity());
    assertEquals(1, com1.readWord(0x10));
  }

  @Test
  public void testTransmitBufferThreshold_ReadAfterWrite() throws AddressTrapException {
    int expected = 0x0FFF0000;
    com1.writeWord(0xC, 0x1FFF0000);
    int actual = com1.readWord(0xC);
    assertEquals(expected, actual);
  }
  
  @Test
  public void testReceiveBufferThreshold_ReadAfterWrite() throws AddressTrapException {
    int expected = 0x00000FFF;
    com1.writeWord(0xC, 0x00001FFF);
    int actual = com1.readWord(0xC);
    assertEquals(expected, actual);
  }
  
  @Test
  public void testFlushTransmitBuffer() throws AddressTrapException {
    com1.writeWord(0x0, (int)'a');
    com1.writeWord(0xC, 0x80000000);
    assertEquals(0, com1.getTransmitBufferCapacity());
  }
  
  @Test
  public void testTransmitThresholdInterrupt() throws AddressTrapException {
    // set transmit threshold = 4
    com1.writeWord(0x0c, 0x00040000);
    // enable interrupts
    com1.writeWord(0x18, 0x00000002);
    // transmit threshold interrupt should be asserted
    assertEquals(0x2, com1.readWord(0x1C));
    assertTrue(com1.interruptAsserted());
    // write 5 characters to the buffer
    for (int i = 0; i < 5; ++i) {
      com1.writeWord(0x0, (int)'a');
    }
    // this should clear the interrupt
    assertEquals(0x0, com1.readWord(0x1C));
    assertFalse(com1.interruptAsserted());
  }

  @Test(timeout=5000)
  public void testTransmitAndReceive() throws AddressTrapException {
    setTransceiverPeriod(0); // send and receive every cycle
    
    assertEquals(0, com1.getReceiveBufferCapacity());
    assertEquals(0, com2.getReceiveBufferCapacity());
    
    com1.writeWord(0x0, (int)'a');

    cycle();

    timestep();

    assertEquals(0, com1.getReceiveBufferCapacity());
    assertEquals(0, com2.getReceiveBufferCapacity());

    cycle();

    assertEquals(0, com1.getReceiveBufferCapacity());
    assertEquals(1, com2.getReceiveBufferCapacity());

    int rxData = com2.readWord(0x0);
    assertEquals((int)'a', rxData);
    assertEquals(0, com1.getReceiveBufferCapacity());
    assertEquals(0, com2.getReceiveBufferCapacity());
  }
  
  @Test(timeout=5000)
  public void testReceiveThresholdInterrupt() throws AddressTrapException {
    setTransceiverPeriod(0); // send and receive every cycle
    // set receive threshold = 3 + 1
    com2.writeWord(0x0c, 0x0000003);
    // enable interrupts
    com2.writeWord(0x18, 0x00000001);
    // receive threshold interrupt should not be asserted
    assertEquals(0x00, com2.readWord(0x1C) & 0x01);
    assertFalse(com2.interruptAsserted());
    
    // send twice
    com1.writeWord(0x0, (int)'a');
    cycle();
    com1.writeWord(0x0, (int)'a');
    cycle();
    
    timestep();
    cycle();
    cycle();
    
    // receive threshold interrupt should not be asserted
    assertEquals(0x00, com2.readWord(0x1C) & 0x01);
    assertFalse(com2.interruptAsserted());
    
    // send twice more
    com1.writeWord(0x0, (int)'a');
    cycle();
    com1.writeWord(0x0, (int)'a');
    cycle();
    
    timestep();
    cycle();
    cycle();
    
    // receive threshold interrupt should be asserted now
    assertEquals(0x01, com2.readWord(0x1C) & 0x01);
    assertTrue(com2.interruptAsserted());
    
  }

}
