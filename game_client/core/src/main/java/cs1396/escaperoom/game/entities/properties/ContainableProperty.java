package cs1396.escaperoom.game.entities.properties;

import java.util.HashSet;
import java.util.Set;

import cs1396.escaperoom.game.entities.properties.base.PhantomProperty;
import cs1396.escaperoom.game.entities.properties.base.PropertyDescription;

public class ContainableProperty extends PhantomProperty {

  private static final PropertyDescription description = new PropertyDescription(
    "Containable",
    "Can be contained",
    "Containable items can be placed into a ContainsItems type item",
    new HashSet<>(Set.of(PropertyType.ContainsItemsProperty))
  );
  @Override 
  public PropertyDescription getDescription(){
    return description;
  }

  @Override
  public String getDisplayName() {
    return "Containable";
  }

  @Override
  public PropertyType getType() {
    return PropertyType.Containable;
  }
}
