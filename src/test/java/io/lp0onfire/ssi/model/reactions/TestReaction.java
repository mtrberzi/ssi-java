package io.lp0onfire.ssi.model.reactions;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import io.lp0onfire.ssi.ParseException;
import io.lp0onfire.ssi.model.Item;
import io.lp0onfire.ssi.model.Material;
import io.lp0onfire.ssi.model.MaterialBuilder;
import io.lp0onfire.ssi.model.items.Component;
import io.lp0onfire.ssi.model.items.ComponentBuilder;
import io.lp0onfire.ssi.model.items.ComponentLibrary;

public class TestReaction {

  private static Material metal;
  private static Material nonmetal;
  
  @BeforeClass
  public static void setupclass() throws ParseException {
    // create a fake metal and non-metal material
    MaterialBuilder metalBuilder = new MaterialBuilder();
    metalBuilder.setMaterialName("bogusite");
    metalBuilder.setCategories(Arrays.asList("metal"));
    metal = metalBuilder.build();
    
    MaterialBuilder nonMetalBuilder = new MaterialBuilder();
    nonMetalBuilder.setMaterialName("fakelite");
    nonmetal = nonMetalBuilder.build();
    
    ComponentBuilder cBuilder1 = new ComponentBuilder();
    cBuilder1.setComponentName("bogus1");
    ComponentLibrary.getInstance().addComponent(cBuilder1);
    
    ComponentBuilder cBuilder2 = new ComponentBuilder();
    cBuilder2.setComponentName("bogus2");
    ComponentLibrary.getInstance().addComponent(cBuilder2);
  }
  
  /*
   * our test reaction is:
   * 2 [metal] bogus1 -> 3 [#0] bogus2
   */
  private Reaction buildTestReaction() {
    List<Reactant> reactants = new LinkedList<>();
    ReactantBuilder rBuilder = new ReactantBuilder();
    rBuilder.setQuantity(2);
    rBuilder.setComponentName("bogus1");
    rBuilder.setConstraints(Arrays.asList(new MaterialCategoryConstraint("metal")));
    reactants.add(rBuilder.build());
    
    List<Product> products = new LinkedList<>();
    ProductBuilder pBuilder = new ProductBuilder();
    pBuilder.setQuantity(3);
    pBuilder.setComponentName("bogus2");
    pBuilder.setCopiedMaterial(0);
    products.add(pBuilder.build());
    
    ReactionBuilder reactionBuilder = new ReactionBuilder();
    reactionBuilder.setReactionName("fabricate bogus2");
    reactionBuilder.setReactionTime(1);
    reactionBuilder.setCategories(Arrays.asList("test"));
    reactionBuilder.setReactants(reactants);
    reactionBuilder.setProducts(products);
    return reactionBuilder.build();
  }
  
  @Test
  public void testBuildReaction() {
    Reaction rx = buildTestReaction();
    assertNotNull(rx);
  }
  
  @Test
  public void testMatchReactants() {
    Reaction rx = buildTestReaction();
    List<Item> reactants = new LinkedList<>();
    for (int i = 0; i < 2; ++i) {
      reactants.add(ComponentLibrary.getInstance().createComponent("bogus1", metal));
    }
    assertTrue(rx.reactantsOK(reactants));
  }
  
  @Test
  public void testMatchReactants_OverQuantity() {
    Reaction rx = buildTestReaction();
    List<Item> reactants = new LinkedList<>();
    for (int i = 0; i < 3; ++i) {
      reactants.add(ComponentLibrary.getInstance().createComponent("bogus1", metal));
    }
    assertTrue(rx.reactantsOK(reactants));
  }
  
  @Test
  public void testMatchReactants_IncorrectMaterial() {
    Reaction rx = buildTestReaction();
    List<Item> reactants = new LinkedList<>();
    for (int i = 0; i < 2; ++i) {
      reactants.add(ComponentLibrary.getInstance().createComponent("bogus1", nonmetal));
    }
    assertFalse(rx.reactantsOK(reactants));
  }
  
  @Test
  public void testMatchReactants_IncorrectQuantity() {
    Reaction rx = buildTestReaction();
    List<Item> reactants = new LinkedList<>();
    for (int i = 0; i < 1; ++i) {
      reactants.add(ComponentLibrary.getInstance().createComponent("bogus1", metal));
    }
    assertFalse(rx.reactantsOK(reactants));
  }
  
  @Test
  public void testMatchReactants_IncorrectQuantityAfterConstraints() {
    Reaction rx = buildTestReaction();
    List<Item> reactants = new LinkedList<>();
    for (int i = 0; i < 1; ++i) {
      reactants.add(ComponentLibrary.getInstance().createComponent("bogus1", metal));
    }
    for (int i = 0; i < 1; ++i) {
      reactants.add(ComponentLibrary.getInstance().createComponent("bogus1", nonmetal));
    }
    assertFalse(rx.reactantsOK(reactants));
  }
  
  @Test
  public void testReact() {
    Reaction rx = buildTestReaction();
    List<Item> reactants = new LinkedList<>();
    for (int i = 0; i < 2; ++i) {
      reactants.add(ComponentLibrary.getInstance().createComponent("bogus1", metal));
    }
    
    Reaction.Result rxResult = rx.react(reactants);
    assertTrue(rxResult.successful());
    List<Item> products = rxResult.getOutputProducts();
    for (Item i : products) {
      assertTrue(i instanceof Component);
      Component c = (Component)i;
      assertEquals(metal, c.getMaterial());
      assertEquals("bogus2", c.getComponentName());
    }
  }
  
}
