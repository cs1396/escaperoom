package cs1396.escaperoom.editor.tools;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;

import java.util.Optional;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Payload;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Target;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.Scaling;

import java.util.function.Predicate;

import cs1396.escaperoom.engine.assets.AssetManager;
import cs1396.escaperoom.engine.control.CursorManager;
import cs1396.escaperoom.engine.control.CursorManager.CursorType;
import cs1396.escaperoom.game.entities.properties.PropertyType;
import cs1396.escaperoom.game.entities.Item;
import cs1396.escaperoom.screens.LevelEditor;
import cs1396.escaperoom.ui.ItemSlot;
import cs1396.escaperoom.ui.widgets.G24Label;

public class ItemDrawer extends Tree<cs1396.escaperoom.editor.tools.ItemDrawer.ItemCategory, Item> {

  LevelEditor editor;
  Array<Item> prototypes = new Array<>();

  private static class ItemDrawerEntry extends Table {
    public ItemDrawerEntry(String categoryName) {
      add(new G24Label(categoryName));
    }
    public ItemDrawerEntry(Item item) {
      padLeft(0);
      defaults().pad(5).left();
      ItemSlot s  = new ItemSlot(item);
      s.setTouchable(Touchable.disabled);
      add(s);
      G24Label label = new G24Label(item.getItemName());
      label.setTouchable(Touchable.disabled);
      add(label).minWidth(100);

      if (item.hasProperty(PropertyType.Brushable) || item.hasProperty(PropertyType.TiledBrushable)) {
        Image brushImg = new Image(AssetManager.instance().getRegion("brush"));
        brushImg.setScaling(Scaling.contain);
        add(brushImg).height(25).width(25);
      }
      setTouchable(Touchable.enabled);  
    }

  }

  public class ItemCategory extends Tree.Node<ItemNode, Item, ItemDrawerEntry> {
    Optional<Brush> brush = Optional.empty();
    protected ItemCategory(ItemDrawerEntry entry) {
      super(entry);
      setSelectable(false);
    }

    public ItemCategory(String categoryName) {
      this(new ItemDrawerEntry(categoryName));
    }

    public Optional<Brush> getBrush(){
      return brush;
    }


  }

  public Array<Item> getItemPrototypes(){
    return prototypes;
  }

  public class ItemNode extends ItemCategory {
    public ItemNode(Item item) {
      this(item, false);
    }

    public ItemNode(Item item, boolean selectable) {
      super(new ItemDrawerEntry(item));
      prototypes.add(item);
      setValue(item);
      makeDraggable(this);
      setSelectable(selectable);

      if(item.hasProperty(PropertyType.Brushable)){
        setSelectable(true);
        brush = Optional.of(new ItemBrush(editor, item));
      } else if (item.hasProperty(PropertyType.TiledBrushable)){
        setSelectable(true);
        brush = Optional.of(new TiledBrush(editor, item));
      }
    }
  }

  /**
   * Convience method to configure an object node as a drag source
   */
  private void makeDraggable(ItemNode node) {
    node.getActor().addListener(CursorManager.hoverHelper(CursorType.Hand));
    editor.getDragAndDrop().addSource(new DragAndDrop.Source(node.getActor()) {
      @Override
      public Payload dragStart(InputEvent event, float x, float y, int pointer) {

        CursorManager.setCursor(CursorType.Hand);
        if (node.getValue().hasProperty(PropertyType.Unique)) {
          Predicate<Item> pred = new Predicate<Item>() {
            public boolean test(Item arg0) {
              return arg0.hasProperty(PropertyType.Unique)
                  && arg0.getType().name.equals(node.getValue().getType().name);
            }
          };
          if (editor.containsItemWhere(pred)) {
            return null;
          }
        }

        getSelection().clear();
        Payload p = new DragAndDrop.Payload();
        p.setObject(node.getValue());
        G24Label l = new G24Label(Item.class.cast(node.getValue()).getItemName(), "default", 0.65f);
        p.setDragActor(l);
        return p;
      }

      public void dragStop (InputEvent event, float x, float y, int pointer, @Null Payload payload, @Null Target target) {
        CursorManager.restoreDefault();
      }
    });
  }

  public ItemDrawer(LevelEditor editor) {
    super(LevelEditor.skin);
    setIndentSpacing(0);
    setPadding(5, 5);
    this.editor = editor;
  }
}
