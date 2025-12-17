package cs1396.escaperoom.editor.ui;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;

import cs1396.escaperoom.editor.core.ToolManager.ToolType;
import cs1396.escaperoom.editor.tools.DeletionTool.Deletion;
import cs1396.escaperoom.editor.tools.EyeDropTool;
import cs1396.escaperoom.editor.tools.RotationTool.RotationAction;
import cs1396.escaperoom.engine.assets.maps.MapSaver;
import cs1396.escaperoom.game.entities.Item;
import cs1396.escaperoom.game.entities.properties.base.ItemProperty.MenuType;
import cs1396.escaperoom.screens.ItemEditor;
import cs1396.escaperoom.screens.LevelEditor;
import cs1396.escaperoom.screens.utils.ScreenManager;
import cs1396.escaperoom.ui.notifications.Notifier;

public class ItemMenu extends Menu {
  Item item;
  public ItemMenu(Item target, LevelEditor editor) {
    super(null, target.getItemName(), editor);
    item = target;

    Vector2 uiPos = editor.gameCoordToUI(item.getPosition().add(item.getWidth(), 1).asVector2());
    setPosition(uiPos.x, uiPos.y, Align.bottomLeft);

    add(MenuEntry.label("ID: " + item.getID())).row();
    add(MenuEntry.divider()).row();

    if (target.getProperties().stream().anyMatch(p -> p.getInputType() != MenuType.None)) {
      add(new MenuEntryBuilder(this,"Properties")
          .spawns((parent) -> {
            return new PropertyMenu(parent, item, editor);
          })
          .build())
        .row();

      add(MenuEntry.divider()).row();;
    }

    add(new MenuEntryBuilder(this,"Edit Item Instance")
      .onClick(() -> {
        target.setSelected(false);
        if (MapSaver.saveMap(editor.getMapData())){
          ScreenManager.instance().showScreen(
            new ItemEditor(editor.getMapData(), target),
            true
          );
        } else {
          Notifier.error("Failed to save map");
        }
      })
      .build())
    .row();

    add(new MenuEntryBuilder(this, "Delete")
      .onClick(() -> {
        ItemMenu.this.close();
        item.remove(false);
        editor.recordEditorAction(new Deletion(editor, item));
      })
      .build())
    .row();

    add(new MenuEntryBuilder(this, "Copy")
      .onClick(() -> {
        EyeDropTool tool = ((EyeDropTool)editor.getTool(ToolType.EyeDrop));
        editor.setActiveTool(tool);
        tool.copyItem(target);
        ItemMenu.this.close();
      })
      .build())
    .row();

    add(new MenuEntryBuilder(this, "Rotate")
      .onClick(() -> {
        item.rotateBy(90);
        editor.recordEditorAction(new RotationAction(item));
      })
      .build())
    .row();

    add(new MenuEntryBuilder(this, "Flip Horizontal")
      .onClick(() -> {
        item.mirrorHorizontal();
      })
      .build())
    .row();

    add(new MenuEntryBuilder(this, "Flip Vertical")
      .onClick(() -> {
        item.mirrorVertical();
      })
      .build())
    .row();
    pack();

    add(new MenuEntryBuilder(this, "Send forward")
      .onClick(() -> {
        item.increaseRenderPriotity();
      })
      .build())
    .row();
    pack();

    add(new MenuEntryBuilder(this, "Send backward")
      .onClick(() -> {
        item.decreaseRenderPriotity();
      })
      .build())
    .row();
    pack();
  }

}
