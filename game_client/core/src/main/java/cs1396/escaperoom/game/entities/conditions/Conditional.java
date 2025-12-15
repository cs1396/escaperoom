package cs1396.escaperoom.game.entities.conditions;

import java.util.Optional;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import cs1396.escaperoom.editor.ui.ConditionalUI;
import cs1396.escaperoom.editor.ui.ConfigurationMenu.HandlesMenuClose;
import cs1396.escaperoom.editor.ui.Menu;
import cs1396.escaperoom.editor.ui.Menu.MenuEntry;
import cs1396.escaperoom.game.entities.properties.values.ItemPropertyValue;
import cs1396.escaperoom.game.state.GameContext;
import cs1396.escaperoom.screens.LevelEditor;
import cs1396.escaperoom.ui.SimpleUI;

abstract public class Conditional implements Json.Serializable, ItemPropertyValue {

  /**
   * @param ctx the {@link GameContext} which should be used to evaluate this {@link Conditional}
   *
   * @return whether or not this {@link Conditional} is {@code true}
   */
  abstract public boolean evaluate(GameContext ctx);

  /**
   * @return the type of this conditional
   */
  abstract public ConditionalType getType();

  /**
   * @return the name of this {@link Conditional}
   */
  abstract public String getName();

  /**
   * @return whether or not this {@link Conditional} requires polling
   */
  public boolean requiresPoll(){
    return false;
  }

  /**
   * Given the game context, return whether or not this conditional needs to be polled.
   *
   * It must be valid to poll the conditional even after this function returns false
   *
   * @param ctx the game context
   * @return whether or not this conditional still needs to be polled
   */
  public boolean poll(GameContext ctx){
    return false;
  }
  /**
   * Whether or not to limit the number of children below this conditional
   *
   * By default assumes the condition can have no children
   *
   * @return Some(count) if this conditional has a limit to the number of children
   */
  public Optional<Integer> childCount() {
    return Optional.of(0);
  }

  public Conditional clone() {
    JsonValue jsonData = new JsonReader().parse(new Json().toJson(this));
    Conditional c = this.getType().getConditional();
    c.read(new Json(), jsonData);
    return c;
  }

  /**
   * Returns an array of this conditional dependents, if they have any.
   *
   * The array will be the same length as {@link Conditional#childCount()}
   *
   * @return an array of conditionals which this conditional depends on 
   *
   */
  public Array<Conditional> getChildren(){
    return new Array<>();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Conditional){
      Conditional other = Conditional.class.cast(obj);
      return other.getType() == this.getType();
    }
    return false;
  }

  /**
   * @param conds the {@link Conditional}s to set
   */
  public void setChildren(Conditional ... conds){
  }

  /**
   * Conditionals are not represented in Menus
   */
  @Override
  public MenuEntry getDisplay(Menu parent) {
    return null;
  }

  /**
   *
   * Primarily used in the {@link ConditionalUI}
   *
   * @param <CC> an actor type implenting {@link HandlesMenuClose}
   *             should  this be uncessary, use {@link SimpleUI}
   * @param editor the {@link LevelEditor} on which this menu will be spawned 
   * @return Some(CC) should this {@link Conditional} require additional 
   *         configuration. 
   *
   */
  abstract public<CC extends Actor & HandlesMenuClose> Optional<CC> getEditorConfiguration(LevelEditor editor);
}
