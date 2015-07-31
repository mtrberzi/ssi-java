package io.lp0onfire.ssi.microcontroller.peripherals;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.assertEquals;
import io.lp0onfire.ssi.microcontroller.AddressTrapException;

public class TestSerialTransceiver {

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

  @Test
  public void testConnectToCable() {
    assertEquals(cable, com1.getCable());
  }

  @Test
  public void testTransmitBufferCapacity() throws AddressTrapException {
    assertEquals(0, com1.getTransmitBufferCapacity());
    com1.writeWord(0x0, (int)'a');
    assertEquals(1, com1.getTransmitBufferCapacity());
  }

  @Test(timeout=5000)
  public void testTransmitAndReceive() throws AddressTrapException {
    setTransceiverPeriod(0); // send and receive every cycle
    
    assertEquals(0, com1.getReceiveBufferCapacity());
    assertEquals(0, com2.getReceiveBufferCapacity());
    
    com1.writeWord(0x0, (int)'a');

    com1.cycle();
    com2.cycle();

    com1.timestep();
    com2.timestep();

    assertEquals(0, com1.getReceiveBufferCapacity());
    assertEquals(0, com2.getReceiveBufferCapacity());

    com1.cycle();
    com2.cycle();

    assertEquals(0, com1.getReceiveBufferCapacity());
    assertEquals(1, com2.getReceiveBufferCapacity());

    int rxData = com2.readWord(0x0);
    assertEquals((int)'a', rxData);
    assertEquals(0, com1.getReceiveBufferCapacity());
    assertEquals(0, com2.getReceiveBufferCapacity());

  }

}
