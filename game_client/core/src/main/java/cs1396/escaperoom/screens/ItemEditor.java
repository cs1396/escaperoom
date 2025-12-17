package cs1396.escaperoom.screens;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Null;

import cs1396.escaperoom.editor.item.ui.ItemSideBar;
import cs1396.escaperoom.editor.item.ui.PropertyBank;
import cs1396.escaperoom.editor.item.ui.PropertyConfigurationMenu;
import cs1396.escaperoom.editor.item.ui.PropertyWorkspace;
import cs1396.escaperoom.editor.ui.Menu;
import cs1396.escaperoom.engine.BackManager;
import cs1396.escaperoom.engine.assets.AssetManager;
import cs1396.escaperoom.engine.assets.items.ItemSaver;
import cs1396.escaperoom.engine.assets.items.ItemTypeData;
import cs1396.escaperoom.engine.assets.maps.MapData;
import cs1396.escaperoom.engine.assets.maps.MapLoader;
import cs1396.escaperoom.engine.assets.maps.MapSaver;
import cs1396.escaperoom.engine.control.ControlsManager;
import cs1396.escaperoom.engine.control.ControlsManager.InputType;
import cs1396.escaperoom.engine.control.input.Input;
import cs1396.escaperoom.engine.types.Size;
import cs1396.escaperoom.game.entities.Item;
import cs1396.escaperoom.game.entities.properties.PropertyType;
import cs1396.escaperoom.game.entities.properties.values.ItemPropertyValue;
import cs1396.escaperoom.screens.utils.CamMan;
import cs1396.escaperoom.screens.utils.CamMan.Translation;
import cs1396.escaperoom.screens.utils.ScreenManager;
import cs1396.escaperoom.ui.ConfirmDialog;
import cs1396.escaperoom.ui.notifications.Notifier;

public class ItemEditor extends AbstractScreen {
  private Image room;
  protected final Batch batch;
  private Item newItem;
  private Item originalItem;
  private MapData mapData;
  private boolean dirty = false;
  private CamMan cameraManager;
  private Texture background;
  private int ROOM_W = 9,
              ROOM_H = 9; 
  private static final int BANK_HEIGHT = 300,
                          BAR_WIDTH = 250;
  private Table rootTable;
  private DragAndDrop dragAndDrop;
  public Menu itemMenu;
  public ItemSideBar itemSidebar;
  static PropertyBank bank;
  static Container<Menu> itemMenuContainer;
  public static ItemEditor screen;
  private boolean modifyingItem;
  private PropertyConfigurationMenu configurationMenu;

  public Item getNewItem(){
    return newItem;
  }

  public DragAndDrop getDragAndDrop(){
    return dragAndDrop;
  }


  public static ItemEditor get(){
    return screen;
  }

  public void markModified(){
    this.dirty = true;
  }

	public ItemEditor(MapData data, @Null Item target) {
    super();

    // Field initialization
    batch = new SpriteBatch();
    rootTable = new Table();
    screen = this;
    mapData = data;
    dragAndDrop = new DragAndDrop();
    cameraManager = new CamMan((OrthographicCamera)getViewport().getCamera());

    if (target == null){ 
      modifyingItem = false;
      initEmptyItem();

    } else {
      modifyingItem = true;

      originalItem = target;
      originalItem.remove(true);
      newItem = originalItem.clone(true);
    };

    fillPropertyParams();
    addRoom();
    registerBinds();

    rootTable.defaults().pad(0);
    rootTable.setFillParent(true);
    rootTable.top().left();
    itemMenu = new Menu(null, "Item Values", null);
    itemMenuContainer = new Container<>(itemMenu);
    itemMenuContainer.top().left();
    itemSidebar = new ItemSideBar();
    itemMenu.add(itemSidebar).top().padRight(20).padLeft(10).growY().minWidth(BAR_WIDTH);
    itemMenu.setMovable(false);

    AssetManager.instance().load("textures/menu_hover.png", Texture.class);
    AssetManager.instance().finishLoadingAsset("textures/menu_hover.png");
    Texture bkg = AssetManager.instance().get("textures/menu_hover.png", Texture.class);
    TextureRegionDrawable hoverBackground = new TextureRegionDrawable(new TextureRegion(bkg));

    itemMenuContainer.setBackground(hoverBackground);
    rootTable.add(itemMenuContainer).top().left().growY();

    bank = new PropertyBank();

    rootTable.add(bank).maxHeight(BANK_HEIGHT).bottom().left().growX().growY();

    itemSidebar.populateFor(newItem);

    addUI(rootTable);

    BackManager.setOnEmpty(() -> {
      if (modifyingItem){
        returnToEditor();
      } else {
        if (dirty){
          new ConfirmDialog.Builder("Changes are not saved!")
            .withButton("Continue Editing", () -> true)
            .cancelText("Discard Changes")
            .onCancel(() -> {
              ItemEditor.get().resetItem();
              returnToEditor();
              return true;
            })
            .confirmText("Save")
            .onConfirm(() -> {
              saveItem();
              returnToEditor();
              return true;
            })
            .build()
            .show(getUIStage());
        } else {
          returnToEditor();
        }
      }
    });
	}


  /**
   * Utility function to determine if a ui click hits a pill
   */
  PropertyWorkspace.PropertyPill<?,?> hitsPill(Vector2 uiCoords){
    for (PropertyWorkspace.PropertyPill<?,?> a : itemSidebar.workspace.getPills()){
      Vector2 origin = a.localToStageCoordinates(new Vector2(0,0));
      Rectangle bounds = new Rectangle(origin.x, origin.y, a.getWidth(), a.getHeight());

      if (!bounds.contains(uiCoords)) continue;

      return a;
    }
    return null;
  }

  public boolean mouseCollidesMenu(){
    float x = Gdx.input.getX();
    float y = Gdx.input.getY();

    Vector2 uiPos = getUIStage().screenToStageCoordinates(new Vector2(x, y));

    x = uiPos.x;
    y = uiPos.y;

    for (Actor a : getUIStage().getRoot().getChildren()){
      if (a instanceof Menu){
        Menu m = (Menu)a;
        Rectangle bounds = new Rectangle(m.getX(), m.getY(), m.getWidth(), m.getHeight());
        if (bounds.contains(new Vector2(x,y))){
          return true;
        }
      }
    }
    return false;
  }

  private void registerBinds(){

    addListener(new InputListener() {
      @Override
      public boolean scrolled(InputEvent event, float x, float y, float amountseX, float amountY) {
        if (amountY < 0) {
          cameraManager.zoomOut();
        } else if (amountY > 0) {
          cameraManager.zoomIn();
        }
        return true;
      }

      @Override
      public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        if (!mouseCollidesMenu() &&
             configurationMenu != null){
          configurationMenu.close();
          configurationMenu = null;
          return true;
        }
        return false;
      }

    });


    ControlsManager.registerInput(Input.CONTEXT, InputType.PRESSED, ()->{
      Vector2 screenCoords = new Vector2(Gdx.input.getX(), Gdx.input.getY());
      Vector2 uiCoords = getUIStage().screenToStageCoordinates(screenCoords);

      PropertyWorkspace.PropertyPill<?,?> pill;
      if ((pill = hitsPill(uiCoords)) == null) return;

      configurationMenu = new PropertyConfigurationMenu(pill.getProperty());
      configurationMenu.setPosition(uiCoords.x, uiCoords.y);
      addUI(configurationMenu);
      BackManager.addBack(() -> {
        if (configurationMenu == null || configurationMenu.getStage() == null) return false;

        configurationMenu.close();
        configurationMenu = null;
        return true;
      });
    });

    ControlsManager.registerInput(Input.ZOOM_OUT, InputType.HELD, () -> cameraManager.zoomOut());
    ControlsManager.registerInput(Input.ZOOM_IN, InputType.HELD, () -> cameraManager.zoomIn());
    ControlsManager.registerInput(Input.MOVE_UP, InputType.HELD, () -> cameraManager.translate(Translation.Up));
    ControlsManager.registerInput(Input.MOVE_LEFT, InputType.HELD, () ->  cameraManager.translate(Translation.Left));
    ControlsManager.registerInput(Input.MOVE_DOWN, InputType.HELD, () ->  cameraManager.translate(Translation.Down));
    ControlsManager.registerInput(Input.MOVE_RIGHT, InputType.HELD, () -> cameraManager.translate(Translation.Right));
  }

  /**
   * Repack the root UI elements in the editor
   */
  public void repack(){
    itemMenu.pack();
    itemMenuContainer.pack();
    bank.layout();
    rootTable.pack();
  }

  /**
   * Initialze a blank item for editing
   */
  private void initEmptyItem(){
    ItemTypeData blank = new ItemTypeData("Blank", "None", new Size(1, 1), "placeholder", 0, new HashMap<>());
    newItem = new Item(blank);
  }

  private void addRoom(){
    // Add gridded room
    AssetManager.instance().load("textures/tile.png", Texture.class);
    AssetManager.instance().finishLoading();
    Texture tile = AssetManager.instance().get("textures/tile.png", Texture.class);
    tile.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
    TextureRegion roomTexture = new TextureRegion(tile);
    roomTexture.setRegion(0, 0, tile.getWidth() * ROOM_W, tile.getHeight() * ROOM_H);

    room = new Image(roomTexture);
    room.setSize(ROOM_W, ROOM_H);
    room.setName("room");
    if (room.getStage() == null){
      addActor(room);
    }
    updateRoom();

    AssetManager.instance().load("textures/itemeditor_bkg.png", Texture.class);
    AssetManager.instance().finishLoading();
    background = AssetManager.instance().get("textures/itemeditor_bkg.png", Texture.class);
    addSprite(background, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
  }

  public void resetItem(){
    // reinitialize item to original so we can remove it from the grid
    if (modifyingItem){
      newItem = originalItem.clone(true);
      fillPropertyParams();
    } else {
      initEmptyItem();
    }

    itemSidebar.populateFor(newItem);
    updateItemPosition();
  }

  public MapData getMapData(){
    return mapData;
  }

  private void transferProperties(){
    if(!modifyingItem) return;

    // For each of the new items properties, see if the old item had defined
    // values. If so, move that value in this new item.
    newItem.getProperties().forEach((newProp) -> {
      originalItem.getProperty(newProp.getType()).ifPresent((oldProp)->{
        ItemPropertyValue val = oldProp.getCurrentValue();
        if (val != null){
          newProp.unsafeSet(val);
          return;
        }

        Array<? extends ItemPropertyValue> vals = oldProp.getCurrentValues();
        Array<ItemPropertyValue> downCastVals = new Array<>();

        vals.forEach((v) -> downCastVals.add(v));
        newProp.unsafeSet(downCastVals);
      });
    });
  }


  public void returnToEditor(){

    // We may have newly defined objects, reload the metadata to reflect
    // that we define custom objects
    MapLoader.get(mapData.getMetadata().locations).ifPresent((metadata) ->{
      mapData.setMetadata(metadata);
    });

    if (modifyingItem){

      // Set the new item position
      newItem.setPosition(originalItem.position.x, originalItem.position.y);

      transferProperties();

      // place our modified item
      if (MapScreen.canPlace(newItem, newItem.position, mapData.getStartGrid())){
        mapData.getStartGrid().placeItem(newItem);
      } else {
        Notifier.error("Modified item no longer has a valid placement on the grid.");
        updateItemPosition();
        return;
      }

      // save the map that contains the modified item
      MapSaver.saveMap(mapData);
    } 

    // Reload the map and show the editor
    MapLoader.tryLoadMap(mapData.getMetadata()).inspect((md) -> {
      ScreenManager.instance().showScreen(new LevelEditor(md));
    });
  }

  /**
   * Update the position of the room on resize events
   */
  private void updateRoom(){
    // Everything in screen coords
    float SCREEN_W = Gdx.graphics.getWidth();
    float SCREEN_H = Gdx.graphics.getHeight();

    Vector2 viewOrigin = getUIStage().stageToScreenCoordinates(new Vector2(BAR_WIDTH, BANK_HEIGHT));
    Vector2 viewSize = new Vector2(SCREEN_W, SCREEN_H).sub(viewOrigin);

    Vector2 roomSize = stageToScreenCoordinates(new Vector2(ROOM_W, ROOM_H));

    Vector2 roomOrigin = viewOrigin.add(viewSize.x / 2, viewSize.y / 2).sub(roomSize.x / 2, roomSize.y / 2);

    Vector2 gameCoordRoomOrigin = screenToStageCoordinates(roomOrigin);

    room.setPosition((int)gameCoordRoomOrigin.x, (int)gameCoordRoomOrigin.y);
    cameraManager.setPosition(gameCoordRoomOrigin.x, gameCoordRoomOrigin.y);
    updateItemPosition();
  }

  /**
   * Update the position of the item in the editor.
   *
   * This should be called anytime the item's size is changed
   */
  public void updateItemPosition(){
    newItem.setPosition((int)room.getX() + ROOM_W / 2 - newItem.getWidth() / 2, (int)room.getY() + ROOM_H / 2 - newItem.getHeight() / 2);
  }


  /**
   * Ensures that the propertyParameters field 
   * of this item's {@link ItemTypeData} is filled
   */
  public void fillPropertyParams(){
    HashMap<PropertyType, JsonValue> propertyParams = new HashMap<>();

    newItem.getProperties().forEach((prop) -> {
      JsonValue val = new JsonReader().parse(new Json().toJson(prop));
      propertyParams.put(prop.getType(), val);
    });

    newItem.getType().propertyParameters = propertyParams;
  }

  public boolean modifyingItem(){
    return modifyingItem;
  }

  /**
   * Save the editing item to this maps custom object folder
   */
  public void saveItem(){
    fillPropertyParams();

    if (!ItemSaver.saveCustomItem(newItem, mapData.getMetadata())){
      Notifier.error("Failed to save " + newItem.getItemName());
    } else {
      Notifier.info("Saved item " + newItem.getItemName());
      dirty = false;
    }

  }

  @Override
  public void draw() {
    super.draw();

    Camera camera = getViewport().getCamera();
    camera.update();

    Batch batch = this.batch;
    batch.setProjectionMatrix(camera.combined);
    batch.begin();
    newItem.draw(batch);
    batch.end();
  }

  @Override
  public void act(float delta) {
    super.act(delta);
    newItem.update(delta);
  }

  @Override
  public void dispose(){
    screen = null;
    super.dispose();
  }

  @Override
  public void resize(int width, int height) {
    super.resize(width, height);
    repack();
    updateRoom();
  }
}
