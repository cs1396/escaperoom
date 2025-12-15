package cs1396.escaperoom.editor.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Null;

import cs1396.escaperoom.editor.tools.Brush;
import cs1396.escaperoom.editor.tools.DeletionTool;
import cs1396.escaperoom.editor.tools.EditorTool;
import cs1396.escaperoom.editor.tools.EyeDropTool;
import cs1396.escaperoom.editor.tools.FillTool;
import cs1396.escaperoom.editor.tools.ItemDrawer;
import cs1396.escaperoom.editor.tools.ItemDrawer.ItemCategory;
import cs1396.escaperoom.editor.tools.ItemSelectionTool;
import cs1396.escaperoom.editor.tools.MoveTool;
import cs1396.escaperoom.editor.tools.PanTool;
import cs1396.escaperoom.editor.tools.PropertyTool;
import cs1396.escaperoom.editor.tools.RotationTool;
import cs1396.escaperoom.editor.tools.ToolButton;
import cs1396.escaperoom.engine.BackManager;
import cs1396.escaperoom.engine.control.input.Input;
import cs1396.escaperoom.screens.LevelEditor;

public class ToolManager {
  private final HashSet<Brush> brushes = new HashSet<>();
  private final ButtonGroup<ToolButton> buttons = new ButtonGroup<>();
  private final ItemDrawer drawer;
  private Optional<EditorTool> activeTool = Optional.empty();
  private HashMap<ToolType, ToolPair> map = new HashMap<>();
  private ToolButton fillButton;
  private FillTool fillTool;

  private class ToolPair {
    final EditorTool tool;
    final ToolButton button;
    public ToolPair(EditorTool t, ToolButton b){
      tool = t;
      button = b;
    }
  }

  public enum ToolType {
    Brush,
    Deletion,
    Fill,
    ItemSelect,
    EyeDrop,
    Properties,
    Move,
    Pan,
    Rotation,
    Selection;

    public @Null Input getHotKey(){
      switch (this){
        case Deletion:
            return Input.TOOL_DEL;
        case Fill:
            return Input.TOOL_FILL;
        case ItemSelect:
            return Input.TOOL_SEL;
        case EyeDrop:
            return Input.TOOL_EYEDROP;
        case Properties:
            return Input.TOOL_PPT;
        case Move:
            return Input.TOOL_MOV;
        case Pan:
            return Input.TOOL_PAN;
        case Rotation:
            return Input.TOOL_ROT;
        case Selection:
        case Brush:
            return null;
      }
      return null;
    }
  }

  public ToolManager(LevelEditor screen, ItemDrawer drawer) {
    buttons.setMaxCheckCount(1);
    buttons.setMinCheckCount(0);
    buttons.uncheckAll();
    this.drawer = drawer;

    addTools(screen);

    drawer.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        // find the selection
        ItemCategory selection = drawer.getSelection().first();
        if (selection != null) {
          selection.getBrush().ifPresentOrElse((b) -> {
            if (!brushes.contains(b)) {
              brushes.add(b);
            }
            brushes.forEach((br) -> br.cancel());
            buttons.uncheckAll();
            setTool(b);
          }, () -> {
            brushes.forEach((b) -> b.cancel());
          });
        }
      }

    });
  }

  public Array<ToolButton> getToolButons() {
    Array<ToolButton> btns = buttons.getButtons();
    btns.add(fillButton);
    return btns;
  }

  public EditorTool getTool(ToolType type) {
    return map.get(type).tool;
  }

  public void addBrush(Brush brush) {
    brushes.add(brush);
  }

  private void addTools(LevelEditor screen) {
    addTool(new ItemSelectionTool(screen.getUIStage(), screen));
    addTool(new DeletionTool(screen));
    addTool(new RotationTool(screen));
    addTool(new MoveTool(screen.getUIStage(), screen));
    addTool(new PanTool(screen));
    addTool(new EyeDropTool(screen));
    addTool(new PropertyTool(screen));
    fillTool = new FillTool(screen);
    fillButton = new ToolButton(fillTool);
    fillButton.addListener(new ChangeListener(){

		@Override
		public void changed(ChangeEvent event, Actor actor) {
        if (!fillButton.isChecked()){
          setTool(fillTool.getBrush());
        } 
      }
    });
    fillButton.setDisabled(true);
    this.map.put(ToolType.Fill, new ToolPair(fillTool, fillButton));
  }

  public void addTool(EditorTool tool) {
    ToolButton b = new ToolButton(tool);
    this.map.put(tool.getType(), new ToolPair(tool, b));
    buttons.add(b);
  }

  public boolean canFill(){
    return !fillButton.isDisabled();
  }

  public void setTool(EditorTool tool) {
    if (tool.getType() == ToolType.Brush) {
      fillButton.setDisabled(false);
      fillTool.setBrush(Brush.class.cast(tool));
      buttons.uncheckAll();
    } else if (tool.getType() == ToolType.Fill){
      activeTool.ifPresent((t) -> t.cancel());
    } else {
      fillButton.setDisabled(true);
      fillTool.cancel();
      drawer.getSelection().clear();
      brushes.forEach((b) -> b.cancel());
    }

    BackManager.addBack(() -> {
      if (getActiveTool().isEmpty()) return false;
      deselectAll();
      return true;
    });

    tool.select();
    activeTool = Optional.of(tool);
  }

  public void setTool(ToolType type){
    boolean alreadyChecked = activeTool.map((t) -> t.getType() == type).orElse(false);

    BackManager.addBack(() -> {
      if (getActiveTool().isEmpty()) return false;
      deselectAll();
      return true;
    });

    map.get(type).button.setChecked(!alreadyChecked);
  }

  public ItemSelectionTool getSelectTool() {
    return ItemSelectionTool.class.cast(map.get(ToolType.ItemSelect).tool);
  }

  public void deselectAll() {
    activeTool = Optional.empty();
    fillButton.setDisabled(true);
    fillTool.cancel();
    drawer.getSelection().clear();
    brushes.forEach((b) -> b.cancel());
    buttons.uncheckAll();
  }

  public Optional<EditorTool> getActiveTool() {
    return activeTool;
  }
}
