package cs1396.escaperoom.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import cs1396.escaperoom.editor.ui.ConfigurationMenu.HandlesMenuClose;

public class SimpleUI extends Table implements HandlesMenuClose {
  Actor element;

  public SimpleUI(){
    this.element = null;
  }
  public SimpleUI(Actor element){
    this.element = element;
    add(element).row();
  }

  @Override
  public void handle() { }
}

