package io.lp0onfire.ssi.model;

import java.util.HashMap;
import java.util.Map;

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
    // the material library always contains a material named "bedrock"
    MaterialBuilder bedrockBuilder = new MaterialBuilder();
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
  
}
