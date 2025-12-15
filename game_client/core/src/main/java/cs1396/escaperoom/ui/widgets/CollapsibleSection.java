package cs1396.escaperoom.ui.widgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import cs1396.escaperoom.screens.AbstractScreen;

/**
 * A UI element for building collapsible sections
 */
public class CollapsibleSection extends Tree<cs1396.escaperoom.ui.widgets.CollapsibleSection.AnyNode, Void>{

  private Window findWindowParent(){
    Actor a = this;
    while (a != null){
      if (a instanceof Window){
        return (Window)a;
      }
      a = a.getParent();
    }
    return null;
  }


  /**
   * @param label for the collapsible section
   * @param contents to contain
   */
  public CollapsibleSection(Label label, Actor contents){
    super(AbstractScreen.skin);
    setIndentSpacing(0);
    setYSpacing(10);
    AnyNode root = new AnyNode(label);
    root.add(new AnyNode(contents));
    add(root);
    addListener(new ClickListener(){
      @Override
      public void clicked (InputEvent event, float x, float y) {
          invalidateHierarchy();

        Window w = findWindowParent();
        if (w != null) {
          w.pack();
          w.setPosition(w.getStage().getWidth() / 2, w.getStage().getHeight() / 2, Align.center);
        }
      }
    });
  }
  class AnyNode extends Tree.Node<AnyNode, Actor, Actor>{
    AnyNode(Actor a){
      setActor(a);
      setSelectable(false);
    }
  }
}
