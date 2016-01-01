package io.lp0onfire.ssi.model.reactions;

import io.lp0onfire.ssi.ParseException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ReactionLibrary {

  private static ReactionLibrary inst = null;
  public static ReactionLibrary getInstance() {
    if (inst == null) {
      inst = new ReactionLibrary();
    }
    return inst;
  }
  
  private ReactionLibrary() {
    reactions = new HashMap<>();
    reactionsByID = new HashMap<>();
  }
  
  // maps reaction categories to every reaction in that category
  private Map<String, List<Reaction>> reactions;
  
  // maps reaction ID to reaction
  private Map<Integer, Reaction> reactionsByID;
  
  public void clear() {
    reactions.clear();
    reactionsByID.clear();
  }
  
  public void addReaction(String category, Reaction reaction) {    
    if (!reactions.containsKey(category)) {
      reactions.put(category, new LinkedList<Reaction>());
    }
    List<Reaction> rxs = reactions.get(category);
    rxs.add(reaction);
    
    reactionsByID.put(reaction.getID(), reaction);
  }
  
  public Reaction getReactionByID(Integer id) {
    return reactionsByID.get(id);
  }
  
  private List<String> parseCategories(Node categoriesNode) throws ParseException {
    // each element should be <category name="ABC"/>
    List<String> categories = new LinkedList<>();
    NodeList children = categoriesNode.getChildNodes();
    for (int i = 0; i < children.getLength(); ++i) {
      Node subnode = children.item(i);
      if (subnode.getNodeType() == Node.ELEMENT_NODE) {
        if (subnode.getNodeName().equals("category")) {
          NamedNodeMap categoryAttrs = subnode.getAttributes();
          Node nameNode = categoryAttrs.getNamedItem("name");
          if (nameNode != null) {
            categories.add(nameNode.getNodeValue());
          } else {
            throw new ParseException("category missing 'name' attribute");
          }
        }
      }
    }
    return categories;
  }
  
  private List<ReactantConstraint> parseReactantConstraints(Node constraints) throws ParseException {
    List<ReactantConstraint> results = new LinkedList<>();
    NodeList subnodes = constraints.getChildNodes();
    for (int i = 0; i < subnodes.getLength(); ++i) {
      Node constraint = subnodes.item(i);
      if (constraint.getNodeType() == Node.ELEMENT_NODE) {
        if (constraint.getNodeName().equals("materialCategoryConstraint")) {
          Node category = constraint.getAttributes().getNamedItem("category");
          if (category != null) {
            MaterialCategoryConstraint c = new MaterialCategoryConstraint(category.getNodeValue());
            results.add(c);
          } else {
            throw new ParseException("materialCategoryConstraint must contain category attribute");
          }
        } else {
          throw new ParseException("unknown reactant constraint: " + constraint.toString());
        }
      }
    }
    return results;
  }
  
  private Reactant parseReactant(Node reactantNode) throws ParseException {
    ReactantBuilder builder = new ReactantBuilder();
    
    NamedNodeMap attrs = reactantNode.getAttributes();
    for (int i = 0; i < attrs.getLength(); ++i) {
      Node attr = attrs.item(i);
      if (attr.getNodeName().equals("quantity")) {
        try {
          builder.setQuantity(Integer.parseInt(attr.getNodeValue()));
        } catch (NumberFormatException e) {
          throw new ParseException("reactant quantity must be an integer");
        }
      } else {
        throw new ParseException("unexpected attribute in reactant definition: " + attr.toString());
      }
    }
    
    NodeList subnodes = reactantNode.getChildNodes();
    for (int i = 0; i < subnodes.getLength(); ++i) {
      Node subnode = subnodes.item(i);
      if (subnode.getNodeName().equals("component")) {
        NamedNodeMap componentAttrs = subnode.getAttributes();
        Node componentName = componentAttrs.getNamedItem("name");
        if (componentName != null) {
          builder.setComponentName(componentName.getNodeValue());
        } else {
          throw new ParseException("component specifier must contain name attribute");
        }
      } else if (subnode.getNodeName().equals("constraints")) {
        List<ReactantConstraint> constraints = parseReactantConstraints(subnode);
        builder.setConstraints(constraints);
      }
    }
    
    return builder.build();
  }
  
  private CreatedRobot parseCreatedRobot(Node robotNode) throws ParseException {
    CreatedRobotBuilder builder = new CreatedRobotBuilder();
    
    NamedNodeMap attrs = robotNode.getAttributes();
    for (int i = 0; i < attrs.getLength(); ++i) {
      Node attr = attrs.item(i);
      if (attr.getNodeName().equals("class")) {
        builder.setRobotClass(attr.getNodeValue());
      } else {
        throw new ParseException("unexpected attribute in product definition: " + attr.toString());
      }
    }
    
    return builder.build();
  }
  
  private CreatedMachine parseCreatedMachine(Node robotNode) throws ParseException {
    CreatedMachineBuilder builder = new CreatedMachineBuilder();
    
    NamedNodeMap attrs = robotNode.getAttributes();
    for (int i = 0; i < attrs.getLength(); ++i) {
      Node attr = attrs.item(i);
      if (attr.getNodeName().equals("class")) {
        builder.setMachineClass(attr.getNodeValue());
      } else if (attr.getNodeName().equals("mcu")) {
        builder.setRequiresMCU(Boolean.parseBoolean(attr.getNodeValue()));
      } else {
        throw new ParseException("unexpected attribute in product definition: " + attr.toString());
      }
    }
    
    return builder.build();
  }
  
  private Product parseProduct(Node productNode) throws ParseException {
    ProductBuilder builder = new ProductBuilder();
    
    NamedNodeMap attrs = productNode.getAttributes();
    for (int i = 0; i < attrs.getLength(); ++i) {
      Node attr = attrs.item(i);
      if (attr.getNodeName().equals("quantity")) {
        try {
          builder.setQuantity(Integer.parseInt(attr.getNodeValue()));
        } catch (NumberFormatException e) {
          throw new ParseException("product quantity must be an integer");
        }
      } else {
        throw new ParseException("unexpected attribute in product definition: " + attr.toString());
      }
    }
    
    NodeList subnodes = productNode.getChildNodes();
    for (int i = 0; i < subnodes.getLength(); ++i) {
      Node subnode = subnodes.item(i);
      if (subnode.getNodeName().equals("component")) {
        NamedNodeMap componentAttrs = subnode.getAttributes();
        Node componentName = componentAttrs.getNamedItem("name");
        if (componentName != null) {
          builder.setComponentName(componentName.getNodeValue());
        } else {
          throw new ParseException("component specifier must contain name attribute");
        }
      } else if (subnode.getNodeName().equals("copyMaterial")) {
        NamedNodeMap matAttrs = subnode.getAttributes();
        Node indexNode = matAttrs.getNamedItem("index");
        if (indexNode != null) {
          try {
            int idx = Integer.parseInt(indexNode.getNodeValue());
            builder.setCopiedMaterial(idx);
          } catch (NumberFormatException e) {
            throw new ParseException("copyMaterial index must be an integer");
          }
        } else {
          throw new ParseException("copyMaterial specifier must contain index attribute");
        }
      }
    }
    
    return builder.build();
  }
  
  private List<Reactant> parseReactants(Node reactantsNode) throws ParseException {
    List<Reactant> reactants = new LinkedList<>();
    NodeList subnodes = reactantsNode.getChildNodes();
    for (int i = 0; i < subnodes.getLength(); ++i) {
      Node subnode = subnodes.item(i);
      if (subnode.getNodeType() == Node.ELEMENT_NODE) {
        if (subnode.getNodeName().equals("reactant")) {
          reactants.add(parseReactant(subnode));
        } else {
          throw new ParseException("unexpected node, expecting reactant definition: " + subnode.toString());
        }
      }
    }
    return reactants;
  }
  
  private List<Product> parseProducts(Node productsNode) throws ParseException {
    List<Product> products = new LinkedList<>();
    NodeList subnodes = productsNode.getChildNodes();
    for (int i = 0; i < subnodes.getLength(); ++i) {
      Node subnode = subnodes.item(i);
      if (subnode.getNodeType() == Node.ELEMENT_NODE) {
        if (subnode.getNodeName().equals("product")) {
          products.add(parseProduct(subnode));
        } else {
          throw new ParseException("unexpected node, expecting product definition: " + subnode.toString());
        }
      }
    }
    return products;
  }
  
  private List<CreatedObject> parseCreatedObjects(Node createdObjectsNode) throws ParseException {
    List<CreatedObject> createdObjects = new LinkedList<>();
    NodeList subnodes = createdObjectsNode.getChildNodes();
    for (int i = 0; i < subnodes.getLength(); ++i) {
      Node subnode = subnodes.item(i);
      if (subnode.getNodeType() == Node.ELEMENT_NODE) {
        if (subnode.getNodeName().equals("robot")) {
          createdObjects.add(parseCreatedRobot(subnode));
        } else if (subnode.getNodeName().equals("machine")) {
          createdObjects.add(parseCreatedMachine(subnode));
        } else {
          throw new ParseException("unexpected node, expecting created object definition: " + subnode.toString());
        }
      }
    }
    return createdObjects;
  }
  
  private void parseReaction(Node reactionNode) throws ParseException {
    if (!reactionNode.getNodeName().equals("reaction")) {
      throw new ParseException("not a reaction definition: " + reactionNode.toString());
    }
    ReactionBuilder builder = new ReactionBuilder();
    NamedNodeMap attrs = reactionNode.getAttributes();
    for (int i = 0; i < attrs.getLength(); ++i) {
      Node subnode = attrs.item(i);
      if (subnode.getNodeType() == Node.ATTRIBUTE_NODE) {
        if (subnode.getNodeName().equals("name")) {
          builder.setReactionName(subnode.getNodeValue());
        } else if (subnode.getNodeName().equals("time")) {
          try {
            int time = Integer.parseInt(subnode.getNodeValue());
            builder.setReactionTime(time);
          } catch (NumberFormatException e) {
            throw new ParseException("value of attribute 'time' must be a positive integer");
          }
        } else if (subnode.getNodeName().equals("id")) {
          try {
            int id = Integer.parseInt(subnode.getNodeValue());
            builder.setReactionID(id);
          } catch (NumberFormatException e) {
            throw new ParseException("value of attribute 'id' must be a positive integer");
          }
        } else {
          throw new ParseException("unexpected attribute in reaction definition: " + subnode.toString());
        }
      }
    }
    NodeList children = reactionNode.getChildNodes();
    for (int i = 0; i < children.getLength(); ++i) {
      Node subnode = children.item(i);
      if (subnode.getNodeType() == Node.ELEMENT_NODE) {
        if (subnode.getNodeName().equals("categories")) {
          List<String> categories = parseCategories(subnode);
          builder.setCategories(categories);
        } else if (subnode.getNodeName().equals("reactants")) {
          List<Reactant> reactants = parseReactants(subnode);
          builder.setReactants(reactants);
        } else if (subnode.getNodeName().equals("products")) {
          List<Product> products = parseProducts(subnode);
          builder.setProducts(products);
        } else if (subnode.getNodeName().equals("createdObjects")) {
          List<CreatedObject> createdObjects = parseCreatedObjects(subnode);
          builder.setCreatedObjects(createdObjects);
        } else {
          throw new ParseException("unexpected node in reaction definition: " + subnode.toString());
        }
      }
    }
    
    Reaction rx = builder.build();
    // check for a duplicate
    if (getReactionByID(rx.getID()) != null) {
      throw new ParseException("duplicate reaction ID: " + Integer.toString(rx.getID()));
    }
    
    for (String category : rx.getCategories()) {
      addReaction(category, rx);
    }
  }
  
  //returns true iff successful
  public boolean loadReactions(File reactionsXML) {
    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      // ignore all lexical information
      dbf.setCoalescing(true);
      dbf.setExpandEntityReferences(true);
      dbf.setIgnoringComments(true);
      dbf.setIgnoringElementContentWhitespace(true);
   
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(reactionsXML);
   
      if (!doc.hasChildNodes()) {
        throw new ParseException("reaction definition file contains no content");
      }
   
      Node comps = doc.getFirstChild();
   
      if (!comps.getNodeName().equals("reactions")) {
        throw new ParseException("not a reaction definition file: top-level node is '" + doc.getNodeName() + "' but expected 'reactions'");
      }
   
      // each subnode is a component definition
      NodeList list = comps.getChildNodes();
      for (int i = 0; i < list.getLength(); ++i) {
        Node subnode = list.item(i);
        if (subnode.getNodeType() == Node.ELEMENT_NODE) {
          parseReaction(subnode);
        }
      }
     
      return true;
    } catch (ParserConfigurationException | SAXException | IOException | ParseException e) {
      System.err.println("internal error: failed to load reaction definition file " + reactionsXML.getAbsolutePath());
      e.printStackTrace();
      return false;
    }
  }
  
}
