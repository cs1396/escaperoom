package cs1396.escaperoom.editor.item.ui;

import java.util.Set;
import java.util.stream.Collectors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;

import cs1396.escaperoom.editor.ui.Menu;
import cs1396.escaperoom.editor.ui.Menu.MenuEntryBuilder;
import cs1396.escaperoom.game.entities.Item;
import cs1396.escaperoom.game.entities.properties.PropertyType;
import cs1396.escaperoom.game.entities.properties.base.ItemProperty;
import cs1396.escaperoom.screens.AbstractScreen;
import cs1396.escaperoom.screens.ItemEditor;
import cs1396.escaperoom.ui.FontBuilder;
import cs1396.escaperoom.ui.FontManager.FontStyle;
import cs1396.escaperoom.ui.widgets.G24Label;

public class PropertyWorkspace extends ScrollPane {
  private Table innerTable = new Table();
  private Menu parent;

  private Table addPropertyEntry(ItemProperty<?> property){
    Table ret = new Table();
      ret.add(new MenuEntryBuilder(parent, property.getDescription().name)
      .spawns((p) -> {
        PropertyConfigurationMenu pcm = new PropertyConfigurationMenu(p, property);
        pcm.divider();
        ItemEditor.get().setOpenMenu(pcm);

        G24Label label = new G24Label("Delete");
        LabelStyle style = new LabelStyle(AbstractScreen.skin.get(LabelStyle.class));
        style.fontColor = Color.valueOf("C34043");
        style.font = new FontBuilder().style(FontStyle.Bold).size(20).color(Color.valueOf("C34043")).build();
        label.setStyle(style);

        pcm.add(
          new MenuEntryBuilder(pcm, label)
            .onClick(() -> {
              ItemEditor.get().getItem().removeProperty(property.getType());
              ItemEditor.get().markModified();
              populateFor(ItemEditor.get().getItem());
              pcm.close();
            })
            .build()
        );

        return pcm;
      })
      .build()).growX();

    return ret;
  }  

  public void populateFor(Item item){
    innerTable.clear();

    for (ItemProperty<?> prop : item.getProperties()){
      Table row  = addPropertyEntry(prop);
      innerTable.add(row).growX().row();
    }

    innerTable.add(
      new MenuEntryBuilder(parent, "+ Add Property")
      .spawns((p) -> {
        Menu m = new Menu(p, "Properties", ItemEditor.get());
        ItemEditor.get().setOpenMenu(m);

        Set<PropertyType> current = ItemEditor.get()
          .getItem()
          .getProperties()
          .stream()
          .map((prop) -> prop.getType())
          .collect(Collectors.toSet());

        LabelStyle invalid = new LabelStyle(AbstractScreen.skin.get(LabelStyle.class));
        invalid.font = new FontBuilder().color(Color.valueOf("C34043")).build();
        invalid.fontColor = Color.valueOf("C34043");

        for (PropertyType propType : PropertyType.values()){
          if (propType == PropertyType.InvalidProperty) continue;

          ItemProperty<?> prop = propType.getEmptyProperty();
          if (!current.contains(propType)){

            boolean valid = true;
            for (PropertyType curI : current){
              if (prop.getDescription().mutallyExclusiveWith.contains(curI)){
                valid = false;

                G24Label label = new G24Label(prop.getDescription().name);
                label.setStyle(invalid);

                m.add(
                  new MenuEntryBuilder(m, label)
                  .build()
                  .withToolTip("Conflicts with " + curI.getEmptyProperty().getDescription().name)
                ).row();

                break;
              }
            }

            if (!valid) continue;

            m.add(
              new MenuEntryBuilder(m, prop.getDescription().name)
              .onClick(() -> {
                prop.apply(ItemEditor.get().getItem());
                ItemEditor.get().getItem().addProperty(prop);
                ItemEditor.get().markModified();
                m.close();
                populateFor(ItemEditor.get().getItem());
              })
              .build()
            ).row();


          } else {
            G24Label label = new G24Label(prop.getDescription().name);
            label.setStyle(invalid);

            m.add(
              new MenuEntryBuilder(m, label)
              .build().withToolTip("Already Added")
            ).row();
          }
        }
        return m;
      })
      .build()
    ).growX();

    ItemEditor.get().repack();
  }

  public PropertyWorkspace(Menu parent) {
    super(null, AbstractScreen.skin);
    this.parent = parent;
    parent.add(innerTable);
    setStyle(AbstractScreen.skin.get("nobkg", ScrollPaneStyle.class));
  }
}
