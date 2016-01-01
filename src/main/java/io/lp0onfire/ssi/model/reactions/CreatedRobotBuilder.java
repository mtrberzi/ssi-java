package io.lp0onfire.ssi.model.reactions;

public class CreatedRobotBuilder {

  private String robotClass = null;
  public void setRobotClass(String c) {
    this.robotClass = c;
  }
 
  public CreatedRobot build() {
    if (robotClass == null) {
      throw new IllegalArgumentException("robot classname not set");
    }
    
    return new CreatedRobot(robotClass);
  }
  
}
