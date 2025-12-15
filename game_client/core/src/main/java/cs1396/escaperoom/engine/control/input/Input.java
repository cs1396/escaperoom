package cs1396.escaperoom.engine.control.input;

import java.util.ArrayList;

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;

import cs1396.escaperoom.engine.control.ControlsManager.InputPair;
import cs1396.escaperoom.engine.control.bindings.InputBinding;
import cs1396.escaperoom.engine.control.bindings.InputModifier;
import cs1396.escaperoom.engine.control.bindings.KeyBinding;
import cs1396.escaperoom.engine.control.bindings.MapGroup;
import cs1396.escaperoom.engine.control.bindings.MouseBinding;

public enum Input {
  // Global / Generic Binds
  MOVE_UP(MapGroup.GENERAL, "Move Up", new KeyBinding(Keys.W), new KeyBinding(Keys.UP)),
  MOVE_LEFT(MapGroup.GENERAL, "Move Left", new KeyBinding(Keys.A), new KeyBinding(Keys.LEFT)),
  MOVE_DOWN(MapGroup.GENERAL, "Move Down",new KeyBinding(Keys.S), new KeyBinding(Keys.DOWN)),
  MOVE_RIGHT(MapGroup.GENERAL, "Move Right",new KeyBinding(Keys.D), new KeyBinding(Keys.RIGHT)),
  BACK(MapGroup.GENERAL, "Back",new KeyBinding(Keys.ESCAPE)),
  ZOOM_IN(MapGroup.GENERAL, "Zoom In",new KeyBinding(Keys.EQUALS, InputModifier.SHIFT), new KeyBinding(Keys.EQUALS)),
  ZOOM_OUT(MapGroup.GENERAL, "Zoom Out",new KeyBinding(Keys.MINUS)),

  // Level Editor Binds
  COPY(MapGroup.EDITOR, "Copy", new KeyBinding(Keys.C, InputModifier.CONTROL)),
  PASTE(MapGroup.EDITOR, "Paste", new KeyBinding(Keys.V, InputModifier.CONTROL)),
  UNDO(MapGroup.EDITOR, "Undo", new KeyBinding(Keys.Z, InputModifier.CONTROL)),
  REDO(MapGroup.EDITOR, "Redo", new KeyBinding(Keys.R, InputModifier.CONTROL)),
  ROTCW(MapGroup.EDITOR, "Rotate Item (Hovered)", new KeyBinding(Keys.R)),
  ROTCCW(MapGroup.EDITOR, "Rotate Item (Hovered)", new KeyBinding(Keys.R, InputModifier.SHIFT)),
  MIRROR_H(MapGroup.EDITOR, "Mirror Item Horizontally (Hovered)", new KeyBinding(Keys.H, InputModifier.SHIFT)),
  MIRROR_V(MapGroup.EDITOR, "Mirror Item Vertically (Hovered)", new KeyBinding(Keys.V, InputModifier.SHIFT)),
  KEYBIND_HELP(MapGroup.EDITOR, "Key Bind Menu", new KeyBinding(Keys.H)),
  DELETE_SELECTION(MapGroup.EDITOR, "Delete Active Selection", new KeyBinding(Keys.DEL), new KeyBinding(Keys.FORWARD_DEL)),
  TOOL_ROT(MapGroup.EDITOR, "Rotation Tool", new KeyBinding(Keys.R, InputModifier.CONTROL, InputModifier.SHIFT)),
  TOOL_PAN(MapGroup.EDITOR, "Pan Tool", new KeyBinding(Keys.SPACE)),
  TOOL_MOV(MapGroup.EDITOR, "Move Tool", new KeyBinding(Keys.V)),
  TOOL_PPT(MapGroup.EDITOR, "Property Tool", new KeyBinding(Keys.P)),
  TOOL_FILL(MapGroup.EDITOR, "Fill Tool", new KeyBinding(Keys.G)),
  TOOL_SEL(MapGroup.EDITOR, "Select Tool", new KeyBinding(Keys.M)),
  TOOL_EYEDROP(MapGroup.EDITOR, "Eye Drop Tool", new KeyBinding(Keys.I)),
  TOOL_DEL(MapGroup.EDITOR, "Delete Tool", new KeyBinding(Keys.E)),
  SELECT(MapGroup.EDITOR, "Select Item/Pan Camera", new MouseBinding(Buttons.LEFT)),
  SELECT_MULTI(MapGroup.EDITOR, "Add to Selection", new MouseBinding(Buttons.LEFT, InputModifier.SHIFT)),
  CONTEXT(MapGroup.EDITOR, "Open Context Menu", new MouseBinding(Buttons.RIGHT)),

  // Gameplay Binds
  INTERACT(MapGroup.GAME,"Interact", new KeyBinding(Keys.F)),
  CHANGE_INTERACT_FOCUS(MapGroup.GAME, "Cycle Interact Focus",new KeyBinding(Keys.Q)),
  INVENTORY(MapGroup.GAME,"Open Inventory",new KeyBinding(Keys.I)),

  // Debug Binds
  PRINT_INVENTORY(MapGroup.DEBUG,"Print Inventory",new KeyBinding(Keys.K)),

  ;

  private InputBinding[] binds;
  private ArrayList<InputPair> pairs;
  private String description;
  private MapGroup group;

  private Input(MapGroup grp,String desc, InputBinding ...keys) {
    binds = keys;
    description = desc;
    group = grp;

    pairs = new ArrayList<>(keys.length);
    for (int i = 0; i < keys.length; i++) {
      pairs.add(binds[i].getPair());
    }
  }

  public InputBinding[] getBinds() {
    return binds;
  }

  public ArrayList<InputPair> getInputs() {
    return pairs;
  }

  public MapGroup getGroup(){
    return group;
  }

  public final static class MappingDescription {
    public final String desc;
    public final String[] bindings;
    public MappingDescription(String description, String ... bindStrings){
      desc = description;
      bindings = bindStrings;
    }
  }

  public MappingDescription description() {
    String[] bindingStrs = new String[binds.length];

    for (int i = 0; i < binds.length; i++){
      bindingStrs[i] = binds[i].toString();
    }
    return new MappingDescription(description, bindingStrs);
  }
}
