package io.lp0onfire.ssi.microcontroller.peripherals;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.UUID;

import io.lp0onfire.ssi.microcontroller.AddressTrapException;
import io.lp0onfire.ssi.microcontroller.InterruptSource;
import io.lp0onfire.ssi.microcontroller.RV32SystemBus;
import io.lp0onfire.ssi.microcontroller.SystemBusPeripheral;
import io.lp0onfire.ssi.model.Item;
import io.lp0onfire.ssi.model.Machine;
import io.lp0onfire.ssi.model.reactions.Reaction;
import io.lp0onfire.ssi.model.reactions.ReactionLibrary;

public class InventoryController implements SystemBusPeripheral, InterruptSource {

  private boolean tracing = false;
  public void setTracing(boolean b) {
    this.tracing = b;
  }
  private void trace(String s) {
    if (tracing) {
      System.err.println(s);
    }
  }
  
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
  
  private RV32SystemBus bus;
  
  @Override
  public void setSystemBus(RV32SystemBus bus) {
    this.bus = bus;
  }
  
  public enum ErrorCode {
    NO_ERROR(0),
    ILLEGAL_INSN(1),
    UNDERFLOW(2),
    OVERFLOW(3),
    NO_SUCH_BUFFER(4),
    ILLEGAL_MANIP(5),
    MANIP_ERROR(6),
    NO_SUCH_RX(7),
    ILLEGAL_RX(8),
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
    
    public boolean isQueryCommand() {
      return (getType() == 1);
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
    
    private int rxRegister = 0;
    public int getRxRegister() {
      return this.rxRegister;
    }
    
    private int responseRegister = 0;
    public int getResponseRegister() {
      return this.responseRegister;
    }
    
    private ByteBuffer responseBuffer = null;
    public ByteBuffer getResponseBuffer() {
      return this.responseBuffer;
    }
    
    private int currentAddress;
    public int getCurrentAddress() {
      return this.currentAddress;
    }
    public void setCurrentAddress(int addr) {
      this.currentAddress = addr;
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
      switch (opcode) {
      case 0: // LIST
        responseRegister = (insn & 0b0000001111100000) >>> 5;
        sourceBuffer = (insn &     0b0000000000011110) >>> 1;
        sourceTail = (insn &       0b0000000000000001) != 0;
        totalCycles = 32;
        break;
      case 1: // MSTAT
        responseRegister = (insn & 0b0000001111100000) >>> 5;
        manipID = (insn &          0b0000000000011111);
        totalCycles = 3;
        break;
      default:
        this.illegalInstruction = true;
      }
    }
    
    private void type2Init() {
      if ((getOpcode() & 0x0000000C) == 0) {
        // TAKE
        uuidReg = (insn    & 0b0000110000000000) >>> 10;
        destBuffer = (insn & 0b0000001111000000) >>> 6;
        destTail = (insn   & 0b0000000000100000) != 0;
        manipID = (insn    & 0b0000000000011111);
        totalCycles = 1;
      } else if (getOpcode() == 0b0100) {
        // GIVE
        sourceBuffer = (insn & 0b0000001111000000) >>> 6;
        sourceTail = (insn     & 0b0000000000100000) != 0;
        manipID = (insn      & 0b0000000000011111);
        totalCycles = 1;
      } else if (getOpcode() == 0b0101) {
        // NEXT
        destBuffer = (insn & 0b0000001111000000) >>> 6;
        destTail = (insn   & 0b0000000000100000) != 0;
        manipID = (insn    & 0b0000000000011111);
        totalCycles = 1;
      } else {
        this.illegalInstruction = true;
      }
    }
    
    private void type3Init() {
      switch (opcode) {
      case 0:
        // SET
        rxRegister = (insn & 0b0000001111100000) >>> 5;
        manipID = (insn    & 0b0000000000011111);
        totalCycles = 3;
        break;
      default:
        this.illegalInstruction = true;
      }
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
  
  private int[] reactionIDRegister = new int[32];
  protected void writeReactionIDRegister(int addr, int value) {
    // first register is at 0x50
    int registerIndex = (addr - 0x50) / 4;
    if (registerIndex < 0 || registerIndex >= 31) {
      throw new IllegalArgumentException("reaction ID register #" + registerIndex + " does not exist");
    }
    reactionIDRegister[registerIndex] = value;
  }
  
  private int[] responseAddressRegister = new int[32];
  protected void writeResponseAddressRegister(int addr, int value) {
    // first register is at 0xD0
    int registerIndex = (addr - 0xD0) / 4;
    if (registerIndex < 0 || registerIndex >= 31) {
      throw new IllegalArgumentException("response address register #" + registerIndex + " does not exist");
    }
    responseAddressRegister[registerIndex] = value;
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
    case 0x50: case 0x54: case 0x58: case 0x5C:
    case 0x60: case 0x64: case 0x68: case 0x6C:
    case 0x70: case 0x74: case 0x78: case 0x7C:
    case 0x80: case 0x84: case 0x88: case 0x8C:
    case 0x90: case 0x94: case 0x98: case 0x9C:
    case 0xA0: case 0xA4: case 0xA8: case 0xAC:
    case 0xB0: case 0xB4: case 0xB8: case 0xBC:
    case 0xC0: case 0xC4: case 0xC8: case 0xCC:
      // INV_RX
      writeReactionIDRegister(addr, value); break;
    case 0x0D0: case 0x0D4: case 0x0D8: case 0x0DC:
    case 0x0E0: case 0x0E4: case 0x0E8: case 0x0EC:
    case 0x0F0: case 0x0F4: case 0x0F8: case 0x0FC:
    case 0x100: case 0x104: case 0x108: case 0x10C:
    case 0x110: case 0x114: case 0x118: case 0x11C:
    case 0x120: case 0x124: case 0x128: case 0x12C:
    case 0x130: case 0x134: case 0x138: case 0x13C:
    case 0x140: case 0x144: case 0x148: case 0x14C:
      // INV_RESPADDR
      writeResponseAddressRegister(addr, value); break;
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
    switch (cmd.getOpcode()) {
    case 0: // LIST
    {
      int srcBuffer = cmd.getSourceBuffer();
      boolean srcTail = cmd.getSourceTail();
      if (!objectBuffers.containsKey(srcBuffer)) {
        error(ErrorCode.NO_SUCH_BUFFER, cmd); return false;
      }
      
      // find target address
      cmd.currentAddress = responseAddressRegister[cmd.getResponseRegister()];
      
      LinkedList<Item> src = objectBuffers.get(srcBuffer);
      ByteBuffer responseBuffer = ByteBuffer.allocate(4 + src.size()*8);
      responseBuffer.order(ByteOrder.LITTLE_ENDIAN);
      cmd.responseBuffer = responseBuffer;
      // write out the header
      responseBuffer.putShort((short)src.size()); // number of objects
      responseBuffer.putShort((short)8); // record size
      ListIterator<Item> iterator;
      if (srcTail) {
        // start at end
        iterator = src.listIterator(src.size());
      } else {
        iterator = src.listIterator(0);
      }
      // now iterate over it
      while (
          (srcTail && iterator.hasPrevious())
          ||
          (!srcTail && iterator.hasNext())
          ) {
        Item obj;
        if (srcTail) {
          obj = iterator.previous();
        } else {
          obj = iterator.next();
        }
        // write object kind
        responseBuffer.putShort(obj.getKind());
        // write object flags
        // TODO
        responseBuffer.putShort((short)0);
        // write object type
        responseBuffer.putInt(obj.getType());
      }
      responseBuffer.position(0);
      return true;
    }
    case 1: // MSTAT
    {
      int mIdx = cmd.getManipID();
      if (mIdx >= machine.getNumberOfManipulators()) {
        error(ErrorCode.ILLEGAL_MANIP, cmd); return false;
      }
      ByteBuffer response = machine.manipulator_MSTAT(mIdx);
      if (response == null) {
        error(ErrorCode.MANIP_ERROR, cmd); return false;
      }
      response.position(0);
      cmd.responseBuffer = response;
      cmd.currentAddress = responseAddressRegister[cmd.getResponseRegister()];
      return true;
    }
    default:
      error(ErrorCode.ILLEGAL_INSN, cmd); return false;
    }
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
    } else if (cmd.getOpcode() == 0b0100) {
      // GIVE
      int mIdx = cmd.getManipID();
      ErrorCode errCode = machine.getManipulatorError(mIdx);
      if (errCode == ErrorCode.NO_ERROR) {
        // the manipulator has accepted the object, so get it out of the source buffer
        int srcBuffer = cmd.getSourceBuffer();
        boolean srcTail = cmd.getSourceTail();
        LinkedList<Item> src = objectBuffers.get(srcBuffer);
        if (srcTail) {
          src.removeLast();
        } else {
          src.removeFirst();
        }
        return true;
      } else {
        error(errCode, cmd); return false;
      }
    } else if (cmd.getOpcode() == 0b0101) {
      // NEXT
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
    switch (cmd.getOpcode()) {
    case 0: // SET
    {
      int mIdx = cmd.getManipID();
      // check that the specified manipulator exists
      if (mIdx >= machine.getNumberOfManipulators()) {
        error(ErrorCode.ILLEGAL_MANIP, cmd); return false;
      }
      // check that the specified manipulator can accept SET commands
      if (!machine.manipulator_canSetReaction(mIdx)) {
        error(ErrorCode.MANIP_ERROR, cmd); return false;
      }
      // try to find a reaction with this ID
      int reactionID = reactionIDRegister[cmd.getRxRegister()];
      Reaction reaction = ReactionLibrary.getInstance().getReactionByID(reactionID);
      if (reaction == null) {
        error(ErrorCode.NO_SUCH_RX, cmd); return false;
      }
      // tell the manipulator to do this reaction now
      if (!machine.manipulator_setReaction(mIdx, reaction)) {
        error(ErrorCode.ILLEGAL_RX, cmd); return false;
      }
      return true;
    }
    default:
      error(ErrorCode.ILLEGAL_INSN, cmd); return false;
    }
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
    } else if (cmd.getOpcode() == 0b0100) {
      // GIVE
      int mIdx = cmd.getManipID();
      int srcBuffer = cmd.getSourceBuffer();
      boolean srcTail = cmd.getSourceTail();
      // check that the source buffer exists
      if (!objectBuffers.containsKey(srcBuffer)) {
        error(ErrorCode.NO_SUCH_BUFFER, cmd); return false;
      }
      // check that the source buffer is not empty
      LinkedList<Item> src = objectBuffers.get(srcBuffer);
      if (src.size() == 0) {
        error(ErrorCode.UNDERFLOW, cmd); return false;
      }
      // check that the specified manipulator exists
      if (mIdx >= machine.getNumberOfManipulators()) {
        error(ErrorCode.ILLEGAL_MANIP, cmd); return false;
      }
      Item i;
      if (srcTail) {
        i = src.peekLast();
      } else {
        i = src.peekFirst();
      }
      if (machine.manipulator_putItem(mIdx, i)) {
        return true;
      } else {
        error(ErrorCode.MANIP_ERROR, cmd); return false;
      }
    } else if (cmd.getOpcode() == 0b0101) {
      // NEXT
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
      if (machine.manipulator_getNextItem(mIdx)) {
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
        trace("check command #" + i);
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
          trace("executing command #" + i);
          if (cmd_later.isQueryCommand()) {
            trace("cycling query command");
            cmd_later.cycle();
            if (cmd_later.getExecutedCycles() >= cmd_later.getTotalCycles()) {
              if (cmd_later.getResponseBuffer() == null) {
                trace("cycle count up, constructing response buffer");
                boolean status = execute(cmd_later);
                if (!status) {
                  trace("fail");
                  // abort processing
                  break;
                }
              } else {
                // we already have a response -- do one DMA cycle
                trace("DMA cycle");
                ByteBuffer responseBuffer = cmd_later.getResponseBuffer();
                if (responseBuffer.position() == responseBuffer.capacity()) {
                  // all done
                  trace("finished DMA");
                  commandQueue[i] = null;
                } else {
                  int tmp = responseBuffer.getInt();
                  try {
                    bus.storeWord(cmd_later.getCurrentAddress(), tmp);
                    cmd_later.setCurrentAddress(cmd_later.getCurrentAddress() + 4);
                  } catch (AddressTrapException e) {
                    // failure
                    trace("address exception");
                    break;
                  }
                }
              }
            }
          } else if (cmd_later.isManipulatorCommand() && !cmd_later.getManipulatorCommandIssued()) {
            trace("attempting to issue manipulator command...");
            // attempt to issue the command
            boolean status = issueManipulatorCommand(cmd_later);
            if (status) {
              trace("success");
              // okay, the command was at least issued
              cmd_later.setManipulatorCommandIssued(true);
            } else {
              trace("failed");
              // failure, abort processing
              break;
            }
          } else if (!cmd_later.isManipulatorCommand() || cmd_later.getManipulatorCommandCompleted()) {
            trace("cycling command");
            cmd_later.cycle();
            if (cmd_later.getExecutedCycles() >= cmd_later.getTotalCycles()) {
              trace("cycle count up, executing command");
              // execute command
              boolean status = execute(cmd_later);
              if (status) {
                trace("success");
                // success, clean up
                commandQueue[i] = null;
              } else {
                trace("fail");
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
