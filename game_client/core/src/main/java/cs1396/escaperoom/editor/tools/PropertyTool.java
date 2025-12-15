package cs1396.escaperoom.editor.tools;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;

import cs1396.escaperoom.editor.core.ActionHistory.EditorAction;
import cs1396.escaperoom.editor.core.ToolManager.ToolType;
import cs1396.escaperoom.editor.ui.GridView;
import cs1396.escaperoom.editor.ui.ItemDecoration;
import cs1396.escaperoom.editor.ui.Menu;
import cs1396.escaperoom.editor.ui.PropertyMenu;
import cs1396.escaperoom.editor.ui.PropertyMenu.PropertyMenuEntry;
import cs1396.escaperoom.engine.BackManager;
import cs1396.escaperoom.engine.assets.items.ItemTypeData;
import cs1396.escaperoom.engine.control.CursorManager;
import cs1396.escaperoom.engine.control.CursorManager.CursorType;
import cs1396.escaperoom.engine.types.IntVector2;
import cs1396.escaperoom.game.entities.Item;
import cs1396.escaperoom.game.entities.properties.PropertyType;
import cs1396.escaperoom.game.entities.properties.base.ItemProperty;
import cs1396.escaperoom.game.entities.properties.values.ItemPropertyValue;
import cs1396.escaperoom.game.world.Grid;
import cs1396.escaperoom.screens.LevelEditor;
import cs1396.escaperoom.ui.notifications.Notifier;

public class PropertyTool extends EditorTool {
  // should in theory also have the validation logic in here, but
  // cannot reference fields in the parent class
  protected enum State {
    Inactive,
    Property,
    Item,
  }

  private class PasteView implements GridView {
    public ItemDecoration decorate(Item item){
      ItemDecoration decoration = new ItemDecoration();

      boolean valid = false;
      switch (PropertyTool.this.state) {
        case Item:
          valid = item.getType().equals(originalType);
          break;
        case Property:
          valid = item.hasProperty(singleProperty.getType());
          break;
        case Inactive:
          break;
      }

      if (valid) decoration.set(ItemDecoration.DIMMED, false);
      else decoration.set(ItemDecoration.DIMMED, true);

      return decoration;
    }
  }

  /**
   * A list of properties to completely ignore when copying. Some properties
   * have special copy behavior, such as {@code ContainsItemProperty}, but other
   * properties that don't want copied *at all* can be put here.
   */
  private static final HashSet<PropertyType> excludedProperties = new HashSet<>(Arrays.asList(
    PropertyType.TiledBrushable,
    PropertyType.Stylable
  ));

  State state = State.Inactive;
  IntVector2 touchDown = new IntVector2(-1,-1);
  Collection<ItemProperty<? extends ItemPropertyValue>> itemProperties;
  ItemProperty<? extends ItemPropertyValue> singleProperty;
  ItemTypeData originalType;
  boolean pasting;

  public PropertyTool(LevelEditor editor) {
    super(editor);
  }

  //----------------------------------------------------------------------------
  // Public API
  //----------------------------------------------------------------------------

  /**
   * Whether this tool is currently "pasting"
   *
   * That is, the tool has been used to paste a value 
   * to a single item 
   * and the user has not yet lifted the mouse button
   */
  public boolean isPasting(){
    return pasting;
  }

  //---------------------------------------------------------------------------
  // Tool lifecycle
  //---------------------------------------------------------------------------

  @Override
  public void select() {
    editor.addListener(copyPasteListener);
    CursorManager.setCursor(CursorType.CopyPaste);
    if (state != State.Inactive) applyCopied(state);
  }

  @Override
  public void cancel() {
    editor.removeListener(copyPasteListener);
    CursorManager.restoreDefault();
    if (state != State.Inactive) editor.clearGridView();
  }



  //---------------------------------------------------------------------------
  // Copy logic
  //---------------------------------------------------------------------------

  private void attemptPasteAt(float x, float y) {
    if (state == State.Inactive) {
      Notifier.warn("Properties not yet copied.");
      return;
    }

    // Get the item on this square, exit if none
    Optional<Item> oTarget = editor.priorityItemAt((int)x, (int)y);
    if (!oTarget.isPresent()) return;
    Item i = oTarget.get();

    // clear state here, as we need to call all of these callbacks when done
    Grid.onMapCompletion.clear();

    // only paste if this state allows it
    switch (state) {
      case Inactive:
        throw new IllegalStateException();
      case Item:
        if (!i.getType().equals(originalType)) return;

        itemProperties.forEach(p -> {
          if (!excludedProperties.contains(p.getType())) {
            i.addProperty(p.cloneProperty(i));
          }
        });

        editor.recordEditorAction(new SetProperty(i, i.getProperties(), itemProperties));

        break;
      case Property:
        if (!i.hasProperty(singleProperty.getType())) return;

        i.addProperty(singleProperty.cloneProperty(i));
        // editor.recordEditorAction(new SetProperty(i, , replacement));

        break;
    }


    Notifier.info(String.format("Pasted properties to %s", i.getItemName()));

    // apply created callbacks
    Grid.onMapCompletion.forEach((f) -> f.apply(editor.grid));
    return;
  }

  private void attemptCopyAt(float x, float y) {
    Vector2 screenCoords = editor.gameCoordToUI(new Vector2(x, y));
    Menu.MenuEntry entry = editor.collidedMenuEntry(screenCoords.x, screenCoords.y, PropertyMenu.class);
    if (entry != null && entry instanceof PropertyMenuEntry) {
      PropertyMenuEntry e = (PropertyMenuEntry) entry;

      singleProperty = e.getProperty();
      applyCopied(State.Property);

      Notifier.info(String.format("Copied %s property", e.getProperty().getDisplayName()));
      return;
    }

    // Copy the properties of the priority item on this square
    Optional<Item> oTarget = editor.priorityItemAt((int)x, (int)y);
    if (oTarget.isPresent()) {
      Item i = oTarget.get();

      itemProperties =  i.getProperties();
      originalType = i.getType();
      applyCopied(State.Item);

      Notifier.info(String.format("Copied properties of %s", i.getItemName()));
      return;
    }

    return;
  }

  private void applyCopied(State state) {
    this.state = state;

    editor.setGridView(new PasteView());

    BackManager.addBack(() -> {
      EditorTool current = editor.getActiveTool();
      if (current == null || current.getType() != this.getType()) return false;

      itemProperties = null;
      originalType = null;
      singleProperty = null;

      this.state = State.Inactive;
      editor.clearGridView();

      Notifier.info("Cleared property selection");
      return true;
    });
  }

  //----------------------------------------------------------------------------
  // Input handling
  //----------------------------------------------------------------------------

  private boolean handleTouchDown(float x, float y){
    pasting = true;
    touchDown.set(x,y);
    attemptPasteAt(x, y);
    return true;
  }
  private boolean handleSelectProperties(float x, float y) {
    pasting = false;
    attemptCopyAt(x, y);
    return true;
  }
  private void handleDrag(float x, float y){
    if (!pasting || touchDown.equals((int)x, (int)y)) return;

    touchDown.set(x, y);
    attemptPasteAt(x, y);
  }


  private final InputListener copyPasteListener = new InputListener() {
    @Override
    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
      if (button == Buttons.LEFT) return handleTouchDown(x, y);
      else if (button == Buttons.RIGHT) return handleSelectProperties(x, y);
      return false;
    }
    public void touchDragged (InputEvent event, float x, float y, int pointer) {
      handleDrag(x, y);
    }

    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
      pasting = false;
    };
  };




  /**
   * Represents a deletion operation
   */
  public static class SetProperty implements EditorAction {
    State state;
    Item item;
    Collection<ItemProperty<? extends ItemPropertyValue>> originals, replacement;
    ItemProperty<? extends ItemPropertyValue> original, replaced;

    public SetProperty(Item item, Collection<ItemProperty<? extends ItemPropertyValue>> originals, Collection<ItemProperty<? extends ItemPropertyValue>> replacement) {
      this.state = State.Item;
      this.item = item;
      this.originals = originals;
      this.replacement = replacement;
    }

    public SetProperty(Item item, ItemProperty<? extends ItemPropertyValue> original, ItemProperty<? extends ItemPropertyValue> replaced) {
      this.state = State.Property;
      this.item = item;
      this.original = original;
      this.replaced = replaced;
    }

    @Override
    public void doAction() {
      switch (this.state) {
        case Inactive:
          throw new IllegalStateException();
        case Item:
          replacement.forEach(p -> {
            item.addProperty(p);
          });
        case Property:
          item.addProperty(replaced);
          break;
      }
    }

    @Override
    public void undoAction() {
      switch (this.state) {
        case Inactive:
          throw new IllegalStateException();
        case Item:
          originals.forEach(p -> {
            item.addProperty(p);
          });
        case Property:
          item.addProperty(original);
          break;
      }
    }
  }

  //---------------------------------------------------------------------------
  // Tool Attributes
  //---------------------------------------------------------------------------

  @Override
  public String getButtonStyle() {
    return "property_cv";
  }

  @Override
  public ToolType getType(){
    return ToolType.Properties;
  }

  @Override
  public String getName() {
    return "Copy Properties";
  }
}
