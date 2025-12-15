package cs1396.escaperoom.game.entities.properties;

import cs1396.escaperoom.game.entities.properties.base.BooleanProperty;
import cs1396.escaperoom.game.entities.properties.base.PropertyDescription;

public class FragileProperty extends BooleanProperty {

  private static final PropertyDescription description = new PropertyDescription(
    "Fragile",
    "Breaks after use",
    "Fragile items break after a use. Unlocker items get removed from player inventory. Toggleable items can no longer be toggled.",
    null
  );
  @Override 
  public PropertyDescription getDescription(){
    return description;
  }

  boolean isBroken = false;

	@Override
	public String getDisplayName() {
    return "Fragile";
	}

	@Override
	public PropertyType getType() {
    return PropertyType.Fragile;
	}

  public void setBroken(boolean isBroken){
    this.isBroken = isBroken;
  }

  public boolean isBroken(){
    return isBroken;
  }
}
