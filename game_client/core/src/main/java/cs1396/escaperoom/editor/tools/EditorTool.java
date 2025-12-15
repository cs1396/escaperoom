package cs1396.escaperoom.editor.tools;


import cs1396.escaperoom.editor.core.ToolManager.ToolType;
import cs1396.escaperoom.screens.LevelEditor;

public abstract class EditorTool {
  LevelEditor editor;

  public EditorTool(LevelEditor stage) {
    this.editor = stage;
  }

  /**
   * Called when this tool is selected
   */
  public abstract void select();

  /**
   * @return the {@link ToolType} 
   */
  public abstract ToolType getType();

  /**
   * Called when the user presses Escape while this is the active tool
   */
  public abstract void cancel();

  public abstract String getName();
  public  String getButtonStyle(){
    return "default";
  };

  public LevelEditor getEditor() {
    return editor;
  }

}
