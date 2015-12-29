package io.lp0onfire.ssi.model.reactions;

import io.lp0onfire.ssi.microcontroller.Microcontroller;
import io.lp0onfire.ssi.microcontroller.peripherals.EngineController;
import io.lp0onfire.ssi.microcontroller.peripherals.InventoryController;
import io.lp0onfire.ssi.model.Item;
import io.lp0onfire.ssi.model.Robot;
import io.lp0onfire.ssi.model.VoxelOccupant;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class CreatedRobot extends CreatedObject {

  private final String className;
  public String getClassName() {
    return this.className;
  }
  
  public CreatedRobot(String className) {
    this.className = className;
  }

  private Microcontroller buildMicrocontroller() {
    // just hardcode a bunch of safe defaults for now
    // TODO take this from a reactant, or something
    int textMemoryPages = 8;
    int dataMemoryPages = 8;
    Microcontroller mcu = new Microcontroller(textMemoryPages, dataMemoryPages);
    return mcu;
  }
  
  @Override
  public VoxelOccupant createObject(List<List<Item>> reactants) {
    try {
      Class<? extends Robot> robotClass = Class.forName(className).asSubclass(Robot.class);
      Microcontroller mcu = buildMicrocontroller();
      // instantiate a constructor of the form Robot(Microcontroller mcu)
      Constructor<? extends Robot> ctor = robotClass.getDeclaredConstructor(Microcontroller.class);
      Robot robot = ctor.newInstance(mcu);
      // attach peripherals
      EngineController ecu = new EngineController(robot);
      mcu.attachPeripheral(ecu, 0x4A000000);
      InventoryController inv = new InventoryController(robot, 4); // TODO number of buffers from template or something
      mcu.attachInventoryController(inv);
      // finally
      return robot;
    } catch (ClassNotFoundException e) {
      throw new IllegalArgumentException("internal error: robot class '" + className + "' not found");
    } catch (NoSuchMethodException e) {
      throw new IllegalArgumentException("internal error: robot class '" + className + "' does not have a suitable constructor");
    } catch (InstantiationException e) {
      throw new IllegalArgumentException("internal error: robot class '" + className + "' could not be instantiated");
    } catch (IllegalAccessException e) {
      throw new IllegalArgumentException("internal error: robot class '" + className + "' not accessible");
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("internal error: robot class '" + className + "' illegal argument");
    } catch (InvocationTargetException e) {
      throw new IllegalArgumentException("internal error: robot class '" + className + "' threw an exception during invocation");
    }
  }
  
}
