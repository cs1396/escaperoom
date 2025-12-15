package cs1396.escaperoom.editor.core;

import java.util.Optional;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;

import cs1396.escaperoom.engine.control.input.Input;
import cs1396.escaperoom.screens.AbstractScreen;
import cs1396.escaperoom.screens.LevelEditor;
import cs1396.escaperoom.ui.Tooltip;
import cs1396.escaperoom.ui.widgets.G24Label;

/**
 * Utility class to manage the undo/redo history 
 * in the {@link LevelEditor} 
 */
public class ActionHistory {
  private final Array<EditorAction> actions = new Array<>();
  private int index = -1;
  private final ImageButton undoButton = new ImageButton(AbstractScreen.skin, "undo");
  private final ImageButton redoButton = new ImageButton(AbstractScreen.skin, "redo");

  private void buildToolTip(String title, Input input, Actor target){
    String label = title;
    label += "\n Hotkey(s): " + String.join("\n",input.description().bindings);
    G24Label l = new G24Label(label, "bubble_gray", 0.65f);
    l.pack();
    new Tooltip.Builder(l).target(target, () -> Optional.of(target.getStage())).build();
  }

  /**
   */
  public ActionHistory() {
    undoButton.setProgrammaticChangeEvents(false);
    undoButton.setDisabled(true);
    undoButton.addListener(new ChangeListener() {

      @Override
      public void changed(ChangeEvent event, Actor actor) {
        if (undoButton.isChecked()) {
          undoButton.setChecked(false);
          undo();
        }
      }

    });
    buildToolTip("Undo", Input.UNDO, undoButton);

    redoButton.setProgrammaticChangeEvents(false);
    redoButton.setDisabled(true);
    redoButton.addListener(new ChangeListener() {

      @Override
      public void changed(ChangeEvent event, Actor actor) {
        if (redoButton.isChecked()) {
          redoButton.setChecked(false);
          redo();
        }
      }

    });
    buildToolTip("Redo", Input.REDO, redoButton);
  }

  /**
   * @return the button which, when pressed, undoes an {@link EditorAction}
   */
  public ImageButton getUndoButton(){
    return undoButton;
  }

  /**
   * @return the button which, when pressed, redoes an {@link EditorAction}
   */
  public ImageButton getRedoButton(){
    return redoButton;
  }


  /**
   * @param action the action to record
   */
  public void record(EditorAction action) {
    for (int i = actions.size - 1; i > index; i--) {
      actions.pop();
    }
    undoButton.setDisabled(false);
    redoButton.setDisabled(true);
    actions.add(action);
    index += 1;
  }

  /**
   * @return whether or not there is an action to undo
   */
  public boolean undo() {
    if (index >= 0) {
      actions.get(index).undoAction();
      index -= 1;
      if (index <= 0) {
        undoButton.setDisabled(true);
      }
      redoButton.setDisabled(false);
      return true;
    } else {
      return false;
    }
  }

  /**
   * @return whether or not there is an action to redo
   */
  public boolean redo() {
    if (index < actions.size - 1) {
      index += 1;
      actions.get(index).doAction();
      if (index >= actions.size - 1) {
        redoButton.setDisabled(true);
      }
      undoButton.setDisabled(false);
      return true;
    } else {
      return false;
    }
  }

  /**
   * An EditorAction modifies the {@link LevelEditor} 
   * in a way that can be undone and redone
   */
  public interface EditorAction {

    /**
     * Called by {@link ActionHistory} to do an {@link EditorAction}
     */
    void doAction();

    /**
     * Called by {@link ActionHistory} to undo an {@link EditorAction}
     */
    void undoAction();
  }
}
