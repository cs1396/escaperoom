package cs1396.escaperoom.editor.item.ui;

import java.util.function.Function;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import cs1396.escaperoom.engine.assets.AssetManager;
import cs1396.escaperoom.engine.assets.maps.MapLoader;
import cs1396.escaperoom.game.entities.Item;
import cs1396.escaperoom.screens.ItemEditor;
import cs1396.escaperoom.ui.Tooltip;
import cs1396.escaperoom.ui.notifications.Notifier;
import cs1396.escaperoom.ui.widgets.G24NumberInput;
import cs1396.escaperoom.ui.widgets.G24TextButton;
import cs1396.escaperoom.ui.widgets.G24TextInput;
import cs1396.escaperoom.ui.widgets.G24Label;

/**
 * Dependent on {@link ItemEditor}
 */
public class ItemSideBar extends Table {
  public PropertyWorkspace workspace;
  private Function<Item, Void> init;


  private class FieldHeader extends G24Label {
    FieldHeader(String title, String description){
      super(title, "underline", 0.65f);
      if (description != null && !description.isEmpty())
      new Tooltip.Builder(new G24Label(description, "bubble_gray", 0.65f)).target(this, Tooltip.stageHelper(this)).build();
    }
  }

  public void populateFor(Item item){
    init.apply(item);
    workspace.populateFor(item);
  }


  @FunctionalInterface
  private interface ModifiesItemString {
    void modifyString(Item item, String val);
  }

  private void refreshItem(){
    ItemEditor.get().updateItemPosition();
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
          mod.modifyString(ItemEditor.get().getNewItem(), inp.getText());
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
          mod.modifyNumber(ItemEditor.get().getNewItem(), Integer.parseInt(inp.getText().trim()));
          refreshItem();
        }
      }
    });
    return inp;
  }

  public ItemSideBar(){
    pad(0);
    defaults().left().growX();

    G24TextInput nameInp = nonEmptyStringField(
        "Name", 
        ItemEditor.get().getNewItem().getItemName(), 
        "None", 
        (i, s) -> i.getType().name = s
    );

    G24TextInput categoryInp = nonEmptyStringField(
        "Category", 
        ItemEditor.get().getNewItem().getType().category, 
        "Custom", 
        (i, s) -> i.getType().category = s
    );

    G24NumberInput sizeInpWidth = numericField(
        "Width",
        ItemEditor.get().getNewItem().getWidth(),
        1,
        (i, v) -> i.setWidth(v)
    );

    G24NumberInput sizeInpHeight = numericField(
        "Height",
        ItemEditor.get().getNewItem().getHeight(),
        1,
        (i, v) -> i.setHeight(v)
    );
    G24NumberInput renderInp = numericField(
        "Render Priority",
        ItemEditor.get().getNewItem().renderPriority(),
        0,
        (i, v) -> i.setRenderPriority(v)
    );
    G24TextInput textureInp = nonEmptyStringField(
        "Texture", 
        ItemEditor.get().getNewItem().getType().texture, 
        "placeholder", 
        (i, s) -> i.setTexture(AssetManager.instance().getRegion(s))
    );

    G24TextButton texturePicker = new G24TextButton("Pick Texture File");
    texturePicker.addListener(new ChangeListener(){

		@Override
		public void changed(ChangeEvent event, Actor actor) {
        if (texturePicker.isChecked()){
          texturePicker.setChecked(false);
          TexturePicker.pickTexture(ItemEditor.get().getMapData().getMetadata()).ifPresent((filename) -> {
            if (!MapLoader.reloadTextures(ItemEditor.get().getMapData().getMetadata())){
              System.out.println("Failed to reload textures");
            } else {
              Item newItem = ItemEditor.get().getNewItem();

              String sansPng = filename.substring(0, filename.lastIndexOf(".png"));

              newItem.getType().texture = sansPng;
              newItem.setTexture(AssetManager.instance().getRegion(sansPng));
              textureInp.setText(sansPng);
              ItemEditor.get().markModified();
            }
          });
        }
		}
    });

    init = (Item i) -> {
      nameInp.setText(i.getType().name);
      categoryInp.setText(i.getType().category);
      sizeInpWidth.setText(Integer.toString(i.getType().size.width));
      sizeInpHeight.setText(Integer.toString(i.getType().size.height));
      renderInp.setText(Integer.toString(i.getType().renderPriority));
      textureInp.setText(i.getType().texture);
      refreshItem();
      return null;
    };

    add(new FieldHeader("Name", null)).row();
    add(nameInp).row();

    add(new FieldHeader("Category", "Category in the Item Drawer")).row();
    add(categoryInp).row();

    add(new FieldHeader("Size", null)).row();
    add(new G24Label("Width: ", "default", 0.65f)).row();
    add(sizeInpWidth).row();
    add(new G24Label("Height: ", "default", 0.65f)).row();
    add(sizeInpHeight).row();

    add(new FieldHeader("Render Priority","\"Height\" of this item (e.g. 0 for floors, 5 for player, etc)")).row();;
    add(renderInp).row();

    add(new FieldHeader("Texture", null)).row();;
    add(textureInp).row();
    add(texturePicker).row();

    add(new FieldHeader("Properties: ", null)).row();;
    workspace = new PropertyWorkspace();
    add(workspace).padTop(10).growY().row();
    add(new SaveButton()).row();;
    add(new QuitButton()).row();;
    add(new DiscardButton()).row();
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
      super(ItemEditor.get().modifyingItem() ? "Save as New Item" : "Save Item");
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
