package cs1396.escaperoom.screens;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Logger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.utils.Array;

import cs1396.escaperoom.editor.core.ActionHistory;
import cs1396.escaperoom.editor.core.ActionHistory.EditorAction;
import cs1396.escaperoom.editor.core.DragManager;
import cs1396.escaperoom.editor.core.ToolManager;
import cs1396.escaperoom.editor.core.ToolManager.ToolType;
import cs1396.escaperoom.editor.tools.Brush;
import cs1396.escaperoom.editor.tools.DeletionTool;
import cs1396.escaperoom.editor.tools.DeletionTool.Deletion;
import cs1396.escaperoom.editor.tools.EditorTool;
import cs1396.escaperoom.editor.tools.EyeDropTool;
import cs1396.escaperoom.editor.tools.ItemDrawer;
import cs1396.escaperoom.editor.tools.ItemSelectionTool;
import cs1396.escaperoom.editor.tools.MoveTool.SoloMoveTool;
import cs1396.escaperoom.editor.tools.PanTool;
import cs1396.escaperoom.editor.tools.RotationTool.RotationAction;
import cs1396.escaperoom.editor.ui.EditorSettingsDialog;
import cs1396.escaperoom.editor.ui.EditorUI;
import cs1396.escaperoom.editor.ui.EditorUI.ItemHintMode;
import cs1396.escaperoom.editor.ui.GridView;
import cs1396.escaperoom.editor.ui.ItemMenu;
import cs1396.escaperoom.editor.ui.Menu;
import cs1396.escaperoom.engine.BackManager;
import cs1396.escaperoom.engine.assets.items.ItemLoader;
import cs1396.escaperoom.engine.assets.maps.MapData;
import cs1396.escaperoom.engine.assets.maps.MapLoader;
import cs1396.escaperoom.engine.assets.maps.MapMetadata;
import cs1396.escaperoom.engine.control.ControlsManager;
import cs1396.escaperoom.engine.control.ControlsManager.InputType;
import cs1396.escaperoom.engine.control.CursorManager;
import cs1396.escaperoom.engine.control.CursorManager.CursorType;
import cs1396.escaperoom.engine.control.input.Input;
import cs1396.escaperoom.engine.render.Drawable;
import cs1396.escaperoom.engine.types.IntVector2;
import cs1396.escaperoom.game.entities.Item;
import cs1396.escaperoom.game.entities.properties.PropertyType;
import cs1396.escaperoom.game.world.Grid.DrawableComparator;
import cs1396.escaperoom.screens.utils.CamMan.Translation;
import cs1396.escaperoom.screens.utils.ScreenManager;
import cs1396.escaperoom.ui.KeyMapDialog;
import cs1396.escaperoom.ui.notifications.Notifier;
import cs1396.escaperoom.ui.widgets.G24Dialog;

public class LevelEditor extends MapScreen {
  @SuppressWarnings("unused")
  private Logger log = Logger.getLogger(LevelEditor.class.getName());

  private ItemDrawer itemDrawer;
  private ToolManager toolManager;
  private ActionHistory history;
  private DragManager dragManager;
  private EditorUI ui;
  private GridView gridView;
  private Optional<Item> selectedItem = Optional.empty();
  private Optional<SoloMoveTool> movingItem = Optional.empty();
  private final HashSet<Drawable> overlayDrawables = new HashSet<>();
  private boolean dragging = false;
  private boolean panEnabled = true;
  ItemMenu itemMenu = null;

  public LevelEditor(MapData data) {
    super(data);

    itemDrawer = new ItemDrawer(this);
    toolManager = new ToolManager(this, itemDrawer);
    history = new ActionHistory();
    dragManager = new DragManager(this, roomProxy);
    ui = new EditorUI(this, toolManager, dragManager, history, itemDrawer);

    setEditMode();
    loadGrid(data.getGrid());
    cameraManager.setPosition(gridSize.width / 2, gridSize.height / 2);
  }

  public Array<Item> getItemPrototypes() {
    return itemDrawer.getItemPrototypes();
  }

  /**
   * Register edit keybinds add all UI to the stage 
   */
  private void setEditMode() {
    registerEditBinds();
    addUI(ui.getRoot());
    followActor = Optional.empty();
  }

  /**
   * Show the play screen
   */
  public void setPlayMode() {
    Optional<Item> playerStart = grid.findItemWhere(new Predicate<Item>() {
      @Override
      public boolean test(Item arg0) {
        return arg0.hasProperty(PropertyType.Player);
      }
    });
    if (playerStart.isEmpty()){
      Notifier.warn("Cannot play without player start\nSee GameControl -> Mr. E");
      return;
    }

    grid.placedItems.values().forEach((i) -> i.setSelected(false));
    clearGridView();
    CursorManager.restoreDefault();
    ScreenManager.instance().showScreen(new SinglePlayerGame(new MapData(grid, metadata)));
  }

  //----------------------------------------------------------------------------
  // ACCESSORS
  //----------------------------------------------------------------------------

  /**
   * Get the {@link ItemSelectionTool} from this editor
   */
  public MapMetadata getMetadata() {
    return metadata;
  }

  public ItemSelectionTool getSelectionTool() {
    return toolManager.getSelectTool();
  }

  public EditorUI getUI() {
    return ui;
  }

  /**
   * Return the {@link DragAndDrop} instance for this EditorKit
   *
   * (to add {@link DragAndDrop.Source})
   */
  public DragAndDrop getDragAndDrop() {
    return dragManager.getDragAndDrop();
  }

  //----------------------------------------------------------------------------
  // MUTATORS
  //----------------------------------------------------------------------------

  /**
   * Ask the editor to deselect all tools
   */
  public void deselectTools() {
    toolManager.deselectAll();
  }

  public void setActiveTool(EditorTool tool) {
    toolManager.setTool(tool);
  }

  public EditorTool getActiveTool() {
    if (toolManager.getActiveTool().isEmpty()) return null;
    return toolManager.getActiveTool().get();
  }

  public EditorTool getTool(ToolType type) {
    return toolManager.getTool(type);
  }

  //----------------------------------------------------------------------------
  // EDITOR ACTIONS
  //----------------------------------------------------------------------------

  /**
   * Add an {@link EditorAction} to the history
   */
  public void recordEditorAction(EditorAction action) {
    history.record(action);
  }

  public Optional<Item> getSelectedItem(){
    return selectedItem;
  }


  /**
   * Get the "best" item at this location. 
   *
   * That is, the item that is most likely to be selected by the player
   */
  public Optional<Item> priorityItemAt(int x, int y){
    Item inFlight = dragManager.getInFlightItem();
    if (inFlight != null) {
      return Optional.of(inFlight);
    }

    // If the coords are off the map -> return empty
    Optional<Item[]> maybeItems = getItemsAt(x, y);
    if (maybeItems.isEmpty()) {
      return Optional.empty();
    }

    Item[] items = maybeItems.get();

    // If there are no items here -> return empty
    if (items.length == 0) {
      return Optional.empty();
    }

    // Try and see if the currently selected item is in our stack
    if (selectedItem.isPresent()) {
      Item item = selectedItem.get();
      for (int i = 0; i < items.length; i++) {
        if (items[i].getID() == item.getID()) {
          return Optional.of(items[i]);
        }
      }
    }

    // Default to the top of the stack.
    Item item = null;
    int maxRender = Integer.MIN_VALUE;

    // Look for an item deeper in the stack with a higher render priority
    for (int i = items.length - 1; i >= 0; i--){
      // ignore transparent items, as they are (currently) always temporaries
      if (items[i].renderPriority() > maxRender && items[i].getAlpha() == 1f) {
        item = items[i];
        maxRender = item.renderPriority();
      }
    }

    // if no valid items were found, return empty
    if (item == null) {
      return Optional.empty();
    }

    return Optional.of(item);
  }


  /**
   * Given some grid position, get the selected Items at that position
   */
  @SuppressWarnings("unused")
  private Optional<Array<Item>> selectedItemsAt(int x, int y){
    return getItemsAt(x, y).flatMap((items) -> {

      // If there are not items, return
      if (items.length == 0) {
        return Optional.empty();
      }

      Array<Item> ret = new Array<>();
      for (Item i : items){
        if (i.selected){
          ret.add(i);
        }
      }

      return ret.isEmpty() ? Optional.empty() : Optional.of(ret);
    });
  }

  /**
   * Given some grid position, get the "next"
   * item in the stack, weighing render priority 
   * and the currently selected item
   */
  private Optional<Item> nextItemAt(int x, int y){
    return getItemsAt(x, y).flatMap((items) -> {

      // If there are not items, return
      if (items.length == 0) {
        return Optional.empty();
      }


      Item[] sortedItems = Arrays.copyOf(items, items.length);
      Arrays.sort(sortedItems, (a,b)-> new DrawableComparator().compare(a, b));

      // Default to the "top"
      int index = sortedItems.length - 1;

      // Check to see if we need to cycle
      if (selectedItem.isPresent()) {
        Item selection = selectedItem.get();

        // Look from the top to the bottom for the selected item
        for (int i = sortedItems.length - 1; i >= 0; i--) {

          if (sortedItems[i].getID() == selection.getID()) {
            // if we find it and it's at the bottom, cycle back to the top
            if (i == 0) {
              index = sortedItems.length - 1;
            } else {
              // select the next item in the stack
              index = i - 1;
            }
            break;
          }
        }
      }
      return Optional.of(sortedItems[index]);
    });
  }

  private void openItemMenu(Item i){
    itemMenu = new ItemMenu(i, this);
    addUI(itemMenu);
    BackManager.addBack(() -> {
      if (itemMenu == null || itemMenu.getStage() == null) return false;
      itemMenu.close();
      itemMenu = null;
      return true;
    });
  }

  public boolean itemIsPlaced(Item i){
    return grid.placedItems.containsKey(i.getID());
  }

  public boolean collidesMenu(float x, float y){
    for (Actor a : getUIStage().getRoot().getChildren()){
      if (a instanceof Menu){
        Menu m = (Menu)a;
        Rectangle bounds = new Rectangle(m.getX(), m.getY(), m.getWidth(), m.getHeight());
        if (bounds.contains(new Vector2(x,y))){
          return true;
        }
      } else if (a instanceof G24Dialog){
        G24Dialog m = (G24Dialog)a;
        Rectangle bounds = new Rectangle(m.getX(), m.getY(), m.getWidth(), m.getHeight());
        if (bounds.contains(new Vector2(x,y))){
          return true;
        }
      }
    }
    return false;
  }
  public <T extends Menu> Menu.MenuEntry collidedMenuEntry(float x, float y, Class<T> type) {
    for (Actor a : getUIStage().getRoot().getChildren()){
      if (type.isAssignableFrom(a.getClass())) {
        T m = (T)a;
        for (Actor e : m.getChildren()) {
          if (e instanceof Menu.MenuEntry) {
            Menu.MenuEntry me = (Menu.MenuEntry) e;
            Vector2 pos = me.localToStageCoordinates(new Vector2(0, 0));
            Rectangle bounds = new Rectangle(pos.x, pos.y, me.getWidth(), me.getHeight());
            if (bounds.contains(new Vector2(x, y))){
              return me;
            }
          }
        }
      }
    }
    return null;
  }

  private void zoomIn() {
    cameraManager.zoomIn();
    updateProxy();
  }

  private void zoomOut() {
    cameraManager.zoomOut();
    updateProxy();
  }

  public void setPanEnabled(boolean panEnabled) {
    this.panEnabled = panEnabled;
  }

  public void panCamera(float x, float y){
    cameraManager.translate(x, y);
  }

  private void closeItemMenu(){
    if (itemMenu != null){
      itemMenu.close();
      itemMenu = null;
    }
  }

  public void panCamera(Translation translation) {
    cameraManager.translate(translation);
    toolManager.getActiveTool().ifPresent((t) -> {
      if (t.getType() == ToolType.Brush){
        Vector2 gameCoord = screenToStageCoordinates(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
        Brush b = (Brush)t;
        if (b.isDrawing()){
          b.handleDrag(gameCoord.x, gameCoord.y);
        }
      } else if (t.getType() == ToolType.Deletion){
        DeletionTool dt = (DeletionTool)t;
        if (dt.isDeleting()){
          Vector2 gameCoord = screenToStageCoordinates(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
          dt.handleDrag(gameCoord.x, gameCoord.y);
        }
      }
    });
  }

  public void reloadItems() {
    MapLoader.reloadTextures(metadata);
    metadata.objectDirectory.ifPresent((uo) -> ItemLoader.LoadUserObjects(uo));

    // update placed items
    grid.items.values().forEach(i -> {
      i.reloadTexture();
    });

    // update UI
    ui.getRoot().remove();
    itemDrawer = new ItemDrawer(this);
    toolManager = new ToolManager(this, itemDrawer);
    ui = new EditorUI(this, toolManager, dragManager, history, itemDrawer);
    addUI(ui.getRoot());
  }

  //---------------------------------------------------------------------------
  // Render Logic
  //---------------------------------------------------------------------------

  public void setGridView(GridView view){
    gridView = view;
  }

  public void clearGridView(){
    if (gridView != null) gridView.reset(grid.placedItems.values());
    gridView = null;
  }

  public void applyGridView(){
    if (gridView != null){
      Collection<Item> placedItems = grid.placedItems.values();
      gridView.apply(placedItems);
    }
  }

  /**
   *
   * Draw some {@link Drawable} on top of everthing in on the game stage
   *
   * @see LevelEditor#removeOverlayDrawable(Drawable)
   *
   * @param drawable to add
   */
  public void addOverlayDrawable (Drawable drawable){
    overlayDrawables.add(drawable);
  }

  /**
   *
   * Remove some {@link Drawable} from the overlay
   *
   * @see LevelEditor#addOverlayDrawable(Drawable)
   *
   * @param drawable to remove
   */
  public void removeOverlayDrawable (Drawable drawable){
    overlayDrawables.remove(drawable);
  }

  /**
   * Select a single item
   *
   * Clears any bulk selection
   *
   * @param item to select
   */
  private void selectItem(Item item){
    selectedItem.ifPresent((i) -> i.setSelected(false));
    selectedItem = Optional.of(item);

    getSelectionTool().getSelection().ifPresent((s) -> s.clearSelectedItems());

    ui.getHints().itemHint.setMode(ItemHintMode.Selected);
    item.setSelected(true);

    BackManager.addBack(() -> {
      if(selectedItem.isEmpty() || (selectedItem.get().getID() != item.getID()))
        return false;

      item.setSelected(false);
      selectedItem = Optional.empty();
      ui.getHints().itemHint.setMode(ItemHintMode.Hover);
      return true;
    });
  }

  @Override
  public void draw() {
    applyGridView();
    draw(false);
    overlayDrawables.forEach(d -> d.draw(batch));
    batch.end();
  }


  //----------------------------------------------------------------------------
  // KEYBINDS
  //----------------------------------------------------------------------------

  public boolean shouldHandleTouchDown(){
    int x = Gdx.input.getX();
    int y = Gdx.input.getY();

    Vector2 uiPos = getUIStage().screenToStageCoordinates(new Vector2(x, y));

    if (collidesMenu(uiPos.x, uiPos.y)){
      return false;
    }

    return panEnabled && toolManager.getActiveTool().isEmpty() && itemDrawer.getSelection().isEmpty();
  }

  private void registerEditBinds() {
    addListener(new InputListener(){
      public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY) {
        if (amountY < 0){
          cameraManager.zoomOut();
        } else if (amountY > 0){
          cameraManager.zoomIn();
        }
        return true;
      }

      public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        if (!shouldHandleTouchDown() || button != Buttons.LEFT) return false;

        getUIStage().setScrollFocus(null);

        movingItem = priorityItemAt((int) x, (int) y).map((i) -> new SoloMoveTool(i, LevelEditor.this));
        if (movingItem.isEmpty() && panEnabled){
          ((PanTool)toolManager.getTool(ToolType.Pan)).handleTouchDown();
        }
        return true;
      }

      public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
        if (dragging){
          movingItem.ifPresent((mt) -> mt.handleUp(x, y));
        }
        movingItem = Optional.empty();
        dragging = false;
        CursorManager.restoreDefault();
      }

      public void touchDragged(InputEvent event, float x, float y, int pointer) {
        movingItem.ifPresentOrElse((mt) -> {
          selectedItem.ifPresent((si) -> {
            if (si.getID() != mt.getItem().getID()){
              selectItem(mt.getItem());
            }
          });
          mt.handleDrag(x, y);
          dragging = true;
        }, 
          () -> {
            if (panEnabled) {
              ((PanTool)toolManager.getTool(ToolType.Pan)).handleDrag();
              CursorManager.setCursor(CursorType.Hand);
            }
          }
        );
      }
    });

    BackManager.setOnEmpty(() -> {
      new EditorSettingsDialog().show(getUIStage());
    });

    ControlsManager.registerInput(Input.ZOOM_OUT, InputType.HELD, () -> zoomOut());
    ControlsManager.registerInput(Input.ZOOM_IN, InputType.HELD, () -> zoomIn());
    ControlsManager.registerInput(Input.MOVE_UP, InputType.HELD, () -> panCamera(Translation.Up));
    ControlsManager.registerInput(Input.MOVE_LEFT, InputType.HELD, () -> panCamera(Translation.Left));
    ControlsManager.registerInput(Input.MOVE_DOWN, InputType.HELD, () -> panCamera(Translation.Down));
    ControlsManager.registerInput(Input.MOVE_RIGHT, InputType.HELD, () -> panCamera(Translation.Right));

    ControlsManager.registerInput(Input.KEYBIND_HELP, InputType.PRESSED, () -> new KeyMapDialog().show(getUIStage()));
    ControlsManager.registerInput(Input.ROTCW, InputType.PRESSED, () -> {
      if (movingItem.isPresent()) {
        Item i = movingItem.get().getItem();
        i.rotateBy(90);
        recordEditorAction(new RotationAction(i));
        return;
      }

      int x = Gdx.input.getX();
      int y = Gdx.input.getY();
      IntVector2 pos = IntVector2.fromVector2(screenToStageCoordinates(new Vector2(x, y)));

      priorityItemAt(pos.x, pos.y).ifPresent((i) -> {
        i.rotateBy(90);
        recordEditorAction(new RotationAction(i));
      });
    });

    ControlsManager.registerInput(Input.ROTCCW, InputType.PRESSED, () -> {
      if (movingItem.isPresent()) {
        Item i = movingItem.get().getItem();
        i.rotateBy(-90);
        recordEditorAction(new RotationAction(i));
        return;
      }

      int x = Gdx.input.getX();
      int y = Gdx.input.getY();
      IntVector2 pos = IntVector2.fromVector2(screenToStageCoordinates(new Vector2(x, y)));

      priorityItemAt(pos.x, pos.y).ifPresent((i) -> {
        i.rotateBy(-90);
        recordEditorAction(new RotationAction(i));
      });
    });

    ControlsManager.registerInput(Input.MIRROR_H, InputType.PRESSED, () -> {
      if (movingItem.isPresent()) {
        movingItem.get().getItem().mirrorHorizontal();
        return;
      }

      int x = Gdx.input.getX();
      int y = Gdx.input.getY();
      IntVector2 pos = IntVector2.fromVector2(screenToStageCoordinates(new Vector2(x, y)));

      priorityItemAt(pos.x, pos.y).ifPresent((i) -> {
        i.mirrorHorizontal();
      });

    });
    ControlsManager.registerInput(Input.MIRROR_V, InputType.PRESSED, () -> {
      if (movingItem.isPresent()) {
        movingItem.get().getItem().mirrorVertical();
        return;
      }

      int x = Gdx.input.getX();
      int y = Gdx.input.getY();
      IntVector2 pos = IntVector2.fromVector2(screenToStageCoordinates(new Vector2(x, y)));

      priorityItemAt(pos.x, pos.y).ifPresent((i) -> {
        i.mirrorVertical();
      });

    });
    ControlsManager.registerInput(Input.TOOL_PAN, InputType.PRESSED, () -> toolManager.setTool(ToolType.Pan));
    ControlsManager.registerInput(Input.TOOL_FILL, InputType.PRESSED, () -> {
      if (toolManager.canFill()){
        toolManager.setTool(ToolType.Fill);
      } 
    });

    ControlsManager.registerInput(Input.COPY, InputType.PRESSED, () -> {
      if (selectedItem.isEmpty()) return;

      EyeDropTool itmCpy = (EyeDropTool)toolManager.getTool(ToolType.EyeDrop);
      setActiveTool(itmCpy);
      itmCpy.copyItem(selectedItem.get());
    });

    ControlsManager.registerInput(Input.PASTE, InputType.PRESSED, () -> {
      Vector2 screenPos = new Vector2(Gdx.input.getX(), Gdx.input.getY());
      Vector2 position = screenToStageCoordinates(screenPos);

      EyeDropTool itmCpy = (EyeDropTool)toolManager.getTool(ToolType.EyeDrop);

      itmCpy.pasteAt(position);
    });

    ControlsManager.registerInput(Input.TOOL_ROT, InputType.PRESSED, () -> toolManager.setTool(ToolType.Rotation));
    ControlsManager.registerInput(Input.TOOL_MOV, InputType.PRESSED, () -> toolManager.setTool(ToolType.Move));
    ControlsManager.registerInput(Input.TOOL_PPT, InputType.PRESSED, () -> toolManager.setTool(ToolType.Properties));
    ControlsManager.registerInput(Input.TOOL_SEL, InputType.PRESSED, () -> toolManager.setTool(ToolType.ItemSelect));
    ControlsManager.registerInput(Input.TOOL_DEL, InputType.PRESSED, () -> toolManager.setTool(ToolType.Deletion));
    ControlsManager.registerInput(Input.TOOL_EYEDROP, InputType.PRESSED, () -> toolManager.setTool(ToolType.EyeDrop));
    ControlsManager.registerInput(Input.UNDO, InputType.PRESSED, () -> history.undo());
    ControlsManager.registerInput(Input.REDO, InputType.PRESSED, () -> history.redo());



    /**
     * Delete selection:
     *  - Deletes the current selection of the selection tool AND,
     *  - the currently selected item
     */
    ControlsManager.registerInput(Input.DELETE_SELECTION, InputType.PRESSED, () -> {
      toolManager.getSelectTool().deleteSelected();

      if (selectedItem.isPresent()){
        selectedItem.get().remove();
        recordEditorAction(new Deletion(this, selectedItem.get()));
        selectedItem = Optional.empty();
      }
      
    });

    ControlsManager.registerInput(Input.SELECT_MULTI, InputType.PRESSED, () -> {
      if (!shouldHandleTouchDown()) return;

      closeItemMenu();

      Vector2 screenTouchDown = new Vector2(Gdx.input.getX(), Gdx.input.getY());
      Vector2 position = screenToStageCoordinates(screenTouchDown);

      nextItemAt((int)position.x, (int)position.y).ifPresent((item) -> {
        Array<Item> multiSelect = getSelectionTool().getSelection().map((s) -> s.getItems()).orElseGet(() -> new Array<>());

        selectedItem.ifPresent((i) -> multiSelect.add(i));
        selectedItem = Optional.empty();

        // If we already have this item selected, remove it
        if (item.selected){
          multiSelect.removeValue(item, false);
          return;
        }

        item.setSelected(true);
        // Add it 
        multiSelect.add(item);
        getSelectionTool().setItems(multiSelect);

        BackManager.addBack(() -> {
          if(getSelectionTool().getSelection().isEmpty())
            return false;


          getSelectionTool().getSelection().get().clearSelectedItems();
          ui.getHints().itemHint.setMode(ItemHintMode.Hover);
          return true;
        });
      });
    });

    ControlsManager.registerInput(Input.SELECT, InputType.PRESSED, () -> {
      if (!shouldHandleTouchDown()) return;

      closeItemMenu();

      Vector2 screenTouchDown = new Vector2(Gdx.input.getX(), Gdx.input.getY());
      Vector2 position = screenToStageCoordinates(screenTouchDown);

      nextItemAt((int)position.x, (int)position.y).ifPresent((item) -> {
        selectItem(item);
      });
    });

    ControlsManager.registerInput(Input.CONTEXT, InputType.PRESSED, () -> {
      if (!shouldHandleTouchDown()) return;

      closeItemMenu();

      Vector2 screenTouchDown = new Vector2(Gdx.input.getX(), Gdx.input.getY());
      Vector2 position = screenToStageCoordinates(screenTouchDown);

      if (selectedItem.isPresent() && selectedItem.get().getOccupiedRegion().contains(position)) {
        openItemMenu(selectedItem.get());
        return;
      }

      nextItemAt((int) position.x, (int) position.y).ifPresent((item) -> {
        openItemMenu(item);
      });

    });
  }
}
