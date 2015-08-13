package io.lp0onfire.ssi.model;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class IntTestMaterialLibrary {

  @Before
  public void before() {
    MaterialLibrary.getInstance().clear();
  }
  
  @After
  public void after() {
    MaterialLibrary.getInstance().clear();
  }
  
  @Test
  public void inttestLoadMaterials() throws URISyntaxException {
    // TODO there may be a dependency on components and materials,
    // so it might make more sense to integration test whatever
    // "load game resources" method we end up with
    // attempt to load reactions.xml out of resources
    URL rxURL = ClassLoader.getSystemResource("materials.xml");
    if (rxURL == null) {
      fail("could not find resource 'materials.xml'");
    }
    File rxXML = new File(rxURL.toURI());
    assertTrue(MaterialLibrary.getInstance().loadMaterials(rxXML));
  }
  
}
