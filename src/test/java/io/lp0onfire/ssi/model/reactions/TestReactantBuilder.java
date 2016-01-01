package io.lp0onfire.ssi.model.reactions;

import static org.junit.Assert.*;
import io.lp0onfire.ssi.ParseException;
import io.lp0onfire.ssi.model.items.ComponentBuilder;
import io.lp0onfire.ssi.model.items.ComponentLibrary;

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestReactantBuilder {

  @Before
  public void setup() {
    ComponentLibrary.getInstance().clear();
  }
  
  @After
  public void finish() {
    ComponentLibrary.getInstance().clear();
  }
  
  private void createTestComponents() {
    ComponentBuilder cBuilder = new ComponentBuilder();
    cBuilder.setComponentName("bogus1");
    cBuilder.setType(0);
    try {
      ComponentLibrary.getInstance().addComponent(cBuilder);
    } catch (ParseException e) {
      fail("failed to add component to library: " + e.getMessage());
    }
  }
  
  @Test
  public void testBuild() {
    createTestComponents();
    
    ReactantBuilder rBuilder = new ReactantBuilder();
    rBuilder.setQuantity(2);
    rBuilder.setComponentName("bogus1");
    rBuilder.setConstraints(Arrays.asList(new MaterialCategoryConstraint("metal")));
    Reactant r = rBuilder.build();
    assertNotNull(r);
  }
  
  @Test
  public void testBuild_ComponentDoesNotExist() {
    // building this reaction should fail because we haven't created a component called 'bogus1'
    ReactantBuilder rBuilder = new ReactantBuilder();
    rBuilder.setQuantity(2);
    rBuilder.setComponentName("bogus1");
    rBuilder.setConstraints(Arrays.asList(new MaterialCategoryConstraint("metal")));
    
    try {
      rBuilder.build();
      fail("building reactant appeared to be successful, but an error should have been caught");
    } catch (IllegalArgumentException e) {
      String msg = e.getMessage();
      assertTrue("message does not mention component 'bogus1'", msg.contains("bogus1"));
    }
  }
  
}
