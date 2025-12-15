package cs1396.escaperoom.ui.widgets;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.scenes.scene2d.ui.Window;

import cs1396.escaperoom.screens.AbstractScreen;

public class G24Window extends Window  {
  protected G24Label label;
  public G24Window(String title){
    super(title, AbstractScreen.skin);
    
    label = G24StyleWindow(this, title);
  }

  public G24Window(String title,  String style){
    super(title, AbstractScreen.skin, style);
  }
  public static G24Label G24StyleWindow(Window window, String title){
    return G24StyleWindow(window, title, "default");
  }

  public static G24Label G24StyleWindow(Window window, String title, String style){
    window.setStyle(AbstractScreen.skin.get(style, WindowStyle.class));
    window.getTitleTable().clearChildren();
    window.getTitleTable().defaults().pad(10);
    G24Label label = null;
    if (!title.isEmpty()){
      label = new G24Label(title, "bubble");
    }
    window.getTitleTable().add(label).align(Align.center).padBottom(15);
    window.padTop(40);
    return label;
  }

  @Override 
  public Label getTitleLabel(){
    return label;
  }

  public void close() {
    remove();
  }
}
