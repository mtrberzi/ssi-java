package io.lp0onfire.ssi.model.reactions;

public class CreatedMachineBuilder {

  private String machineClass = null;
  public void setMachineClass(String c) {
    this.machineClass = c;
  }
  
  private Boolean requiresMCU = null;
  public void setRequiresMCU(Boolean b) {
    this.requiresMCU = b;
  }
 
  public CreatedMachine build() {
    if (machineClass == null) {
      throw new IllegalArgumentException("machine classname not set");
    }
    
    if (requiresMCU == null) {
      throw new IllegalArgumentException("machine MCU requirement not set");
    }
    
    return new CreatedMachine(machineClass, requiresMCU);
  }
  
}
