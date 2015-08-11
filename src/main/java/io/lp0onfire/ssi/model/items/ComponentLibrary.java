package io.lp0onfire.ssi.model.items;

import io.lp0onfire.ssi.ParseException;
import io.lp0onfire.ssi.model.Material;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ComponentLibrary {

  private static ComponentLibrary inst = null;
  public static ComponentLibrary getInstance() {
    if (inst == null) {
      inst = new ComponentLibrary();
    }
    return inst;
  }
  
  private ComponentLibrary() {
    components = new HashMap<>();
  }
  
  private Map<String, ComponentBuilder> components;
  
  public void clear() {
    components.clear();
  }
  
  public Component createComponent(String cKey, Material material) {
    if (components.containsKey(cKey)) {
      return components.get(cKey).build(material);
    } else {
      throw new IllegalArgumentException("components library does not contain component '" + cKey + "'");
    }
  }
  
  private void parseComponent(Node componentNode) throws ParseException {
    if (!componentNode.getNodeName().equals("component")) {
      throw new ParseException("not a component definition: " + componentNode.toString());
    }
    ComponentBuilder builder = new ComponentBuilder();
    
    NamedNodeMap attrs = componentNode.getAttributes();
    
    for (int i = 0; i < attrs.getLength(); ++i) {
      Node subnode = attrs.item(i);
      if (subnode.getNodeType() == Node.ATTRIBUTE_NODE) {
        if (subnode.getNodeName().equals("name")) {
          builder.setComponentName(subnode.getNodeValue());
        } else {
          throw new ParseException("unexpected attribute in component definition: " + subnode.toString());
        }
      } else {
        throw new ParseException("unexpected node in component definition: " + subnode.toString());
      }
    }
    
    try {
      builder.validate();
      if (components.containsKey(builder.getComponentName())) {
        throw new ParseException("duplicate definition of component " + builder.getComponentName());
      }
      components.put(builder.getComponentName(), builder);
    } catch (IllegalArgumentException e) {
      throw new ParseException("invalid component definition: " + e.getMessage());
    }
  }
  
  // returns true iff successful
  public boolean loadComponents(File componentsXML) {
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
        throw new ParseException("component definition file contains no content");
      }
      
      Node comps = doc.getFirstChild();
      
      if (!comps.getNodeName().equals("components")) {
        throw new ParseException("not a component definition file: top-level node is '" + doc.getNodeName() + "' but expected 'components'");
      }
      
      // each subnode is a component definition
      NodeList list = comps.getChildNodes();
      for (int i = 0; i < list.getLength(); ++i) {
        Node subnode = list.item(i);
        if (subnode.getNodeType() == Node.ELEMENT_NODE) {
          parseComponent(subnode);
        }
      }
      
      return true;
    } catch (ParserConfigurationException | SAXException | IOException | ParseException e) {
      System.err.println("internal error: failed to load component definition file " + componentsXML.getAbsolutePath());
      e.printStackTrace();
      return false;
    }
  }
  
}
