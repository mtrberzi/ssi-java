package io.lp0onfire.ssi.microcontroller.peripherals;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import io.lp0onfire.ssi.microcontroller.AddressTrapException;
import io.lp0onfire.ssi.microcontroller.InterruptSource;
import io.lp0onfire.ssi.microcontroller.SystemBusPeripheral;
import io.lp0onfire.ssi.model.Item;
import io.lp0onfire.ssi.model.Machine;
import io.lp0onfire.ssi.model.World;

public class InventoryController implements SystemBusPeripheral, InterruptSource {

  private Machine machine;
  private World world;
  
  private Map<Integer, LinkedList<Item>> objectBuffers = new HashMap<>();
  private static final int MAXIMUM_OBJECTS_PER_BUFFER = 32;
  
  public LinkedList<Item> getObjectBuffer(int n) {
    return objectBuffers.get(n);
  }
  
  public InventoryController(Machine machine, World world, int nObjectBuffers) {
    if (nObjectBuffers < 1 || nObjectBuffers > 16) {
      throw new IllegalArgumentException("number of object buffers must be between 1 and 16 inclusive");
    }
    for (int i = 0; i < nObjectBuffers; ++i) {
      objectBuffers.put(i, new LinkedList<Item>());
    }
    
    this.machine = machine;
    this.world = world;
  }
  
  public enum ErrorCode {
    NO_ERROR(0),
    ILLEGAL_INSN(1),
    UNDERFLOW(2),
    OVERFLOW(3),
    NO_SUCH_BUFFER(4),
    ;
    
    private final short code;
    public short getCode() {
      return this.code;
    }
    private ErrorCode(int code) {
      this.code = (short)code;
    }
  }
  
  private class Command {
    private final short insn;
    public short getInsn() {
      return this.insn;
    }
    
    private final int type;
    public int getType() {
      return this.type;
    }
    
    private final int opcode;
    public int getOpcode() {
      return this.opcode;
    }
    
    private boolean illegalInstruction = false;
    public boolean isIllegalInstruction() {
      return this.illegalInstruction;
    }
    
    private int destBuffer = 0;
    public int getDestBuffer() {
      return this.destBuffer;
    }
    
    private int sourceBuffer = 0;
    public int getSourceBuffer() {
      return this.sourceBuffer;
    }
    
    private boolean destTail = false;
    public boolean getDestTail() {
      return this.destTail;
    }
    
    private boolean sourceTail = false;
    public boolean getSourceTail() {
      return this.sourceTail;
    }
    
    private int totalCycles = 0;
    public int getTotalCycles() {
      return this.totalCycles;
    }
    
    private int executedCycles = 0;
    public int getExecutedCycles() {
      return this.executedCycles;
    }
    
    public void cycle() {
      executedCycles += 1;
    }
    
    private void type0Init() {
      switch (opcode) {
      case 1: // MOVE
        destBuffer = (insn & 0b0000001111000000) >>> 6;
        sourceBuffer = (insn & 0b0000000000011110) >>> 1;
        destTail = (insn & 0b0000000000100000) != 0;
        sourceTail = (insn & 0b0000000000000001) != 0;
        totalCycles = 50;
        break;
      default:
        this.illegalInstruction = true;
      }
    }
    
    private void type1Init() {
      
    }
    
    private void type2Init() {
      
    }
    
    private void type3Init() {
      
    }
    
    public Command(int insn) {
      this.insn = (short)insn;
      this.type = (this.insn & 0b1100000000000000) >>> 14;
      this.opcode = (this.insn & 0b0011110000000000) >>> 10;
      
      switch (type) {
      case 0:
        type0Init();
        break;
      case 1:
        type1Init();
        break;
      case 2:
        type2Init();
        break;
      case 3:
        type3Init();
        break;
      }
    }
  }
  
  private static final int MAXIMUM_NUMBER_OF_COMMANDS = 4;
  private Command[] commandQueue = new Command[MAXIMUM_NUMBER_OF_COMMANDS];
  
  private int numberOfCommands = 0;
  protected int getNumberOfCommands() {
    return this.numberOfCommands;
  }
  
  private boolean commandQueueStalled = false;
  private short errorCommand = 0;
  private short errorCode = 0;
  
  private void error(ErrorCode code, Command command) {
    this.commandQueueStalled = true;
    this.errorCommand = command.getInsn();
    this.errorCode = code.getCode();
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
  public int readWord(int pAddr) throws AddressTrapException {
    int addr = translateAddress(pAddr);
    switch (addr) {
    case 0: // INV_STATUS
    {
      int result = 0;
      // bit 3: command queue error
      if (commandQueueStalled) {
        result |= (1 << 3);
      }
      // bits 2-0: number of outstanding commands
      result |= (numberOfCommands & 0x00000007);
      return result;
    }
    case 4: // INV_ERRSTAT
    {
      int result = 0;
      if (commandQueueStalled) {
        // bits 31-16: errored command
        result |= (((int)errorCommand) & 0x0000FFFF) << 16;
        // bits 15-0: error code
        result |= ((int)errorCode) & 0x0000FFFF;
      } else {
        result = 0; // no error
      }
      return result;
    }
    default:
      throw new AddressTrapException(5, pAddr);
    }
  }

  @Override
  public void writeByte(int address, int value) throws AddressTrapException {
    throw new AddressTrapException(6, address);
  }

  @Override
  public void writeHalfword(int pAddr, int value) throws AddressTrapException {
    int addr = translateAddress(pAddr);
    switch (addr) {
    case 0: case 2: // INV_CMD, INV_CMD2
      enqueueCommand((short)(value & 0x0000FFFF));
      break;
    default:
      throw new AddressTrapException(6, pAddr);
    }
  }

  @Override
  public void writeWord(int pAddr, int value) throws AddressTrapException {
    int addr = translateAddress(pAddr);
    switch (addr) {
    case 0: // INV_CMD
      // TODO
    default:
      throw new AddressTrapException(7, pAddr);
    }
  }

  protected void enqueueCommand(short c) {
    if (numberOfCommands == MAXIMUM_NUMBER_OF_COMMANDS) {
      // cannot add new commands if maximum number are being processed
      return;
    }
    Command cmd = new Command(c);
    commandQueue[numberOfCommands] = cmd;
    numberOfCommands += 1;
  }
  
  /**
   * returns: true iff the first command blocks the second command
   * from executing concurrently
   */
  protected boolean commandBlocks(Command first, Command second) {
    if (first == null) return false;
    // TODO
    return true;
  }
  
  protected boolean executeType0(Command cmd) {
    switch (cmd.getOpcode()) {
    case 1: // MOVE
    {
      int srcBuffer = cmd.getSourceBuffer();
      boolean srcTail = cmd.getSourceTail();
      int dstBuffer = cmd.getDestBuffer();
      boolean dstTail = cmd.getDestTail();
      // if the destination and source are equal, abort with ILLEGAL_INSN
      if (srcBuffer == dstBuffer && srcTail == dstTail) {
        error(ErrorCode.ILLEGAL_INSN, cmd); return false;
      }
      // if the destination or source does not exist, abort with NO_SUCH_BUFFER
      if (!objectBuffers.containsKey(srcBuffer) || !objectBuffers.containsKey(dstBuffer)) {
        error(ErrorCode.NO_SUCH_BUFFER, cmd); return false;
      }
      LinkedList<Item> src = objectBuffers.get(srcBuffer);
      LinkedList<Item> dst = objectBuffers.get(dstBuffer);
      // if the source buffer contains no objects, abort with UNDERFLOW
      if (src.isEmpty()) {
        error(ErrorCode.UNDERFLOW, cmd); return false;
      }
      // if the destination buffer cannot hold any more objects, abort with OVERFLOW
      // ** special case: if srcBuffer and dstBuffer are the same, this is fine
      if (dst.size() == MAXIMUM_OBJECTS_PER_BUFFER && srcBuffer != dstBuffer) {
        error(ErrorCode.OVERFLOW, cmd); return false;
      }
      // move one object from source to destination
      Item movedObject;
      if (srcTail) {
        movedObject = src.removeLast();
      } else {
        movedObject = src.removeFirst();
      }
      if (dstTail) {
        dst.addLast(movedObject);
      } else {
        dst.addFirst(movedObject);
      }
      // success
      return true;
    }
    default:
      error(ErrorCode.ILLEGAL_INSN, cmd); return false;
    }
  }
  
  protected boolean executeType1(Command cmd) {
    // TODO
    error(ErrorCode.ILLEGAL_INSN, cmd); return false;
  }
  
  protected boolean executeType2(Command cmd) {
    // TODO
    error(ErrorCode.ILLEGAL_INSN, cmd); return false;
  }
  
  protected boolean executeType3(Command cmd) {
    // TODO
    error(ErrorCode.ILLEGAL_INSN, cmd); return false;
  }
  
  protected boolean execute(Command cmd) {
    if (cmd.isIllegalInstruction()) {
      error(ErrorCode.ILLEGAL_INSN, cmd);
    }
    switch (cmd.getType()) {
    case 0:
      return executeType0(cmd);
    case 1:
      return executeType1(cmd);
    case 2:
      return executeType2(cmd);
    case 3:
      return executeType3(cmd);
    default:
      throw new IllegalStateException("internal error: illegal inventory command type " + cmd.getType());
    }
  }
  
  @Override
  public void cycle() {
    if (commandQueueStalled) {
      // TODO
    } else {
      // check each command in turn, and see whether it can execute
      // concurrently with all commands ahead of it
      for (int i = 0; i < numberOfCommands; ++i) {
        Command cmd_later = commandQueue[i];
        // TODO this can be cached, as it only needs to be recomputed
        // when the command queue changes
        boolean commandCanExecute = true;
        for (int j = 0; j < i; ++j) {
          Command cmd_earlier = commandQueue[j];
          if (commandBlocks(cmd_earlier, cmd_later)) {
            commandCanExecute = false;
            break;
          }
        }
        if (commandCanExecute) {
          cmd_later.cycle();
          if (cmd_later.getExecutedCycles() >= cmd_later.getTotalCycles()) {
            // execute command
            boolean status = execute(cmd_later);
            if (status) {
              // success, clean up
              commandQueue[i] = null;
            } else {
              // failure, abort processing
              break;
            }
          }
        }
      }
      // clear all nulls out of the command queue
      for (int i = 0; i < numberOfCommands; ++i) {
        Command cmd = commandQueue[i];
        if (cmd == null) {
          // shift all commands after this one ahead by one place
          for (int j = i+1; j < numberOfCommands; ++j) {
            commandQueue[j-1] = commandQueue[j];
          }
          i -= 1;
          numberOfCommands -= 1;
        }
      }
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
