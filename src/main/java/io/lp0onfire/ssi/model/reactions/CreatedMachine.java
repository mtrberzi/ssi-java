package io.lp0onfire.ssi.model.reactions;

import io.lp0onfire.ssi.microcontroller.Microcontroller;
import io.lp0onfire.ssi.microcontroller.peripherals.InventoryController;
import io.lp0onfire.ssi.model.Item;
import io.lp0onfire.ssi.model.Machine;
import io.lp0onfire.ssi.model.VoxelOccupant;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class CreatedMachine extends CreatedObject {

  private final String className;
  public String getClassName() {
    return this.className;
  }
  
  private final boolean requiresMCU;
  public boolean isMCURequired() {
    return this.requiresMCU;
  }
  
  public CreatedMachine(String className, boolean requiresMCU) {
    this.className = className;
    this.requiresMCU = requiresMCU;
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
      Class<? extends Machine> machineClass = Class.forName(className).asSubclass(Machine.class);
      Machine machine;
      
      // if we require a microcontroller, get a constructor of the form Machine(Microcontroller)
      // if we do not require a microcontroller, get a constructor of the form Machine()

      if (isMCURequired()) {
        Microcontroller mcu = buildMicrocontroller();
        Constructor<? extends Machine> ctor = machineClass.getDeclaredConstructor(Microcontroller.class);
        machine = ctor.newInstance(mcu);
        // attach peripherals
        InventoryController inv = new InventoryController(machine, 4); // TODO number of buffers from template or something
        mcu.attachInventoryController(inv);
      } else {
        Constructor<? extends Machine> ctor = machineClass.getDeclaredConstructor();
        machine = ctor.newInstance();
      }
      // finally
      return machine;
    } catch (ClassNotFoundException e) {
      throw new IllegalArgumentException("internal error: machine class '" + className + "' not found");
    } catch (NoSuchMethodException e) {
      throw new IllegalArgumentException("internal error: machine class '" + className + "' does not have a suitable constructor");
    } catch (InstantiationException e) {
      throw new IllegalArgumentException("internal error: machine class '" + className + "' could not be instantiated");
    } catch (IllegalAccessException e) {
      throw new IllegalArgumentException("internal error: machine class '" + className + "' not accessible");
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("internal error: machine class '" + className + "' illegal argument");
    } catch (InvocationTargetException e) {
      throw new IllegalArgumentException("internal error: machine class '" + className + "' threw an exception during invocation");
    }
  }
  
}
