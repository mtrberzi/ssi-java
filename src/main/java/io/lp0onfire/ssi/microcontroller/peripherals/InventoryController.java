package io.lp0onfire.ssi.microcontroller.peripherals;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

import io.lp0onfire.ssi.microcontroller.AddressTrapException;
import io.lp0onfire.ssi.microcontroller.InterruptSource;
import io.lp0onfire.ssi.microcontroller.SystemBusPeripheral;
import io.lp0onfire.ssi.model.Item;
import io.lp0onfire.ssi.model.Machine;

public class InventoryController implements SystemBusPeripheral, InterruptSource {

  private Machine machine;
  
  private Map<Integer, LinkedList<Item>> objectBuffers = new HashMap<>();
  private static final int MAXIMUM_OBJECTS_PER_BUFFER = 32;
  
  public LinkedList<Item> getObjectBuffer(int n) {
    return objectBuffers.get(n);
  }
  
  public InventoryController(Machine machine, int nObjectBuffers) {
    if (nObjectBuffers < 1 || nObjectBuffers > 16) {
      throw new IllegalArgumentException("number of object buffers must be between 1 and 16 inclusive");
    }
    for (int i = 0; i < nObjectBuffers; ++i) {
      objectBuffers.put(i, new LinkedList<Item>());
    }
    
    this.machine = machine;
  }
  
  public enum ErrorCode {
    NO_ERROR(0),
    ILLEGAL_INSN(1),
    UNDERFLOW(2),
    OVERFLOW(3),
    NO_SUCH_BUFFER(4),
    ILLEGAL_MANIP(5),
    MANIP_ERROR(6),
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
    
    public boolean isManipulatorCommand() {
      return (getType() == 2);
    }
    
    private boolean manipulatorCommandIssued = false;
    public boolean getManipulatorCommandIssued() {
      return this.manipulatorCommandIssued;
    }
    public void setManipulatorCommandIssued(boolean b) {
      this.manipulatorCommandIssued = b;
    }
    
    private boolean manipulatorCommandCompleted = false;
    public boolean getManipulatorCommandCompleted() {
      return this.manipulatorCommandCompleted;
    }
    public void setManipulatorCommandCompleted(boolean b) {
      this.manipulatorCommandCompleted = b;
    }
    
    private int uuidReg;
    public int getUUIDReg() {
      return this.uuidReg;
    }
    
    private int manipID;
    public int getManipID() {
      return this.manipID;
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
      if ((getOpcode() & 0x0000000C) == 0) {
        // TAKE
        uuidReg = (insn    & 0b0000110000000000) >>> 10;
        destBuffer = (insn & 0b0000001111000000) >>> 6;
        destTail = (insn   & 0b0000000000100000) != 0;
        manipID = (insn    & 0b0000000000011111);
        totalCycles = 1;
      } else {
        this.illegalInstruction = true;
      }
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
  
  private int[][] uuidRawWords = new int[4][4];
  private UUID[] uuidRegister = new UUID[4];
  
  protected UUID readUUIDRegister(int index) {
    if (index < 0 || index >= 4) {
      throw new IllegalArgumentException("UUID register #" + index + " does not exist");
    }
    if (uuidRegister[index] == null) {
      // recompute UUID
      long lsb = ((long)uuidRawWords[index][0]) & 0x00000000FFFFFFFFL
          | ((long)uuidRawWords[index][1]) << 32;
      long msb = ((long)uuidRawWords[index][2]) & 0x00000000FFFFFFFFL
          | ((long)uuidRawWords[index][3]) << 32;
      uuidRegister[index] = new UUID(msb, lsb);
    }
    return uuidRegister[index];
  }
  
  protected void writeUUIDRegister(int addr, int value) {
    // figure out which register is being written to
    int registerIndex = ((addr & 0x000000F0) >>> 4) - 1;
    if (registerIndex >= 4) {
      throw new IllegalArgumentException("UUID register #" + registerIndex + " does not exist");
    }
    // figure out which word is being written to
    int wordIndex = (addr & 0x0000000F) / 4;
    uuidRawWords[registerIndex][wordIndex] = value;
    // invalidate constructed UUID
    uuidRegister[registerIndex] = null;
  }
  
  @Override
  public void writeWord(int pAddr, int value) throws AddressTrapException {
    int addr = translateAddress(pAddr);
    switch (addr) {
    case 0: // INV_CMD
      // TODO
      throw new AddressTrapException(7, pAddr);
    case 0x10: case 0x14: case 0x18: case 0x1C:
    case 0x20: case 0x24: case 0x28: case 0x2C:
    case 0x30: case 0x34: case 0x38: case 0x3C:
    case 0x40: case 0x44: case 0x48: case 0x4C:
      // INV_UUID
      writeUUIDRegister(addr, value); break;
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
    if ((cmd.getOpcode() & 0x0000000C) == 0) {
      // TAKE
      int mIdx = cmd.getManipID();
      ErrorCode errCode = machine.getManipulatorError(mIdx);
      if (errCode == ErrorCode.NO_ERROR) {
        // the manipulator should have an item available now;
        // take it and insert it into the destination buffer
        Item i = machine.takeManipulatorItem(mIdx);
        int dstBuffer = cmd.getDestBuffer();
        boolean dstTail = cmd.getDestTail();
        LinkedList<Item> dst = objectBuffers.get(dstBuffer);
        if (dstTail) {
          dst.addLast(i);
        } else {
          dst.addFirst(i);
        }
        return true;
      } else {
        error(errCode, cmd); return false;
      }
    } else {
      error(ErrorCode.ILLEGAL_INSN, cmd); return false;
    }
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
  
  protected boolean issueManipulatorCommand(Command cmd) {
    if (!cmd.isManipulatorCommand()) {
      throw new IllegalStateException("attempt to issue non-manipulator command as though it were one");
    }
    if (cmd.isIllegalInstruction()) {
      error(ErrorCode.ILLEGAL_INSN, cmd); return false;
    }
    if ((cmd.getOpcode() & 0x0000000C) == 0) {
      // TAKE
      int uuidReg = cmd.getUUIDReg();
      int mIdx = cmd.getManipID();
      int dstBuffer = cmd.getDestBuffer();
      // check that the destination buffer exists
      if (!objectBuffers.containsKey(dstBuffer)) {
        error(ErrorCode.NO_SUCH_BUFFER, cmd); return false;
      }
      // check that the destination buffer is not full
      LinkedList<Item> dst = objectBuffers.get(dstBuffer);
      if (dst.size() == MAXIMUM_OBJECTS_PER_BUFFER) {
        error(ErrorCode.OVERFLOW, cmd); return false;
      }
      // check that the specified manipulator exists
      if (mIdx >= machine.getNumberOfManipulators()) {
        error(ErrorCode.ILLEGAL_MANIP, cmd); return false;
      }
      UUID uuid = readUUIDRegister(uuidReg);
      if (machine.manipulator_getItemByUUID(mIdx, uuid)) {
        return true;
      } else {
        error(ErrorCode.MANIP_ERROR, cmd); return false;
      }
    } else {
      // TODO
      error(ErrorCode.ILLEGAL_INSN, cmd); return false;
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
        // TODO check whether manipulator commands can execute;
        // only one command per manipulator can be issued at a time
        if (commandCanExecute) {
          if (cmd_later.isManipulatorCommand() && !cmd_later.getManipulatorCommandIssued()) {
            // attempt to issue the command
            boolean status = issueManipulatorCommand(cmd_later);
            if (status) {
              // okay, the command was at least issued
              cmd_later.setManipulatorCommandIssued(true);
            } else {
              // failure, abort processing
              break;
            }
          } else if (!cmd_later.isManipulatorCommand() || cmd_later.getManipulatorCommandCompleted()){
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
    // check whether each manipulator command has completed
    for (int i = 0; i < numberOfCommands; ++i) {
      Command cmd = commandQueue[i];
      if (cmd.isManipulatorCommand() && cmd.getManipulatorCommandIssued()) {
        int manipID = cmd.getManipID();
        // check to see whether this manipulator is ready for a command
        boolean manipReady = machine.canAcceptManipulatorCommand(manipID);
        if (manipReady) {
          // this command has completed
          cmd.setManipulatorCommandCompleted(true);
        }
      }
    }
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
