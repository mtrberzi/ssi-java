package io.lp0onfire.ssi;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import io.lp0onfire.ssi.model.Material;
import io.lp0onfire.ssi.model.MaterialLibrary;
import io.lp0onfire.ssi.model.items.ComponentBuilder;
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
  
  private void load() {
    try {
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
    } catch (URISyntaxException e) {
      fail("unexpected URISyntaxException: " + e.getMessage());
    }
  }
  
  @Test
  public void inttestLoadResources() {
    load();
  }
  
  @Test
  public void inttestMaterials_NoDuplicateIDs() {
    load();
    // for each pair of materials M and N, if M != N then M.getType() != N.getType()
    ArrayList<Material> materials = new ArrayList<>(MaterialLibrary.getInstance().getAllMaterials());
    for (int i = 0; i < materials.size(); ++i) {
      for (int j = 0; j < materials.size(); ++j) {
        if (i == j) continue;
        Material m = materials.get(i);
        Material n = materials.get(j);
        assertTrue("duplicate material type ID: m=" + m.getName() + ", n=" + n.getName(), m.getType() != n.getType());
      }
    }
  }
  
  @Test
  public void inttestComponents_NoDuplicateIDs() {
    load();
    // for each pair of components M and N, if M != N then M.getType() != N.getType()
    ArrayList<ComponentBuilder> components = new ArrayList<>(ComponentLibrary.getInstance().getAllComponents());
    for (int i = 0; i < components.size(); ++i) {
      for (int j = 0; j < components.size(); ++j) {
        if (i == j) continue;
        ComponentBuilder m = components.get(i);
        ComponentBuilder n = components.get(j);
        assertTrue("duplicate component type ID: m=" + m.getComponentName() + ", n=" + n.getComponentName(),
            m.getType() != n.getType());
      }
    }
  }
  
}
