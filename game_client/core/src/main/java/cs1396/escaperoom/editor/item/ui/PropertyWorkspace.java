package cs1396.escaperoom.editor.item.ui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Payload;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Source;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Target;
import com.badlogic.gdx.utils.Null;

import cs1396.escaperoom.engine.control.CursorManager;
import cs1396.escaperoom.engine.control.CursorManager.CursorType;
import cs1396.escaperoom.game.entities.Item;
import cs1396.escaperoom.game.entities.properties.PropertyType;
import cs1396.escaperoom.game.entities.properties.base.ItemProperty;
import cs1396.escaperoom.game.entities.properties.values.ItemPropertyValue;
import cs1396.escaperoom.screens.AbstractScreen;
import cs1396.escaperoom.screens.ItemEditor;
import cs1396.escaperoom.ui.widgets.G24Label;

public class PropertyWorkspace extends ScrollPane {
  // private HashMap<PropertyType, YogaNode> nodes = new HashMap<>();
  private HashSet<PropertyPill<?,?>> pills = new HashSet<>();
  private Table table;

  public class PropertyPill<V extends ItemPropertyValue, P extends ItemProperty<V>> extends G24Label {

    P property;

    public P getProperty(){
      return property;
    }

    public PropertyPill(P property) {
      super(property.getDescription().name, "bubble", 0.65f);
      addListener(CursorManager.hoverHelper(CursorType.Hand));
      this.property = property;

      ItemEditor.get().getDragAndDrop().addSource(new DragAndDrop.Source(this) {
        @Override
        public Payload dragStart(InputEvent event, float x, float y, int pointer) {
          Payload pl = new Payload();
          pl.setObject(property.getType());
          G24Label l = new G24Label(property.getDescription().name, "bubble", 0.65f);
          l.pack();
          pl.setDragActor(l);

          G24Label il = new G24Label(property.getDescription().name, "bubble", 0.65f);
          il.setColor(1, 0, 0, 1);
          il.pack();
          pl.setInvalidDragActor(il);

          G24Label vl = new G24Label(property.getDescription().name, "bubble", 0.65f);
          vl.setColor(0, 1, 0, 1);
          vl.pack();
          pl.setValidDragActor(vl);

          PropertyPill.this.setVisible(true);

          return pl;
        }
        public void dragStop (InputEvent event, float x, float y, int pointer, @Null Payload payload, @Null Target target) {
          if (target != null){
            ItemEditor.get().getDragAndDrop().removeSource(this);
            ItemEditor.get().getNewItem().removeProperty(property.getType());
            PropertyPill.this.remove();
            // TODO: fix this to work with table
            // table.remove(nodes.get(property.getType()));
            // nodes.remove(property.getType());
            ItemEditor.get().markModified();
          } else {
            PropertyPill.this.setVisible(true);
          }
        }
      });
    }
  }

  private void addPill(ItemProperty<?> property){
      PropertyPill<?,?> pill = new PropertyPill<>(property);
      // YogaNode node = table.add(pill).setPadding(YogaEdge.ALL, 10);

      // nodes.put(property.getType(), node);
      pills.add(pill);
      table.pack();
      ItemEditor.get().repack();
  }  

  public Set<PropertyPill<?,?>> getPills(){
    return pills;
  }

  public void populateFor(Item item){
    // TODO:
    // for (PropertyType type : nodes.keySet()){
      // table.remove(nodes.get(type));
    // }
    table.clear();
    table.pack();


    ItemEditor.get().repack();

    for (ItemProperty<?> prop : item.getProperties()){
      addPill(prop);
    }
  }

  public PropertyWorkspace() {
    super(null, AbstractScreen.skin);
    table = new Table();
    setActor(table);

    ItemEditor.get().getDragAndDrop().addTarget(new DragAndDrop.Target(this) {

      @Override
      public boolean drag(Source source, Payload payload, float x, float y, int pointer) {
        PropertyType type = (PropertyType) payload.getObject();
        if (ItemEditor.get().getNewItem().hasProperty(type) || 
            ItemEditor.get().getNewItem().getProperties().stream().anyMatch(
              (prop) -> { 
                return prop.getDescription().mutallyExclusiveWith.contains(type); 
              }
            )
        ) {
          return false;
        }
        return true;
      }

      @Override
      public void drop(Source source, Payload payload, float x, float y, int pointer) {
        PropertyType type = (PropertyType) payload.getObject();
        ItemProperty<? extends ItemPropertyValue> prop = type.getEmptyProperty();
        ItemEditor.get().getNewItem().addProperty(prop);
        prop.apply(ItemEditor.get().getNewItem());
        addPill(prop);
        ItemEditor.get().markModified();
      }
    });

  }
}
