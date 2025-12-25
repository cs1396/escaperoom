package cs1396.escaperoom.editor.tools;

import java.util.Optional;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import cs1396.escaperoom.screens.AbstractScreen;
import cs1396.escaperoom.ui.Tooltip;
import cs1396.escaperoom.ui.widgets.G24Label;

/**
 * A {@link ToolButton}
 */
public class ToolButton extends ImageButton {
  public ToolButton(EditorTool tool) {
    super(AbstractScreen.skin, tool.getButtonStyle());

    String label = tool.getName();
    if (tool.getType().getHotKey() != null){
      label += "\nHotKey(s): " + String.join("\n",tool.getType().getHotKey().description().bindings);
    }
    G24Label l = new G24Label( label, "bubble_gray");
    l.pack();
    new Tooltip.Builder(l).target(this, () -> Optional.of(this.getStage())).build();

    addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        if (isChecked()) {
          tool.getEditor().setActiveTool(tool);
        } else {
          tool.cancel();
          tool.getEditor().deselectTools();
        }
      }
    });
  }
}
