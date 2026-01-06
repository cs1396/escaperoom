package cs1396.escaperoom.editor.item.ui;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Stream;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import cs1396.escaperoom.engine.assets.AssetManager;
import cs1396.escaperoom.engine.assets.maps.MapLoader;
import cs1396.escaperoom.game.entities.Item;
import cs1396.escaperoom.screens.AbstractScreen;
import cs1396.escaperoom.screens.ItemEditor;
import cs1396.escaperoom.ui.notifications.Notifier;
import cs1396.escaperoom.ui.widgets.G24NumberInput;
import cs1396.escaperoom.ui.widgets.G24TextButton;
import cs1396.escaperoom.ui.widgets.G24TextInput;
import cs1396.escaperoom.ui.widgets.G24ImageButton;
import cs1396.escaperoom.ui.widgets.G24Label;
import cs1396.escaperoom.editor.ui.Menu;
import cs1396.escaperoom.editor.ui.Menu.MenuEntry;
import cs1396.escaperoom.editor.ui.Menu.MenuEntryBuilder;

/**
 * Dependent on {@link ItemEditor}
 */
public class ItemSideBar {
  public PropertyWorkspace workspace;
  private Function<Item, Void> init;
  private Logger log = Logger.getLogger(ItemSideBar.class.getName());

  public void populateFor(Item item){
    init.apply(item);
    workspace.populateFor(item);
  }


  @FunctionalInterface
  private interface ModifiesItemString {
    void modifyString(Item item, String val);
  }

  private void refreshItem(){
    ItemEditor.get().updateRoom();
    ItemEditor.get().markModified();
  }

  private G24TextInput nonEmptyStringField(String name, String value, String defaultValue, ModifiesItemString mod){
    G24TextInput inp = new G24TextInput(value);
    inp.setAlphanumericWithWhitespace();
    inp.setOnEnter(() -> {
      if (inp.getText().isEmpty() || inp.getText().isBlank()){
        Notifier.error( name + " cannot be empty");
        inp.setText(defaultValue);
      }
    });
    inp.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        if (!(inp.getText().isEmpty() || inp.getText().isBlank())){
          mod.modifyString(ItemEditor.get().getItem(), inp.getText());
          refreshItem();
        }
      }
    });
    return inp;
  }

  @FunctionalInterface
  private interface ModifiesItemNumber {
    void modifyNumber(Item item, int val);
  }
  private G24NumberInput numericField(String name, int val, int defaultValue, ModifiesItemNumber mod){
    G24NumberInput inp = new G24NumberInput(Integer.toString(val));
    inp.setOnEnter(() -> {
      if (inp.getText().isEmpty() || inp.getText().isBlank()){
        Notifier.error(name + " cannot be empty");
        inp.setText(Integer.toString(defaultValue));
      }
    });
    inp.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        if (!(inp.getText().isEmpty() || inp.getText().isBlank())){
          mod.modifyNumber(ItemEditor.get().getItem(), Integer.parseInt(inp.getText().trim()));
          refreshItem();
        }
      }
    });
    return inp;
  }

  public ItemSideBar(Menu parent){

    G24TextInput nameInp = nonEmptyStringField(
        "Name", 
        ItemEditor.get().getItem().getItemName(), 
        "None", 
        (i, s) -> i.getType().name = s
    );
    nameInp.setStyle(AbstractScreen.skin.get("transparent", TextFieldStyle.class));

    G24TextInput categoryInp = nonEmptyStringField(
        "Category", 
        ItemEditor.get().getItem().getType().category, 
        "Custom", 
        (i, s) -> i.getType().category = s
    );

    G24NumberInput sizeInpWidth = numericField(
        "Width",
        ItemEditor.get().getItem().getWidth(),
        1,
        (i, v) -> i.setWidth(v)
    );
    sizeInpWidth.setMaxLength(2);

    G24NumberInput sizeInpHeight = numericField(
        "Height",
        ItemEditor.get().getItem().getHeight(),
        1,
        (i, v) -> i.setHeight(v)
    );
    sizeInpHeight.setMaxLength(2);

    G24NumberInput renderInp = numericField(
        "Render Priority",
        ItemEditor.get().getItem().renderPriority(),
        0,
        (i, v) -> i.setRenderPriority(v)
    );


    init = (Item i) -> {
      nameInp.setText(i.getType().name);
      categoryInp.setText(i.getType().category);
      sizeInpWidth.setText(Integer.toString(i.getType().size.width));
      sizeInpHeight.setText(Integer.toString(i.getType().size.height));
      renderInp.setText(Integer.toString(i.getType().renderPriority));
      refreshItem();
      return null;
    };

    G24ImageButton editName = new G24ImageButton("edit");
    editName.setProgrammaticChangeEvents(false);
    editName.addListener(new ChangeListener(){
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        if (editName.isChecked()){
          ItemEditor.get().getUIStage().setKeyboardFocus(nameInp);
          nameInp.setCursorPosition(nameInp.getText().length());
        } 
        editName.setChecked(false);
      }
    });
    Table nameTable = new Table();
    nameTable.add(nameInp).growX();
    nameTable.add(editName);
    parent.add(nameTable).growX().row();

    Table sizeInps = new Table();
    sizeInps.pad(0);
    sizeInps.padTop(10);
    sizeInps.defaults().pad(0);
    sizeInps.add(sizeInpWidth).maxWidth(30).padRight(-40);
    sizeInps.add(new G24Label("X"));
    sizeInps.add(sizeInpHeight).maxWidth(30).padLeft(-40);
    parent.add(sizeInps).center().row();

    parent.divider();
    parent.add(
      new MenuEntryBuilder(parent, "Texture").spawns((p) -> {
        Menu m = new Menu(p, "Textures", ItemEditor.get());
        ItemEditor.get().setOpenMenu(m);

        m.add(new MenuEntryBuilder(m, "Default Textures").spawns((tp) -> {

          Menu tm = new Menu(tp, "Default Textures", ItemEditor.get());

          VerticalGroup textures = new VerticalGroup();
          textures.columnLeft();
          textures.fill().expand();
          ScrollPane pane = new ScrollPane(textures);
          tm.add(pane).maxHeight(200);

          Function<String, MenuEntry> defaultTextureEntry = (textureName) -> {

            return new MenuEntryBuilder(tm, textureName)
            .onClick(()-> {
                Item newItem = ItemEditor.get().getItem();
                newItem.getType().texture = textureName + ".png";
                newItem.setTexture(AssetManager.instance().getRegion(textureName));
                ItemEditor.get().markModified();
            })
            .build();
          };

          try (Stream<Path> paths = Files.walk(new File("textures/entity_textures").toPath(), FileVisitOption.FOLLOW_LINKS)) {
            paths
            .sorted((p1, p2) -> p1.getFileName().toString().compareToIgnoreCase(p2.getFileName().toString()))
            .forEach((path) -> {
              AssetManager.textureName(path).inspect(textureName -> {
                textures.addActor(defaultTextureEntry.apply(textureName));
              });
            });
          } catch (IOException ioe){
            Notifier.error("Error loading textures");
          }
          return tm;
        }).build()).row();

        m.add(new MenuEntryBuilder(m, "My Textures").spawns((tp) -> {
          Menu tm = new Menu(tp, "My Textures", ItemEditor.get());

          ItemEditor.get().getMapData().getMetadata().textureDirectory.ifPresent((textureDir) -> {

            // Function to apply to each valid texture file
            Function<Path, MenuEntry> userDefinedTextureEntry = (texturePath) -> {
              String filename = texturePath.getFileName().toString();

              return new MenuEntryBuilder(tm, filename)
              .onClick(()-> {
                AssetManager.textureName(texturePath).inspect(textureName -> {
                  texturePath.toFile().setLastModified(System.currentTimeMillis());

                  if (!MapLoader.reloadTextures(ItemEditor.get().getMapData().getMetadata())){
                    Notifier.error("Failed to load texture: " + filename);
                  } else {
                    Item item = ItemEditor.get().getItem();
                    item.getType().texture = textureName;
                    item.setTexture(AssetManager.instance().getRegion(textureName));
                    ItemEditor.get().markModified();
                  }
                });
              })
              .build();
            };

            try (Stream<Path> paths = Files.walk(new File(textureDir).toPath(), FileVisitOption.FOLLOW_LINKS)) {
              paths
              // Get pngs
              .filter((path) -> AssetManager.textureName(path).isOk())
              // Sorted by most recently modified
              .sorted((p1, p2) -> Long.compare(p2.toFile().lastModified(), p1.toFile().lastModified()))
              // limit to 10
              .limit(10)
              // add each to our menu
              .forEach((path) -> {
                tm.add(userDefinedTextureEntry.apply(path)).row();
              });
            } catch (IOException ioe){
              Notifier.error("Error loading textures");
            }
          });

          return tm;
        }).build()).row();

        m.add(new MenuEntryBuilder(m, "Open File").onClick(() -> {
          TexturePicker.pickTexture(ItemEditor.get().getMapData().getMetadata()).ifPresent((texturePath) -> {
            if (!MapLoader.reloadTextures(ItemEditor.get().getMapData().getMetadata())){
              log.severe("Failed to reload textures");
            } else {
              AssetManager.textureName(texturePath).inspect(textureName ->{
                Item newItem = ItemEditor.get().getItem();
                newItem.getType().texture = textureName;
                newItem.setTexture(AssetManager.instance().getRegion(textureName));
                ItemEditor.get().markModified();
              });
            }
          });
        }).build()).row();

        m.pack();
        return m;
      })
      .build()
    ).row();
    parent.divider();

    parent.add(
      new MenuEntryBuilder(parent, "Advanced")
      .spawns((p) -> {
        Menu m = new Menu(p, "Advanced", ItemEditor.get());
        ItemEditor.get().setOpenMenu(m);

        m.add(MenuEntry.label("Category").withToolTip("Category in the Item Drawer")).row();
        m.add(categoryInp).row();
        m.divider();
        m.add(MenuEntry.label("Render Priority").withToolTip("\"Height\" of the item in the world.\nHigher priority is rendered later.")).row();
        m.add(renderInp).row();

        return m;
      })
      .build()
    ).row();

    parent.divider();
    parent.add(MenuEntry.sectionLabel("Properties")).row();
    parent.divider();

    workspace = new PropertyWorkspace(parent);
    parent.add(workspace).padTop(10).growY().row();
    parent.divider();
    parent.add(new SaveButton()).row();
    parent.add(new QuitButton()).row();
    parent.add(new DiscardButton()).row();
  }

  public class QuitButton extends G24TextButton {
    public QuitButton(){
      super("Return to Editor");
      setProgrammaticChangeEvents(false);
      addListener(new ChangeListener(){
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          if (isChecked()){
            setChecked(false);
            ItemEditor.get().returnToEditor();
          }
        }
      });
    }
  }

  public class DiscardButton extends G24TextButton {
    public DiscardButton(){
      super("Discard Changes");
      setProgrammaticChangeEvents(false);
      addListener(new ChangeListener(){
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          if (isChecked()){
            setChecked(false);
            ItemEditor.get().resetItem();
          }
        }
      });
    }
  }

  public class SaveButton extends G24TextButton {
    public SaveButton(){
      super(ItemEditor.get().modifyingItem() ? "Save Changes" : "Save as New Item");
      setProgrammaticChangeEvents(false);
      addListener(new ChangeListener(){
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          if (isChecked()){
            setChecked(false);
            ItemEditor.get().saveItem();
          }
        }
      });
    }
  }
}
