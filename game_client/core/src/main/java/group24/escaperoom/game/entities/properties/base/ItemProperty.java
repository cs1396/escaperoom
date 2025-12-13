package group24.escaperoom.game.entities.properties.base;

import java.util.Optional;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;

import group24.escaperoom.editor.ui.ConfigurationMenu;
import group24.escaperoom.editor.ui.ConfigurationMenu.HandlesMenuClose;
import group24.escaperoom.editor.ui.ItemMenu;
import group24.escaperoom.editor.ui.Menu.MenuEntry;
import group24.escaperoom.editor.ui.PropertyConfiguration;
import group24.escaperoom.game.entities.Item;
import group24.escaperoom.game.entities.player.PlayerAction;
import group24.escaperoom.game.entities.properties.ConditionallyActive;
import group24.escaperoom.game.entities.properties.PropertyType;
import group24.escaperoom.game.entities.properties.values.BooleanValue;
import group24.escaperoom.game.entities.properties.values.ItemPropertyValue;
import group24.escaperoom.game.state.GameContext;
import group24.escaperoom.screens.GameScreen;
import group24.escaperoom.screens.ItemEditor;

public abstract class ItemProperty<T extends ItemPropertyValue> implements Json.Serializable {
  protected Item owner;

  public void setOwner(Item i) {
    this.owner = i;
  }

  public Item getOwner(){
    return owner;
  }

  public enum MenuType {
    /**
     * Selecting this property in a menu toggles it in some way
     *
     * Returning this means that the {@link ItemProperty#set(ItemPropertyValue)} 
     * method accepts a {@link BooleanValue}
     */
    Toggleable, 
    /**
     * Only one of the values: {@link ItemProperty#getPotentialValues()} 
     * may be selected
     */
    SelectOne, 
    /**
     * Up to all of the values: {@link ItemProperty#getPotentialValues()} 
     * may be selected
     */
    Select,
    /**
     * Selecting this property from a menu should create a pop out menu
     * from the menu provided by {@link ItemProperty#getPopOut}
     */
    PopOut,
    /**
     * Do not display this property on a menu
     *
     * May be a instance of {@link PhantomProperty}
     */
    None,
  }

  /**
   * @return the class of the {@link ItemPropertyValue} for this property
   */
  abstract public Class<T> getValueClass();

  /**
   * @return all possible values for this property
   */
  abstract public Array<T> getPotentialValues();

  /**
   * @return name of this property, used as a display string in menus
   */
  abstract public String getDisplayName();

  /**
   * @param owner create a default configuration of this property for the given owner
   */
  public void defaultConfiguration(Item owner){
    setOwner(owner);
  }

  /**
   * @param owner apply this property to this item
   */
  public void apply(Item owner){
    setOwner(owner);
  }

  /**
   * @return the {@link PropertyDescription} of this Property
   */
  abstract public PropertyDescription getDescription();

  /**
   * @return {@link PropertyType} of this property.
   *
   * This allows us to get the property of an item from it's property map
   * 
   * @see Item#getProperty
   */
  abstract public PropertyType getType();

  /**
   * Set the value of this property to value
   * It is the responsibility of the caller to only pass values that
   * are recieved from {@link ItemProperty#getPotentialValues()}
   *
   * @param values to set
   */
  public void set(Array<T> values) {}

  /**
   * Set the value of this property to value
   * It is the responsibility of the caller to only pass values that
   * are recieved from {@link ItemProperty#getPotentialValues()}
   *
   * @param value to set
   */
  public void set(T value) {}

  /**
   * Unsafely set this item. 
   * This should only be used when the compile time class of the 
   * expected property type cannot be determined.
   *
   * The caller must ensure that the value class is the same
   * as {@link ItemProperty#getValueClass()}
   *
   * @param value to set
   *
   */
  public void unsafeSet(Array<ItemPropertyValue> value){
    try {
      Array<T> upcastVals = new Array<>();
      value.forEach((v) -> upcastVals.add((T)v));
      set(upcastVals);
    } catch (ClassCastException cce){
      cce.printStackTrace();
      System.err.println("unsafeSet failed to cast class -> set failed");
    }
  }

  /**
   * Unsafely set this item
   *
   * This should only be used when the compile time class of the 
   * expected property class cannot be determined.
   *
   * The caller must ensure that the value class is the same
   * as {@link ItemProperty#getValueClass()}
   * 
   * @param value to set
   */
  public void unsafeSet(ItemPropertyValue value){
    try {
    set((T)value);
    } catch (ClassCastException cce){
      cce.printStackTrace();
      System.err.println("unsafeSet failed to cast class -> set failed");
    }
  }

  /**
   * @return the {@link MenuType} that should be used to recieve and display
   * the {@link ItemPropertyValue} associated with this property
   *
   * - adding a new input type requires updating {@link ItemMenu} to
   *          support it
   */
  abstract public MenuType getInputType();

  /**
   * 
   * If this property specifies {@link MenuType#PopOut}
   *
   * This function will be called to get the Menu which should be 
   * displayed when this property is selected.
   *
   * @param <CC> the {@link ConfigurationMenu} requires an actor type implementing {@link HandlesMenuClose} 
   * @param parent {@link MenuEntry} spawning this menu
   * @return the menu to display
   */
  public<CC extends Actor & HandlesMenuClose> ConfigurationMenu<CC> getPopOut(MenuEntry parent) {
    return null;
  }

  /**
   * 
   * Get a {@link PropertyConfiguration} that contains all fields necessary to 
   * configure this property in the {@link ItemEditor}
   *
   * @return Some({@link PropertyConfiguration}) should this property need configuration
   *
   */
  public Optional<PropertyConfiguration> getCustomItemConfigurationMenu() {return Optional.empty();}

  /**
   * @return an array containing the current values of this property
   */
  public Array<T> getCurrentValues() {return new Array<>();}

  /**
   * @return the current value of this property
   */
  public T getCurrentValue() {return null; }

  /**
   * @return all available {@link PlayerAction}
   */
  abstract protected Array<PlayerAction> getAvailableActions();

  public Array<PlayerAction> getActions(GameContext ctx) {
    if (owner == null) {
      throw new IllegalStateException();
    }
    return owner
        .getProperty(PropertyType.ConditionallyActive, ConditionallyActive.class)
        .map((p) -> {
          return p.isValid(ctx) ? getAvailableActions(): new Array<PlayerAction>();
        })
        .orElseGet(() -> getAvailableActions());
  }

  /**
   * @param newOwner the new owner of this property
   * @return a clone of this item property (along with state and values)
   * for a new item
   */
  public ItemProperty<? extends ItemPropertyValue> cloneProperty(Item newOwner) {
    ItemProperty<? extends ItemPropertyValue> p = this.getType().getEmptyProperty();
    p.owner = newOwner;
    p.read(new Json(), new JsonReader().parse(new Json().toJson(this)));
    return p;
  }

  /**
   * Updates the owner's texture based on the property. Properties which do not
   * change the texture should not override this function.
   *
   * Typically used when reloading textures on already placed items.
   */
  public void updateTexture() { }

  /**
   * Called when this item is loaded onto into a {@link group24.escaperoom.screens.GameScreen}
   *
   * May be used to attach listeners to the {@link group24.escaperoom.game.state.GameEventBus}
   */
  public void onGameLoad(GameScreen screen) { }
}
