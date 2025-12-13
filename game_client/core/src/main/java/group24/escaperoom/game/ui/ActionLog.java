package group24.escaperoom.game.ui;

import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import group24.escaperoom.screens.AbstractScreen;
import group24.escaperoom.screens.GameScreen;
import group24.escaperoom.ui.widgets.G24Label;

/**
 * Captures all player actions and emits to a UI element in the bottom left hand corner 
 * of the game screen
 * 
 */
public class ActionLog extends ScrollPane {
  private Table inner;

  /**
   * 
   */
  public ActionLog(GameScreen screen) {
    super(null, AbstractScreen.skin);
    setStyle(AbstractScreen.skin.get("transparent", ScrollPaneStyle.class));
    inner = new Table();
    this.setSize(300, 150);
    inner.bottom().left();
    inner.defaults().left().expandX();
    inner.setSize(300, 150);
    setActor(inner);
    this.pack();

    screen.getEventBus().addListener((ev) -> {
      String message = ev.toString();
      if (!message.isEmpty()){
        emit(message);
      }
    });
  }

  /**
   * @param message to emit 
   */
  public void emit(String message){
    emit(message, "white");
  }

  /**
   * @param content conten of a label 
   * @param style a label style to use
   */
  public void emit(String content, String style){
    inner.row();
    G24Label l = new G24Label(content, style);
    l.setWrap(true);
    l.setWidth(300);
    inner.add(l).expandX().width(300);
    inner.pack();
    this.scrollTo(0, 0, 1, 1);
  }
}
