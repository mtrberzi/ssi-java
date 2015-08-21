package io.lp0onfire.ssi.microcontroller.peripherals;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import io.lp0onfire.ssi.microcontroller.AddressTrapException;
import io.lp0onfire.ssi.microcontroller.InterruptSource;
import io.lp0onfire.ssi.microcontroller.RV32SystemBus;
import io.lp0onfire.ssi.microcontroller.SystemBusPeripheral;
import io.lp0onfire.ssi.model.VoxelOccupant;
import io.lp0onfire.ssi.model.World;

public class SensorSystem implements SystemBusPeripheral, InterruptSource {

  private VoxelOccupant robot;
  private World world;
  
  public SensorSystem(VoxelOccupant robot, World world) {
    this.robot = robot;
    this.world = world;
  }
  
  public enum QueryType {
    // Queries require 3 + the number of extra words of query-specific data
    // dma cycles to perform all read cycles.
    
    Q_RETRIEVE_RESULTS(0, 4, 0),
    Q_LOCAL_SCAN(1, 3, 2000),
    ;
    
    private final short index;
    public short getIndex() {
      return this.index;
    }
    
    private final int numberOfReadCycles;
    public int getNumberOfReadCycles() {
      return this.numberOfReadCycles;
    }
    
    private final int numberOfQueryCycles;
    public int getNumberOfQueryCycles() {
      return this.numberOfQueryCycles;
    }
    
    private QueryType(int idx, int numberOfReadCycles, int numberOfQueryCycles) {
      this.index = (short)idx;
      this.numberOfReadCycles = numberOfReadCycles;
      this.numberOfQueryCycles = numberOfQueryCycles;
    }
    
    public static QueryType getQueryByIndex(short idx) {
      // TODO this is slow, optimize
      for (QueryType qt : QueryType.values()) {
        if (qt.getIndex() == idx) {
          return qt;
        }
      }
      return null;
    }
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
  private QueryType queryType;
  private int queryCycle = 0;
  
  private int maxObjectsPerResponse;
  private int responseBufferAddress;
  private int responseBufferSize; // in bytes
  
  private ArrayList<VoxelOccupant> queryResultSet = new ArrayList<>();
  
  private ByteBuffer resultBuffer;
  
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
          queryType = QueryType.getQueryByIndex((short)qtype);
          if (queryType == null) {
            // abort due to invalid query type
            this.state = SensorState.STATE_IDLE;
            this.queryError = true;
            return;
          }
          // and allocate buffer now
          queryBuffer = ByteBuffer.allocate(queryType.getNumberOfReadCycles() * 4);
          queryBuffer.putInt(tmp);
        } else {
          queryBuffer.putInt(tmp);
        }
        ++dmaCycle;
        if (dmaCycle == queryType.getNumberOfReadCycles()) {
          // figure out the rest of the query parameters
          queryBuffer.position(0);
          queryBuffer.getShort(); // query type
          maxObjectsPerResponse = (int)queryBuffer.getShort();
          responseBufferAddress = queryBuffer.getInt();
          responseBufferSize = queryBuffer.getInt();
          // now the query buffer's position points to the first query-specific parameter
          this.state = SensorState.STATE_EXECUTE_QUERY;
          this.queryCycle = 0;
        }
      }
        break;
      case STATE_EXECUTE_QUERY:
        ++queryCycle;
        switch (queryType) {
        case Q_RETRIEVE_RESULTS:
        {
          if (true){
            throw new UnsupportedOperationException("not yet implemented");
          }
        } break;
        case Q_LOCAL_SCAN:
        {
          if (queryCycle == 1) {
            // grab list of all items in our voxel
            queryResultSet.clear();
            queryResultSet.addAll(world.getOccupants(robot.getPosition(), robot.getExtents()));
          } else {
            if (queryCycle == queryType.getNumberOfQueryCycles()) {
              // prepare result buffer
              // object type is always present and non-zero
              // header is 8 bytes
              // each object record is 16 + 2 + 2 + 4 = 24 bytes
              int objectsToReturn = Math.min(maxObjectsPerResponse, queryResultSet.size());
              resultBuffer = ByteBuffer.allocate(8 + 24*objectsToReturn);
              
              // write header
              resultBuffer.putShort((short)0); // error code: no error
              resultBuffer.putShort((short)Integer.min(Short.MAX_VALUE, queryResultSet.size())); // total # objects
              resultBuffer.putShort((short)objectsToReturn); // # objects in this response
              resultBuffer.putShort((short)24); // size of each object record
              
              // write object records
              for (int i = 0; i < objectsToReturn; ++i) {
                VoxelOccupant obj = queryResultSet.get(i);
                // object ID
                resultBuffer.putLong(obj.getUUID().getLeastSignificantBits());
                resultBuffer.putLong(obj.getUUID().getMostSignificantBits());
                // object kind
                resultBuffer.putShort(obj.getKind());
                // object flags
                // TODO
                resultBuffer.putShort((short)0);
                // object type
                resultBuffer.putInt(obj.getType());
              }
              
              this.state = SensorState.STATE_DMA_WRITE;
            }
          }
          break;
        }
        default:
          throw new IllegalStateException("query execution for " + queryType.toString() + " not yet implemented");
        }
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
