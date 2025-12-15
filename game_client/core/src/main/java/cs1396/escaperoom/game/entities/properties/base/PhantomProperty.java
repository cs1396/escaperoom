package cs1396.escaperoom.game.entities.properties.base;

import com.badlogic.gdx.utils.Array;

import cs1396.escaperoom.game.entities.player.PlayerAction;
import cs1396.escaperoom.game.entities.properties.ContainableProperty;
import cs1396.escaperoom.game.entities.properties.values.PhantomPropertyValue;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

/**
 * A {@code PhantomProperty} is to be used as a marker object to represent simple object behavior.
 *
 * e.g. {@link ContainableProperty} is a PhantomProperty in that an object is able to be placed in a container
 *
 * Extend this class to create marker objects
 *
 * - These properties will not show up in the {@link cs1396.escaperoom.screens.LevelEditor}
 */
public abstract class PhantomProperty extends ItemProperty<PhantomPropertyValue> {
  @Override
  public Array<PhantomPropertyValue> getPotentialValues() {
    return new Array<>();
  }

  @Override
  public Class<PhantomPropertyValue> getValueClass(){
      return PhantomPropertyValue.class;
  }

  @Override
  public MenuType getInputType() {
    return MenuType.None;
  }

  @Override
  public Array<PlayerAction> getAvailableActions() {
    return new Array<>();
  }

  @Override
  public void write(Json json) { }

  @Override
  public void read(Json json, JsonValue jsonData) { }
}
