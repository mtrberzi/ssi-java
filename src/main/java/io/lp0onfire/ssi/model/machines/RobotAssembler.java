package io.lp0onfire.ssi.model.machines;

import io.lp0onfire.ssi.microcontroller.Microcontroller;
import io.lp0onfire.ssi.model.ControlledTransportEndpoint;
import io.lp0onfire.ssi.model.Vector;
import io.lp0onfire.ssi.model.Machine.ManipulatorType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * A Robot Assembler is a microcontrolled machine that builds new robots out of frames and parts.
 * It only has a single fabrication component and can only build simple kinds of robots.
 *
 * Endpoints: input0 (manipulator #0), input1 (manipulator #1), input2 (manipulator #2)
 * Manipulator #3 is the robot assembler, which builds robots.
 */
public class RobotAssembler extends ControlledTransportEndpoint {

  @Override
  public Integer getManipulatorIndexOfEndpoint(String endpoint) {
    if (endpoint.equals("input0")) return 0;
    else if (endpoint.equals("input1")) return 1;
    else if (endpoint.equals("input2")) return 2;
    else return null; 
  }

  @Override
  public String getEndpointOfManipulatorIndex(Integer mIdx) {
    switch (mIdx) {
    case 0: return "input0";
    case 1: return "input1";
    case 2: return "input2";
    default: return null;
    }
  }

  @Override
  public boolean endpointCanReceive(String endpoint) {
    return (endpoint.equals("input0") || endpoint.equals("input1") || endpoint.equals("input2"));
  }

  @Override
  public boolean endpointCanSend(String endpoint) {
    return false;
  }

  @Override
  public Set<String> getTransportEndpoints() {
    return new HashSet<>(Arrays.asList("input0", "input1", "input2"));
  }

  @Override
  public int getNumberOfManipulators() {
    return 4;
  }

  @Override
  public ManipulatorType getManipulatorType(int mIdx) {
    switch (mIdx) {
    case 0: case 1: case 2:
      return ManipulatorType.TRANSPORT_TUBE_ENDPOINT;
    case 3:
      return ManipulatorType.ROBOT_ASSEMBLER;
    default: return null;
    }
  }

  @Override
  public int getType() {
    return 4;
  }

  @Override
  public Vector getExtents() {
    return new Vector(1, 1, 1);
  }

  private static final Integer nObjectBuffers = 4;

  public RobotAssembler(Microcontroller mcu) {
    super(mcu);
    makeInventoryController(nObjectBuffers);
  }
  
}
