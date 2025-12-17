package cs1396.escaperoom.editor.ui;

import java.util.Arrays;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import cs1396.escaperoom.editor.core.ActionHistory;
import cs1396.escaperoom.editor.core.DragManager;
import cs1396.escaperoom.editor.core.ToolManager;
import cs1396.escaperoom.editor.tools.ItemDrawer;
import cs1396.escaperoom.editor.tools.ItemDrawer.ItemCategory;
import cs1396.escaperoom.editor.tools.ItemDrawer.ItemNode;
import cs1396.escaperoom.editor.tools.ToolButton;
import cs1396.escaperoom.editor.ui.Menu.MenuEntry;
import cs1396.escaperoom.editor.ui.Menu.MenuEntry.MenuInputOptions;
import cs1396.escaperoom.editor.ui.Menu.MenuEntryBuilder;
import cs1396.escaperoom.engine.assets.items.ItemLoader.LoadedObjects;
import cs1396.escaperoom.engine.assets.items.ItemTypeData;
import cs1396.escaperoom.engine.assets.maps.GameSettings;
import cs1396.escaperoom.engine.assets.maps.MapSaver;
import cs1396.escaperoom.engine.types.IntVector2;
import cs1396.escaperoom.game.entities.Item;
import cs1396.escaperoom.game.entities.properties.PropertyType;
import cs1396.escaperoom.game.entities.properties.Stylable;
import cs1396.escaperoom.game.entities.properties.values.Style;
import cs1396.escaperoom.screens.ItemEditor;
import cs1396.escaperoom.screens.LevelEditor;
import cs1396.escaperoom.screens.utils.ScreenManager;
import cs1396.escaperoom.ui.FontManager;
import cs1396.escaperoom.ui.notifications.Notifier;
import cs1396.escaperoom.ui.widgets.G24Label;
import cs1396.escaperoom.ui.widgets.G24TextButton;

public class EditorUI {
  private final LevelEditor screen;
  private final EditorUITable root;
  private final ItemDrawer itemDrawer;
  private final Hints hints;
  private final Table toolDrawer = new Table();

  private class EditorUITable extends Table {
    EditorUITable() {
      super();
      screen.addCaptureListener(new InputListener() {
        @Override
        public boolean mouseMoved(InputEvent event, float x, float y) {
          IntVector2 pos = new IntVector2(x, y);
          hints.coordHints.update(pos.x, pos.y);
          hints.itemHint.update(pos.x, pos.y);
          return false;
        }
      });
    }
  }
  public class Hints extends HorizontalGroup {
    public final ItemHint itemHint = new ItemHint();
    public final CoordHints coordHints = new CoordHints();

    public Hints(){
      space(10);
      addActor(coordHints);
      addActor(itemHint);
    }
  }

  public enum ItemHintMode {
    Hover,
    Selected,
  }
  public class ItemHint extends G24Label {
    ItemHintMode hintMode = ItemHintMode.Hover;

    ItemHint(){
      super ("<no item> ID: -", "bubble");

      LabelStyle smallStyle = new LabelStyle(LevelEditor.skin.get("bubble", LabelStyle.class));
      smallStyle.font = FontManager.size(16);
      setStyle(smallStyle);
    }

    public void update(int x, int y) {
      switch (hintMode){
      case Hover:
        setItem(screen.priorityItemAt(x, y).orElse(null));
        break;
      case Selected:
        setItem(screen.getSelectedItem().orElse(null));
        break;
      }
    }

    private void setItem(Item i){
      if (i == null) {
        setText("<no item> ID: -");
        return;
      }
      setText(i.getItemName() + " ID: " + i.getID());
    }

    public void setMode(ItemHintMode mode){
      hintMode = mode;
      float x = Gdx.input.getX();
      float y = Gdx.input.getY();
      IntVector2 pos = IntVector2.fromVector2(screen.screenToStageCoordinates(new Vector2(x,y)));
      update(pos.x, pos.y);
    }
    
  }

  public class CoordHints extends HorizontalGroup {
    private G24Label coords = new G24Label("x: _ y: _", "bubble");
    private final int maxX, maxY;

    public CoordHints() {

      LabelStyle smallStyle = new LabelStyle(LevelEditor.skin.get("bubble", LabelStyle.class));
      smallStyle.font = FontManager.size(16);
      coords.setStyle(smallStyle);

      maxX = LevelEditor.WORLD_WIDTH - 1;
      maxY = LevelEditor.WORLD_HEIGHT - 1;
      space(5);
      addActor(coords);
    }

    public void update(Rectangle r) {
      this.coords.setText("x: " + (int) r.getX() + " y: " + (int) r.getY() + " width: " + (int) r.getWidth()
          + " height: " + (int) r.getHeight());
    }

    public void update(int x, int y) {

      if (x < 0 || x > maxX || y < 0 || y > maxY) {
        this.coords.setText("x: - y: -");
      } else {
        this.coords.setText("x: " + x + " y: " + y);
      }

    }
  }

  public Hints getHints() {
    return this.hints;
  }

  public EditorUI(LevelEditor screen, ToolManager tools, DragManager drag, ActionHistory history,
      ItemDrawer drawer) {
    this.itemDrawer = drawer;
    this.screen = screen;
    this.root = new EditorUITable();
    this.hints = new Hints();
    buildItemDrawer(screen);
    buildToolDrawer(tools, history);
    buildUI(screen);
  }

  public boolean clearSelections() {
    if (!itemDrawer.getSelection().isEmpty()) {
      itemDrawer.getSelection().clear();
      return true;
    }
    return false;
  }

  private void buildUI(LevelEditor screen) {
    root.defaults();
    root.setFillParent(true);
    root.top().left();


    Menu sideBar = new Menu(null, screen.getMetadata().name , screen, true);
    sideBar.defaults().width(250);
    sideBar.setMovable(false);

    GameSettings settings = screen.getMetadata().gameSettings;
    sideBar.add(
      new MenuEntryBuilder(sideBar, "Game Settings")
        .spawns((p) -> {
          Menu m = new Menu(p, "Game Options", screen);
          m.add(MenuEntry.toggle(m,
              "Persistent FOW",
              new MenuInputOptions<Boolean>(
                settings.persistentReveal,
                (val) -> settings.persistentReveal = val
              ))
          ).row();
          m.addDivider();
          m.add(MenuEntry.label("Default Zoom")
              .withToolTip("The zoom the player starts the game at.\nHigher is more zoomed out.")
          ).row();
          m.add(MenuEntry.floatInput(m,
            new MenuInputOptions<Float>(
              settings.defaultZoom,
              (val) -> {
                settings.defaultZoom =  MathUtils.clamp(val, 0.1f, 10.0f);
              }
            ))
          ).row();

          m.addDivider();

          m.add(MenuEntry.toggle(m,
              "Use default items",
              new MenuInputOptions<Boolean>(
                settings.usesDefaultItems,
                (val) -> {
                  settings.usesDefaultItems = val;
                }
              ))
          ).row();
        
          m.pack();
          return m;
         }) 
        .build()
    ).row();

    sideBar.addDivider();
    sideBar.add(MenuEntry.sectionLabel("Tools")).row();
    sideBar.addDivider();
    sideBar.add(toolDrawer).center().pad(0).row();

    sideBar.addDivider();
    sideBar.add(MenuEntry.sectionLabel("Items")).row();
    sideBar.addDivider();

    // add item drawer
    ScrollPane scrollWrapper = new ScrollPane(itemDrawer);
    sideBar.add(scrollWrapper).growY().padTop(10).padBottom(10).row();


    /**
     * Improves scroll ux 
     *
     * Without this listener, the use has to click off of the 
     * item drawer to allow the input event to be seen 
     * by the level editor
     */
    scrollWrapper.addListener(
        new InputListener() {
          @Override
          public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
            screen.getUIStage().setScrollFocus(scrollWrapper);
          }

          @Override
          public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
            screen.getUIStage().setScrollFocus(null);
          }
        }
    );


    G24TextButton playButton = new G24TextButton("Play");
    playButton.setProgrammaticChangeEvents(false);
    playButton.addListener(new ChangeListener() {
      public void changed(ChangeEvent event, Actor actor) {
        if (playButton.isChecked()) {
          if (MapSaver.saveMap(screen.getMapData())) {
            screen.setPlayMode();
            playButton.setChecked(false);
          } else {
            Notifier.warn("Failed to save map");
          }
        }
      }
    });

    G24TextButton saveButton = new G24TextButton("Save");
    saveButton.setProgrammaticChangeEvents(false);
    saveButton.addListener(new ChangeListener() {
      public void changed(ChangeEvent event, Actor actor) {
        if (saveButton.isChecked()) {
          if (MapSaver.saveMap(screen.getMapData())) {
            Notifier.info("Map saved");
          } else {
            Notifier.warn("Map failed to save");
          }
          saveButton.setChecked(false);
        }
      }
    });

    G24TextButton newItemBtn = new G24TextButton("  New Item");
    newItemBtn.setProgrammaticChangeEvents(false);
    newItemBtn.addListener(new ChangeListener() {
      public void changed(ChangeEvent event, Actor actor) {
        if (newItemBtn.isChecked()) {
          if (MapSaver.saveMap(screen.getMapData())){
            ScreenManager.instance().showScreen(new ItemEditor(screen.getMapData(), null));
          } else {
            Notifier.error("Failed to save map");
          }
          newItemBtn.setChecked(false);
        }
      }
    });

    G24TextButton reloadBtn = new G24TextButton("  Reload Items");
    reloadBtn.setProgrammaticChangeEvents(false);
    reloadBtn.addListener(new ChangeListener() {
      public void changed(ChangeEvent event, Actor actor) {
        screen.reloadItems();

        Notifier.error("reloaded all items");
        reloadBtn.setChecked(false);
      }
    });

    HorizontalGroup hg = new HorizontalGroup();
    hg.addActor(saveButton);
    hg.addActor(playButton);

    VerticalGroup vg = new VerticalGroup();
    vg.addActor(newItemBtn);
    vg.addActor(reloadBtn);
    vg.grow();
    sideBar.add(vg).center().pad(0);

    root.add(sideBar).left().top().growY();
    root.add(hints).bottom().left().expandX().padLeft(5).padBottom(5);
    root.add(hg).expandX().right().bottom().padRight(5);
  }

  private void buildToolDrawer(ToolManager toolManager, ActionHistory history) {
    toolDrawer.defaults().center().pad(2);

    int count = 0;

    for (ToolButton tb : toolManager.getToolButons()) {
      toolDrawer.add(tb);
      count++;
      if (count > 0 && count % 4 == 0){
        toolDrawer.row();
      }
    }

    toolDrawer.row();
    toolDrawer.add(history.getUndoButton());
    toolDrawer.add(history.getRedoButton());

    toolDrawer.pack();

  }


  /**
   * Build the Item Drawer
   */
  private void buildItemDrawer(LevelEditor screen) {
    boolean usesDefaultItems = screen.getMetadata().gameSettings.usesDefaultItems;

    String[] categories;
    if (usesDefaultItems) {
      categories = LoadedObjects.getCategories().toArray(new String[0]);
    } else {
      categories = LoadedObjects.getUserCategories().toArray(new String[0]);
    }

    Arrays.sort(categories);
    // For all our defined categories
    for (String category : categories) {
      // Add an category to the drawer
      ItemCategory objCategory = itemDrawer.new ItemCategory(category);
      itemDrawer.add(objCategory);

      ItemTypeData[] typeDatas;
      if (usesDefaultItems) {
        typeDatas = LoadedObjects.getItems(category).toArray(new ItemTypeData[0]);
      } else {
        typeDatas = LoadedObjects.getUserItems(category).toArray(new ItemTypeData[0]);
      }

      Arrays.sort(typeDatas, (a, b) -> {
        return a.name.compareTo(b.name);
      });

      // For all the items defined in that category
      for (ItemTypeData itemType : typeDatas) {
        // Create a template item
        Item i = new Item(itemType);

        // If that item is styable
        if (i.hasProperty(PropertyType.Stylable)) {

          Stylable prop = i.getProperty(PropertyType.Stylable, Stylable.class).get();

          // Add all the available styles to the category
          for (Style s : prop.getPotentialValues()) {

            Item styleItem = new Item(itemType);
            styleItem.getProperty(PropertyType.Stylable, Stylable.class).get().set(s);

            ItemNode n = itemDrawer.new ItemNode(styleItem);
            objCategory.add(n);
          }
        } else {
          // Otherwise, just add the item
          ItemNode n = itemDrawer.new ItemNode(i);
          objCategory.add(n);

        }
      }
    }
  }

  public Table getRoot() {
    return root;
  }
}
