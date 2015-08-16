package io.lp0onfire.ssi;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import io.lp0onfire.ssi.model.MaterialLibrary;
import io.lp0onfire.ssi.model.items.ComponentLibrary;
import io.lp0onfire.ssi.model.reactions.ReactionLibrary;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class IntTestLoadResources {

  @Before
  public void before() {
    MaterialLibrary.getInstance().clear();
    ComponentLibrary.getInstance().clear();
    ReactionLibrary.getInstance().clear();
  }
  
  @After
  public void after() {
    MaterialLibrary.getInstance().clear();
    ComponentLibrary.getInstance().clear();
    ReactionLibrary.getInstance().clear();
  }
  
  @Test
  public void inttestLoadResources() throws URISyntaxException {
    URL matURL = ClassLoader.getSystemResource("materials.xml");
    if (matURL == null) {
      fail("could not find resource 'materials.xml'");
    }
    File matXML = new File(matURL.toURI());
    assertTrue(MaterialLibrary.getInstance().loadMaterials(matXML));
    
    URL compURL = ClassLoader.getSystemResource("components.xml");
    if (compURL == null) {
      fail("could not find resource 'components.xml'");
    }
    File compXML = new File(compURL.toURI());
    assertTrue(ComponentLibrary.getInstance().loadComponents(compXML));
    
    URL rxURL = ClassLoader.getSystemResource("reactions.xml");
    if (rxURL == null) {
      fail("could not find resource 'reactions.xml'");
    }
    File rxXML = new File(rxURL.toURI());
    assertTrue(ReactionLibrary.getInstance().loadReactions(rxXML));
  }
  
}
