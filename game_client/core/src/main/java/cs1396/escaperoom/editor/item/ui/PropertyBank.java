package cs1396.escaperoom.editor.item.ui;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Payload;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Source;

import cs1396.escaperoom.engine.control.CursorManager;
import cs1396.escaperoom.engine.control.CursorManager.CursorType;
import cs1396.escaperoom.game.entities.properties.PropertyType;
import cs1396.escaperoom.game.entities.properties.base.ItemProperty;
import cs1396.escaperoom.game.entities.properties.values.ItemPropertyValue;
import cs1396.escaperoom.screens.AbstractScreen;
import cs1396.escaperoom.screens.ItemEditor;
import cs1396.escaperoom.ui.widgets.G24Label;

public class PropertyBank extends ScrollPane {
  public static class PropertyPill<V extends ItemPropertyValue, P extends ItemProperty<V>> extends G24Label {
    public PropertyPill(P property) {
      super(property.getDescription().name, "bubble");
      addListener(CursorManager.hoverHelper(CursorType.Hand));
      ItemEditor.get().getDragAndDrop().addSource(new DragAndDrop.Source(this) {
        @Override
        public Payload dragStart(InputEvent event, float x, float y, int pointer) {
          Payload pl = new Payload();
          pl.setObject(property.getType());
          G24Label l = new G24Label(property.getDescription().name, "bubble");
          l.pack();
          pl.setDragActor(l);

          G24Label il = new G24Label(property.getDescription().name, "bubble");
          il.setColor(1, 0, 0, 1);
          il.pack();
          pl.setInvalidDragActor(il);

          G24Label vl = new G24Label(property.getDescription().name, "bubble");
          vl.setColor(0, 1, 0, 1);
          vl.pack();
          pl.setValidDragActor(vl);

          return pl;
        }
    @Override
    public void dragStop(InputEvent event, float x, float y, int pointer, Payload payload, DragAndDrop.Target target) {
          CursorManager.restoreDefault();
        }
      });
    }
  }

  Table table;
  public PropertyBank(){
    super(null, AbstractScreen.skin);

    // Init flexbox
    table = new Table();
    setActor(table);

    // Add pills
    addPropertyPills();

    // Accept all payloads
    ItemEditor.get().getDragAndDrop().addTarget(
      new DragAndDrop.Target(this) {
        public boolean drag(Source source, Payload payload, float x, float y, int pointer) { return true; }
        public void drop(Source source, Payload payload, float x, float y, int pointer) {}
    });

  }

  @Override
  public void pack() {
      super.pack();
      table.pack();
  }

  private void addPropertyPills(){
    for (PropertyType type : PropertyType.values()){
      if (type == PropertyType.InvalidProperty) continue;
      table.add(new PropertyPill<>(type.getEmptyProperty())).row();
    }
  }
}
