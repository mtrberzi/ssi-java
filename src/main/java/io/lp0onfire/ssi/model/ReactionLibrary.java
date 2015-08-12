package io.lp0onfire.ssi.model;

import io.lp0onfire.ssi.ParseException;
import io.lp0onfire.ssi.model.reactions.Product;
import io.lp0onfire.ssi.model.reactions.ProductBuilder;
import io.lp0onfire.ssi.model.reactions.Reactant;
import io.lp0onfire.ssi.model.reactions.ReactantBuilder;

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
  }
  
  // maps reaction categories to every reaction in that category
  private Map<String, List<Reaction>> reactions;
  
  public void clear() {
    reactions.clear();
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
  
  private Reactant parseReactant(Node reactantNode) {
    ReactantBuilder builder = new ReactantBuilder();
    
    // TODO
    
    return builder.build();
  }
  
  private Product parseProduct(Node productNode) {
    ProductBuilder builder = new ProductBuilder();
    
    // TODO
    
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
            int time = Integer.parseInt(subnode.toString());
            builder.setReactionTime(time);
          } catch (NumberFormatException e) {
            throw new ParseException("value of attribute 'time' must be a positive integer");
          }
        } else {
          throw new ParseException("unexpected attribute in reaction definition: " + subnode.toString());
        }
      } else {
        throw new ParseException("unexpected node in reaction definition: " + subnode.toString());
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
        } else {
          throw new ParseException("unexpected node in reaction definition: " + subnode.toString());
        }
      } else {
        throw new ParseException("unexpected node in reaction definition: " + subnode.toString());
      }
    }
    
    Reaction rx = builder.build();
    // TODO add rx to each category
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
