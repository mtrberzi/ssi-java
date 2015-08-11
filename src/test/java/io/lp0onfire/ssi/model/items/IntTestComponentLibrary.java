package io.lp0onfire.ssi.model.items;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class IntTestComponentLibrary {

  @Before
  public void before() {
    ComponentLibrary.getInstance().clear();
  }
  
  @After
  public void after() {
    ComponentLibrary.getInstance().clear();
  }
  
  @Test
  public void inttestLoadComponents() throws URISyntaxException {
    // attempt to load components.xml out of resources
    URL compURL = ClassLoader.getSystemResource("components.xml");
    if (compURL == null) {
      fail("could not find resource 'components.xml'");
    }
    File compXML = new File(compURL.toURI());
    assertTrue(ComponentLibrary.getInstance().loadComponents(compXML));
  }
  
}
