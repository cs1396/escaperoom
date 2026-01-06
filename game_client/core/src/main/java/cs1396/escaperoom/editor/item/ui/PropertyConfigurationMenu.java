package cs1396.escaperoom.editor.item.ui;

import cs1396.escaperoom.editor.ui.ConfigurationMenu;
import cs1396.escaperoom.editor.ui.ConfigurationMenu.HandlesMenuClose;
import cs1396.escaperoom.editor.ui.Menu;
import cs1396.escaperoom.game.entities.properties.base.ItemProperty;
import cs1396.escaperoom.game.entities.properties.values.ItemPropertyValue;
import cs1396.escaperoom.screens.ItemEditor;
import cs1396.escaperoom.ui.widgets.G24Label;
import cs1396.escaperoom.ui.widgets.G24Label.G24LabelStyle;

public class PropertyConfigurationMenu extends Menu {
  private class SpecialLittleTinyBabyLabel extends G24Label implements HandlesMenuClose {
    SpecialLittleTinyBabyLabel(String content){
      super(content, G24LabelStyle.Default);
      setWrap(true);
      setWidth(150);
    }

    @Override
    public void handle() {
    }
  }

  public PropertyConfigurationMenu(MenuEntry parent, ItemProperty<? extends ItemPropertyValue> property){
    super(parent, property.getDescription().name, ItemEditor.screen);

    add(new SpecialLittleTinyBabyLabel(property.getDescription().shortDesc)).row();
    divider();

    property.getCustomItemConfigurationMenu().ifPresent((config) -> {
      add(new MenuEntryBuilder(this, "Configure")
        .spawns((p) -> {
          ItemEditor.get().markModified();
          return new ConfigurationMenu<>(
            p, 
            config,
            property.getDescription().name + " Configuration", 
            screen);
        })
        .build())
      .row();
    });

    add(new MenuEntryBuilder(this, "Help")
      .spawns((p) -> {
        return new ConfigurationMenu<>(
          p, 
          new SpecialLittleTinyBabyLabel(property.getDescription().longDesc),
          property.getDescription().name + " Details", 
          screen);
      })
      .build())
    .row();

    if (!property.getDescription().mutallyExclusiveWith.isEmpty()){
      add(new MenuEntryBuilder(this, "Conflicting Properties ")
        .spawns((pa) -> {

          ConfigurationMenu.VGroup conflicts = new ConfigurationMenu.VGroup();
          property.getDescription().mutallyExclusiveWith.forEach((p) -> {
            if (p != property.getType()){
              conflicts.addActor(new G24Label(p.getEmptyProperty().getDescription().name, G24LabelStyle.Bubble));
            }
          });

          return new ConfigurationMenu<>(
            pa, 
            conflicts,
            property.getDescription().name + " Conflicts", 
            screen);
        })
        .build())
      .row();
    }
    pack();
  }
}
