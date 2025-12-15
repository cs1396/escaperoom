package cs1396.escaperoom.editor.ui;

import java.util.HashMap;

import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.utils.Array;

import cs1396.escaperoom.game.entities.Item;
import cs1396.escaperoom.game.entities.properties.base.BooleanProperty;
import cs1396.escaperoom.game.entities.properties.base.ItemProperty;
import cs1396.escaperoom.game.entities.properties.values.BooleanValue;
import cs1396.escaperoom.game.entities.properties.values.ItemPropertyValue;
import cs1396.escaperoom.screens.AbstractScreen;
import cs1396.escaperoom.screens.LevelEditor;

public class PropertyMenu extends Menu {
  public PropertyMenu(MenuEntry parent, Item item, LevelEditor editor) {
    super(parent, "Properties",  editor);

    for (ItemProperty<? extends ItemPropertyValue> property : item.getProperties()) {
      switch (property.getInputType()) {
        case Toggleable:
          addToggle((BooleanProperty) property);
          break;
        case PopOut:
          addPopOut(property);
          break;
        case Select:
          addSelect(property, true);
          break;
        case SelectOne:
          addSelect(property, false);
          break;
        default:
          break;
      }
    }
    pack();
  }


  private <V extends ItemPropertyValue, P extends ItemProperty<V>> void addSelect(P property, boolean multiselect) {

    MenuEntry e = new MenuEntryBuilder(this, property.getDisplayName())
        .spawns((entryParent) -> {
          MenuEntryGroup group = new MenuEntryGroup();
          HashMap<MenuEntry, V> values = new HashMap<>();

          V selectedValue = property.getCurrentValue();
          Menu popOutMenu = new Menu(null, property.getDisplayName(), screen);

          for (V value : property.getPotentialValues()) {
            MenuEntry entry = value.getDisplay(popOutMenu);
            values.put(entry, value);

            // ensure this entry is selectable
            entry.selectable = true;

            if (value.equals(selectedValue)) {
              entry.setSelected();
            }

            // wrap our callback to set the value
            SelectionHandler handler = entry.onSelect;
            entry.onSelect = () -> {
              if (multiselect) {
                Array<V> valuesToSet = new Array<>(property.getValueClass());
                group.getSelected().forEach((me) -> valuesToSet.add(values.get(me)));
                property.set(valuesToSet);
              } else {
                property.set(value);
              }
              handler.handle();
            };

            // add the entry to the group
            group.addEntry(entry);
          }

          popOutMenu.parent = entryParent;
          group.entries.forEach((ent) -> popOutMenu.add(ent).row());
          return popOutMenu;
        })
        .build();

    add(new PropertyMenuEntry(e, property)).row();
  }

  private <V extends ItemPropertyValue, P extends ItemProperty<V>> void addPopOut(P property) {
    add(new PropertyMenuEntry(
      new MenuEntryBuilder(
        this, property.getDisplayName()
      ).spawns(
        m -> property.getPopOut(m)
      ).build(),
      property
    )).row();
  }

  private <P extends BooleanProperty> void addToggle(P property) {

    CheckBox checkBox = new CheckBox(property.getDisplayName(), AbstractScreen.skin);

    MenuEntry entry = new MenuEntryBuilder(this, checkBox)
      .onSelect(() -> {
        property.set(new BooleanValue(true));
      })
      .onDeselect(() -> {
        property.set(new BooleanValue(false));
      })
      .build();

    if (property.getCurrentValue().isTrue()){
      entry.setSelected();
    }

    add(MenuEntry.divider()).row();
    add(new PropertyMenuEntry(entry, property)).row();
    add(MenuEntry.divider()).row();
  }

  public static class PropertyMenuEntry extends MenuEntry {
    ItemProperty<? extends ItemPropertyValue> property;

    protected PropertyMenuEntry(MenuEntry base, ItemProperty<? extends ItemPropertyValue> property) {
      super(
        base.parent,
        base.content,
        base.spawnsMenu,
        base.onSelect,
        base.onDeselect,
        base.onClick,
        base.selectable
      );

      this.property = property;
    }

    public ItemProperty<? extends ItemPropertyValue> getProperty() {
      return property;
    }
  }
}
