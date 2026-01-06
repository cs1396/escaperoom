package cs1396.escaperoom.ui.widgets;

import com.badlogic.gdx.scenes.scene2d.ui.Label;

import cs1396.escaperoom.screens.AbstractScreen;

public class G24Label extends Label {

  public enum G24LabelStyle {
    Default("default"),
    DefaultMedText("default-med-text"),
    Underline("underline"),
    Bubble("bubble"),
    BubbleSelect("bubble_select"),
    BubbleGray("bubble_gray"),
    White("white"),
    Title("title"),
    ;

    private String styleID;
    private G24LabelStyle(String styleID){ this.styleID = styleID; }
  }


  public G24Label(String content){
    this(content, "default", 0.65f);
  }

  public G24Label(String content, G24LabelStyle style){
    this(content, style.styleID, 0.65f);
  }

  public G24Label(String content, String style, float scale){
    super(content, AbstractScreen.skin);
    setStyle(AbstractScreen.skin.get(style, LabelStyle.class));
  }
}
