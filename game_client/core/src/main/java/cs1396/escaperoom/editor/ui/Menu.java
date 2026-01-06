package cs1396.escaperoom.editor.ui;

import java.util.Objects;

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Null;

import cs1396.escaperoom.engine.BackManager;
import cs1396.escaperoom.engine.assets.AssetManager;
import cs1396.escaperoom.screens.AbstractScreen;
import cs1396.escaperoom.ui.Tooltip;
import cs1396.escaperoom.ui.widgets.G24Label;
import cs1396.escaperoom.ui.widgets.G24Label.G24LabelStyle;
import cs1396.escaperoom.ui.widgets.G24NumberInput.FloatInput;
import cs1396.escaperoom.ui.widgets.G24NumberInput.IntInput;
import cs1396.escaperoom.ui.widgets.G24Window;

public class Menu extends G24Window {
  Array<Menu> spawned = new Array<>();
  protected AbstractScreen screen;
  @Null MenuEntry parent;
  boolean movedIndependent = false;
  Vector2 parentRelativeOffset = new Vector2();

  public AbstractScreen getScreen(){
    return screen;
  }

  public @Null MenuEntry getParent(){
    return parent;
  }

  @FunctionalInterface
  public interface SpawnsMenu {
    public Menu newMenu(MenuEntry parent);
  }

  @FunctionalInterface
  public interface MenuAction {
    public void onClick();
  }

  @FunctionalInterface
  public interface SelectionHandler {
    public void handle();
  }

  /**
   * A group of Selectable Menu entries
   */
  public class MenuEntryGroup {
    Array<MenuEntry> entries = new Array<>();
    boolean multiSelect = false;

    public MenuEntryGroup(){}

    public void setMultiSelect(boolean multiSelect){
      this.multiSelect = multiSelect;
    }


    /**
     * Given a newly selected entry, maybe restrict 
     * other selected entries in this group if we don't 
     * allow multiselect
     */
    private void restrictSelection(MenuEntry entry){
      if (multiSelect) return;

      for (MenuEntry e : entries){
        if (e != entry && e.selected) {
          e.toggleSelect();
        }
      }
    }

    public Array<MenuEntry> getSelected(){
      Array<MenuEntry> selected = new Array<>();
      for (MenuEntry e : entries){
        if (e.selected) {
          selected.add(e);
        }
      }
      return selected;
    }

    public void addEntry(MenuEntry entry){
      if (entry.selectable){
        SelectionHandler handler = entry.onSelect;
        entry.onSelect = () -> {
          restrictSelection(entry);
          handler.handle();
        };

        entries.add(entry);
      }
    }
  }


  public static abstract class AbstractBuilder<M extends MenuEntry, B extends AbstractBuilder<M,B>> {
    final Menu parent;
    public AbstractBuilder(Menu parent){
      this.parent = parent;
    }

    protected abstract B self();
    public abstract M build();
  }

  public static class MenuEntryBuilder extends AbstractBuilder<MenuEntry, MenuEntryBuilder> {
    final Actor content;
    @Null SpawnsMenu menuSpawner; 
    SelectionHandler onSelect = () -> {}; 
    SelectionHandler onDeselect = () -> {} ; 
    MenuAction onClick = () -> {}; 
    boolean selectable = false;
    boolean selected = false;

    public MenuEntryBuilder(Menu parent, Actor content){
      super(parent);
      this.content = content;
    }

    public MenuEntryBuilder(Menu parent, String label){
      this(parent, new MenuLabel(label));
    }

    public MenuEntryBuilder spawns(SpawnsMenu action){
      this.menuSpawner = action;
      return this;
    }

    public MenuEntryBuilder onClick(MenuAction action){
      this.onClick = action;
      return this;
    }

    public MenuEntryBuilder onSelect(SelectionHandler action){
      this.selectable = true;
      this.onSelect = action;
      return this;
    }

    public MenuEntryBuilder onDeselect(SelectionHandler action){
      this.selectable = true;
      this.onDeselect = action;
      return this;
    }

    public MenuEntryBuilder selectable(boolean selected){
      this.selectable = true;
      this.selected = selected;
      return this;
    }

    protected MenuEntryBuilder self(){ return this; }

    public MenuEntry build(){
      MenuEntry e = new MenuEntry(parent, content, menuSpawner, onSelect, onDeselect, onClick, selectable);
      if (selected) e.setSelected();
      return e;
    }

  }

  public static class MenuLabel extends G24Label {
    public MenuLabel(String label){
      super(label, G24LabelStyle.Default);
    }
  }

  public static class MenuEntry extends Table {
    Drawable hoverBackground;
    boolean selectable = false;
    boolean selected = false;
    Menu parent;
    SelectionHandler onSelect = () -> {};
    SelectionHandler onDeselect = () -> {};
    MenuAction onClick = () -> {};
    @Null SpawnsMenu spawnsMenu;
    @Null SpawnsMenu spawnsMenuSaved;
    Actor content;

    public MenuEntry withToolTip(String helpString){
      new Tooltip.Builder(helpString).target(this, Tooltip.stageHelper(this)).build();
      return this;
    }

    public AbstractScreen getScreen(){
      return parent.getScreen();
    }

    public void childClosed(Menu menu){
      spawnsMenu = spawnsMenuSaved;
    }

    private void loadBackground(){
      AssetManager.instance().load("textures/menu_hover.png", Texture.class);
      AssetManager.instance().finishLoadingAsset("textures/menu_hover.png");
      Texture bkg = AssetManager.instance().get("textures/menu_hover.png", Texture.class);
      hoverBackground = new TextureRegionDrawable(new TextureRegion(bkg));
    }
    private static Drawable loadDividerBackground(){
      AssetManager.instance().load("textures/menu_divider.png", Texture.class);
      AssetManager.instance().finishLoadingAsset("textures/menu_divider.png");
      Texture bkg = AssetManager.instance().get("textures/menu_divider.png", Texture.class);
      return new TextureRegionDrawable(new TextureRegion(bkg));
    }

    @Deprecated
    public static MenuEntry divider(){
      MenuEntry entry = new MenuEntry();
      entry.setBackground(loadDividerBackground());
      return entry;
    }

    public interface MenuInputHandler<T> {
      public void handle(T newValue);
    }


    public record MenuInputOptions<T>(T initialValue, MenuInputHandler<T> handler){
      // this is some record syntax that happens after field initialization -> validates non-null
      public MenuInputOptions {
        Objects.requireNonNull(handler);
      }

      public MenuInputOptions(){ this(null, (val) -> {}); }
    }


    public static MenuEntry floatInput(Menu parent, MenuInputOptions<Float> options){
      FloatInput input = new FloatInput(
        options.initialValue == null ? 0 : options.initialValue,
        (v) -> options.handler.handle(v)
      );
      MenuEntry entry = new MenuEntryBuilder(parent, input).build();
      return entry;
    }

    public static MenuEntry intInput(Menu parent, MenuInputOptions<Integer> options){
      IntInput input = new IntInput(
        options.initialValue == null ? 0 : options.initialValue,
        (v) -> options.handler.handle(v)
      );
      MenuEntry entry = new MenuEntryBuilder(parent, input).build();
      return entry;
    }

    public static MenuEntry toggle(Menu parent, String label, MenuInputOptions<Boolean> options){
      CheckBox checkBox = new CheckBox(label, AbstractScreen.skin);

      MenuEntry entry = new MenuEntryBuilder(parent, checkBox)
        .onSelect(() -> {
          options.handler.handle(true);
        })
        .onDeselect(() -> {
          options.handler.handle(false);
        })
        .build();

      checkBox.setChecked(options.initialValue);
      
      if (options.initialValue) entry.setSelected();

      return entry;
    }

    public static MenuEntry label(String content){
      MenuEntry entry = new MenuEntry(null, new MenuLabel(content), null, null, null, null, false);
      return entry;
    }

    public static MenuEntry sectionLabel(String content){
      MenuEntry entry = new MenuEntry(null, new G24Label(content, G24LabelStyle.DefaultMedText), null, null, null, null, false);
      return entry;
    }

    /**
     * Does not call any callbacks, meant for reinitialization
     */
    public void setSelected(){
      selected = true;
      setBackground(hoverBackground);
    }

    private void toggleSelect(){
      if (!selectable) return;

      selected = !selected;

      if (selected) {
        onSelect.handle();
        setBackground(hoverBackground);
      };
      if (!selected) {
        onDeselect.handle();
        setBackground((Drawable)null);
      };

    }

    private MenuEntry(){}

    public void createNewMenu(Menu m){
      m.pack();
      parent.spawned.add(m);
      parent.screen.addUI(m);

      Vector2 entryBorder = MenuEntry.this.localToStageCoordinates(new Vector2(MenuEntry.this.getWidth()+2,0));
      m.setPosition(entryBorder.x, entryBorder.y);
      m.movedIndependent = false;
      m.parentRelativeOffset.set(entryBorder.x - parent.getX(),  entryBorder.y - parent.getY());
      m.toFront();

      BackManager.addBack(() -> {
        if (m.getStage() == null) return false;
        m.close();
        return true;
      });
    }

    protected MenuEntry(Menu parentMenu, Actor entryContent, 
                      SpawnsMenu spawner,
                      SelectionHandler selectHandler, 
                      SelectionHandler deselectHandler,
                      MenuAction doesAction,
                      boolean select
    ) {

      left();
      padLeft(5);
      loadBackground();
      add(entryContent).left().expandX();

      content = entryContent;
      spawnsMenu = spawner;
      selectable = select;
      onClick = doesAction;
      parent = parentMenu;
      onSelect = selectHandler;
      onDeselect = deselectHandler;

      if (spawner != null) add(new G24Label(">", G24LabelStyle.Default)).right().padRight(2);

      addListener(new InputListener() {
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) { 
          if (button == Buttons.RIGHT) return false;

          boolean handled = false;

          if (selectable) {
            toggleSelect();
            handled = true;
          } 

          // If we spawn a window, don't spawn another until it was closed.
          if (spawnsMenu != null) {
            parent.spawned.forEach((m) -> {
              m.close();
            });
            Menu m = spawnsMenu.newMenu(MenuEntry.this);
            createNewMenu(m);
            spawnsMenuSaved = spawnsMenu;
            spawnsMenu = null;
            handled = true;
          } else if (spawnsMenuSaved != null){
            parent.spawned.forEach((m) -> {
              m.close();
            });
            handled = true;
          }

          if (onClick != null){ 
            onClick.onClick();
            handled = true;
          }

          if (handled){
            event.stop();
          }

          return handled;
        }
        public void enter(InputEvent event, float x, float y, int pointer, @Null Actor fromActor) {
          if (select || spawner != null || onClick != null){
            setBackground(hoverBackground);
          }
        }
        public void exit(InputEvent event, float x, float y, int pointer, @Null Actor toActor) {
          if (!selected){
            setBackground((Drawable)null);
          }
        }
      });

    }
  }

  @Override
  protected void positionChanged() {
    movedIndependent = true;
    spawned.forEach((m) -> {
      if (m.movedIndependent) {
        return;
      }
      m.setPosition(getX() + m.parentRelativeOffset.x, getY() + m.parentRelativeOffset.y);
      m.movedIndependent = false;
    });
  }

  @Override
  public void close() {
    if (parent != null) parent.childClosed(this);

    spawned.forEach((w) -> w.close());
    super.close();
  }

	public Menu(@Null MenuEntry parentMenu, String title, AbstractScreen screen) {
    this(parentMenu, title, screen, false);
	}

	public Menu(@Null MenuEntry parentMenu, String title, AbstractScreen screen, boolean largeHeading) {
		super(title, largeHeading ? "menu-med-text" : "menu" );
    this.screen = screen;
    parent = parentMenu;

    padTop(35);
    padLeft(5);
    defaults().pad(0).growX().align(Align.topLeft);
    add(MenuEntry.divider()).growX().row();
	}

  public void divider(){
    add(MenuEntry.divider()).minHeight(20).growX().row();
  }
  
}
