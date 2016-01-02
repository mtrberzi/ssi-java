package io.lp0onfire.ssi.model;

import io.lp0onfire.ssi.ParseException;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
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

public class MaterialLibrary {

  private static MaterialLibrary inst = null;
  public static MaterialLibrary getInstance() {
    if (inst == null) {
      inst = new MaterialLibrary();
    }
    return inst;
  }
  
  private MaterialLibrary() {
    materials = new HashMap<>();
    init();
  }
  
  private void init() {
    // the material library always contains a material named "bedrock"
    MaterialBuilder bedrockBuilder = new MaterialBuilder();
    bedrockBuilder.setMaterialName("bedrock");
    bedrockBuilder.setType(0);
    bedrockBuilder.setDurabilityModifier(9999.0);
    addMaterial("bedrock", bedrockBuilder.build());
  }
  
  private Map<String, Material> materials;
  public void addMaterial(String mKey, Material mValue) {
    if (materials.containsKey(mKey)) {
      throw new IllegalArgumentException("materials library already contains material '" + mKey + "'");
    }
    materials.put(mKey, mValue);
  }
  public Material getMaterial(String mKey) {
    if (materials.containsKey(mKey)) {
      return materials.get(mKey);
    } else {
      throw new IllegalArgumentException("materials library does not contain material '" + mKey + "'");
    }
  }
  public void clear() {
    materials.clear();
    init();
  }
  
  public Collection<Material> getAllMaterials() {
    return materials.values();
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
  
  private void parseMaterial(Node materialNode) throws ParseException {
    if (!materialNode.getNodeName().equals("material")) {
      throw new ParseException("not a material definition: " + materialNode.toString());
    }
    MaterialBuilder builder = new MaterialBuilder();
    
    NamedNodeMap attrs = materialNode.getAttributes();
    
    for (int i = 0; i < attrs.getLength(); ++i) {
      Node subnode = attrs.item(i);
      if (subnode.getNodeType() == Node.ATTRIBUTE_NODE) {
        if (subnode.getNodeName().equals("name")) {
          builder.setMaterialName(subnode.getNodeValue());
        } else if (subnode.getNodeName().equals("type")) {
          try {
            builder.setType(Integer.parseInt(subnode.getNodeValue()));
          } catch (NumberFormatException e) {
            throw new ParseException("type ID must be an integer");
          }
        } else if (subnode.getNodeName().equals("durabilityModifier")) {
          try {
            builder.setDurabilityModifier(Double.parseDouble(subnode.getNodeValue()));
          } catch (NumberFormatException e) {
            throw new ParseException("durability modifier must be a decimal value");
          }
        } else {
          throw new ParseException("unexpected attribute in material definition: " + subnode.toString());
        }
      }
    }
    
    NodeList subnodes = materialNode.getChildNodes();
    for (int i = 0; i < subnodes.getLength(); ++i) {
      Node subnode = subnodes.item(i);
      if (subnode.getNodeType() == Node.ELEMENT_NODE) {
        if (subnode.getNodeName().equals("categories")) {
          List<String> categories = parseCategories(subnode);
          builder.setCategories(categories);
        } else {
          throw new ParseException("unexpected subnode in material definition: " + subnode.toString());
        }
      }
    }
    
    try {
      Material m = builder.build();
      if (materials.containsKey(m.getName())) {
        throw new ParseException("duplicate definition of material '" + m.getName() + "'");
      }
      materials.put(m.getName(), m);
    } catch (IllegalArgumentException e) {
      throw new ParseException("invalid component definition: " + e.getMessage());
    }
  }
  
  // returns true iff successful
  public boolean loadMaterials(File componentsXML) {
    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      // ignore all lexical information
      dbf.setCoalescing(true);
      dbf.setExpandEntityReferences(true);
      dbf.setIgnoringComments(true);
      dbf.setIgnoringElementContentWhitespace(true);
      
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(componentsXML);
      
      if (!doc.hasChildNodes()) {
        throw new ParseException("material definition file contains no content");
      }
      
      Node comps = doc.getFirstChild();
      
      if (!comps.getNodeName().equals("materials")) {
        throw new ParseException("not a materials definition file: top-level node is '" + doc.getNodeName() + "' but expected 'materials'");
      }
      
      // each subnode is a component definition
      NodeList list = comps.getChildNodes();
      for (int i = 0; i < list.getLength(); ++i) {
        Node subnode = list.item(i);
        if (subnode.getNodeType() == Node.ELEMENT_NODE) {
          parseMaterial(subnode);
        }
      }
      
      return true;
    } catch (ParserConfigurationException | SAXException | IOException | ParseException e) {
      System.err.println("internal error: failed to load materials definition file " + componentsXML.getAbsolutePath());
      e.printStackTrace();
      return false;
    }
  }
  
}
