package cs1396.escaperoom.game.entities.properties.values;

import cs1396.escaperoom.editor.ui.Menu;
import cs1396.escaperoom.editor.ui.Menu.MenuEntry;

/**
 * A wrapper around a boolean that is also a {@link ItemPropertyValue}
 */
public class BooleanValue implements ItemPropertyValue {
  protected boolean inner = false;

  /**
   * @param inner the inner boolean value
   */
  public BooleanValue(boolean inner) {
    this.inner = inner;
  }

  /**
   * @return whether this value is true
   */
  public boolean isTrue(){
    return inner;
  }

  @Override
  public MenuEntry getDisplay(Menu parent) {
    return null;
  }
}
