package io.lp0onfire.ssi.model.reactions;

import io.lp0onfire.ssi.model.Item;

public abstract class ReactantConstraint {

  public abstract boolean matches(Item i);
  
}
