package io.lp0onfire.ssi.model.machines;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import io.lp0onfire.ssi.microcontroller.Microcontroller;
import io.lp0onfire.ssi.microcontroller.peripherals.InventoryController;
import io.lp0onfire.ssi.model.ControlledTransportEndpoint;
import io.lp0onfire.ssi.model.Vector;

/**
 * A Robot Part Builder is a microcontrolled machine that can create basic robot parts,
 * including robot frames, out of components.
 * Unlike the Rapid Fabricator, it is fully programmable.
 * However, its efficiency is limited by the fact that it only has a single fabrication component.
 *
 * Endpoints: input0 (manipulator #0), input1 (manipulator #1), output0 (manipulator #2), output1 (manipulator #3)
 * Manipulator #4 is the part builder, which is responsible for part and frame fabrication.
 */
public class RobotPartBuilder extends ControlledTransportEndpoint {

  @Override
  public Integer getManipulatorIndexOfEndpoint(String endpoint) {
    if (endpoint.equals("input0")) return 0;
    else if (endpoint.equals("input1")) return 1;
    else if (endpoint.equals("output0")) return 2;
    else if (endpoint.equals("output1")) return 3;
    else return null; 
  }

  @Override
  public String getEndpointOfManipulatorIndex(Integer mIdx) {
    switch (mIdx) {
    case 0: return "input0";
    case 1: return "input1";
    case 2: return "output0";
    case 3: return "output1";
    default: return null;
    }
  }

  @Override
  public boolean endpointCanReceive(String endpoint) {
    return (endpoint.equals("input0") || endpoint.equals("input1"));
  }

  @Override
  public boolean endpointCanSend(String endpoint) {
    return (endpoint.equals("output0") || endpoint.equals("output1"));
  }

  @Override
  public Set<String> getTransportEndpoints() {
    return new HashSet<>(Arrays.asList("input0", "input1", "output0", "output1"));
  }

  @Override
  public int getNumberOfManipulators() {
    return 5;
  }

  @Override
  public ManipulatorType getManipulatorType(int mIdx) {
    switch (mIdx) {
    case 0: case 1: case 2: case 3:
      return ManipulatorType.TRANSPORT_TUBE_ENDPOINT;
    case 4:
      return ManipulatorType.PART_BUILDER;
    default: return null;
    }
  }

  @Override
  public int getType() {
    return 3;
  }

  @Override
  public Vector getExtents() {
    return new Vector(1, 1, 1);
  }

  private static final Integer nObjectBuffers = 4;

  public RobotPartBuilder(Microcontroller mcu) {
    super(mcu);
    makeInventoryController(nObjectBuffers);
  }
  
}
