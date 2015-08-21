package io.lp0onfire.ssi.microcontroller.peripherals;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import io.lp0onfire.ssi.microcontroller.AddressTrapException;
import io.lp0onfire.ssi.microcontroller.RV32SystemBus;
import io.lp0onfire.ssi.model.Machine;
import io.lp0onfire.ssi.model.Vector;
import io.lp0onfire.ssi.model.VoxelOccupant;
import io.lp0onfire.ssi.model.World;

public class TestSensorSystem {

  class TestObject extends Machine {

    @Override
    public boolean impedesXYMovement() {
      return false;
    }
    
    @Override
    public boolean impedesZMovement() {
      return false;
    }

    @Override
    public boolean impedesXYFluidFlow() {
      return false;
    }

    @Override
    public boolean impedesZFluidFlow() {
      return false;
    }

    @Override
    public boolean supportsOthers() {
      return false;
    }

    @Override
    public boolean needsSupport() {
      return false;
    }

    @Override
    public boolean canMove() {
      return true;
    }

    @Override
    public Vector getExtents() {
      return new Vector(1, 1, 1);
    }

    @Override
    public boolean hasWorldUpdates() {
      return false;
    }
    
    @Override
    public int getType() {
      return 0;
    }
    
  };
  
  private VoxelOccupant testObject;
  private RV32SystemBus bus;
  private RAM ram;
  private SensorSystem sensors;
  private World world;
  
  private Vector testObjectPosition = new Vector(2, 2, 1);
  private int ramBaseAddress = 0x10000000;
  private int responseBufferAddress = ramBaseAddress + 0x1000;
  private int sensorBaseAddress = 0xF0000000;
  
  @Before
  public void setup() {
    this.bus = new RV32SystemBus();
    this.ram = new RAM(8);
    this.bus.attachPeripheral(ram, ramBaseAddress);
    this.testObject = new TestObject();
    this.world = new World(10, 5);
    assertTrue(this.world.addOccupant(testObjectPosition, new Vector(0,0,0), this.testObject));
    this.sensors = new SensorSystem(testObject, world);
    this.sensors.setSystemBus(this.bus);
    this.bus.attachPeripheral(sensors, sensorBaseAddress);
  }
  
  // returns: a non-null ByteBuffer corresponding to the scan response,
  // iff the scan completed successfully
  private ByteBuffer performScan(ByteBuffer queryBuffer, int responseBufferAddress, int cycleTimeout) {
    System.err.println();
    try {
      queryBuffer.position(0);
      // write the entire query buffer into RAM
      int destAddr = ramBaseAddress;
      for (int i = 0; i < queryBuffer.capacity(); ++i) {
        byte b = queryBuffer.get();
        bus.storeByte(destAddr, (int)b);
        destAddr += 1;
      }
      
      // make sure at least the first word of the response buffer can be correctly written
      bus.storeWord(responseBufferAddress, 0xa5a5a5a5);
      
      // now write the base address to the sensor peripheral to start the query
      bus.storeWord(sensorBaseAddress, ramBaseAddress);
      for (int c = 0; c < cycleTimeout; ++c) {
        sensors.cycle();
        // try to read status register
        int status = bus.loadWord(sensorBaseAddress);
        if ((status & 0x00000001) != 0) {
          // query completed
          if ((status & 0x00000002) != 0) {
            // error
            return null;
          } else {
            // copy to response buffer, in two steps
            ByteBuffer responseHeader = ByteBuffer.allocate(8);
            responseHeader.order(ByteOrder.LITTLE_ENDIAN);
            responseHeader.putInt(bus.loadWord(responseBufferAddress));
            responseHeader.putInt(bus.loadWord(responseBufferAddress + 4));
            // get shorts at offsets 4 and 6, to have # objects and record size
            int numberOfObjects = responseHeader.getShort(4);
            int objectSize = responseHeader.getShort(6);
            int responseRecordSize = numberOfObjects * objectSize;
            ByteBuffer responseBuffer = ByteBuffer.allocate(8 + responseRecordSize);
            responseBuffer.order(ByteOrder.LITTLE_ENDIAN);
            // copy header
            responseBuffer.put(responseHeader);
            // now copy all records from memory into response buffer
            int srcAddr = responseBufferAddress + 8;
            for (int i = 0; i < numberOfObjects; ++i) {
              for (int b = 0; b < responseRecordSize; ++b) {
                byte tmp = (byte)bus.loadByte(srcAddr);
                responseBuffer.put(tmp);
                srcAddr += 1;
              }
            }
            return responseBuffer;
          }
        }
      }
      fail("scan timed out");
    } catch (AddressTrapException e) {
      e.printStackTrace();
      fail("bus error");
    }
    // unreachable
    return null;
  }
  
  private int getResponseErrorCode(ByteBuffer response) {
    return (int)response.getShort(0);
  }
  
  private int getResponseTotalObjects(ByteBuffer response) {
    return (int)response.getShort(2);
  }
  
  private int getResponseNumberOfRecords(ByteBuffer response) {
    return (int)response.getShort(4);
  }
  
  private int getResponseRecordSize(ByteBuffer response) {
    return (int)response.getShort(6);
  }
  
  @Test
  public void testQuery_LocalScan_EmptyVoxel_NoResults() {
    ByteBuffer query = ByteBuffer.allocate(12);
    query.order(ByteOrder.LITTLE_ENDIAN);
    query.putShort((short)1);
    query.putShort(Short.MAX_VALUE);
    query.putInt(responseBufferAddress);
    query.putInt(0x4000);
    ByteBuffer response = performScan(query, responseBufferAddress, 3000);
    assertNotNull(response);
    response.position(0);
    assertEquals(0, getResponseErrorCode(response));
    assertEquals(0, getResponseTotalObjects(response));
    assertEquals(0, getResponseNumberOfRecords(response));
  }
  
}
