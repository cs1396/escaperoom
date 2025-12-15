package cs1396.escaperoom.game.entities.properties.base;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import cs1396.escaperoom.game.entities.player.PlayerAction;
import cs1396.escaperoom.game.entities.properties.values.BooleanValue;

/**
 * A base class for properties which are true or false
 */
public abstract class BooleanProperty extends ItemProperty<BooleanValue> {

  /**
   * The current value of this property
   */
  public boolean currentValue = false;

  @Override
  public void write(Json json) {
    json.writeValue("value", currentValue);
  }

  @Override
  public void read(Json json, JsonValue jsonData) {
    currentValue = jsonData.getBoolean("value", false);
  }

  @Override
  public Class<BooleanValue> getValueClass() {
    return BooleanValue.class;
  }

  @Override
  public Array<BooleanValue> getPotentialValues() {
    return Array.with(new BooleanValue(true), new BooleanValue(false));
  }

  @Override
  public void set(BooleanValue value) {
    currentValue = value.isTrue();
  }

  @Override
  public MenuType getInputType() {
    return MenuType.Toggleable;
  }

  @Override
  public BooleanValue getCurrentValue() {
    return new BooleanValue(currentValue);
  }

  @Override
  protected Array<PlayerAction> getAvailableActions() {
    return new Array<>();
  }

  public boolean isTrue() {
    return currentValue;
  }

}
