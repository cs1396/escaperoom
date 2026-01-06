package cs1396.escaperoom.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

import cs1396.escaperoom.ui.FontManager.FontStyle;

public class FontBuilder {
  int size = 21;
  Color color = new Color(0.39f, 0.39f, 0.39f, 1f);
  FontStyle style = FontStyle.Regular;

  public FontBuilder() {
  }

  public FontBuilder size(int size) {
    this.size = size;
    return this;
  }

  public FontBuilder style(FontStyle style) {
    this.style = style;
    return this;
  }

  /**
   * Note: For a FreetypeFont to have the correct color
   * both the generated font **and** the skin style must 
   * have the same color
   */
  public FontBuilder color(Color color) {
    this.color = color;
    return this;
  }

  public BitmapFont build() {
    return FontManager.get(this);
  }

  FreeTypeFontParameter asParams() {
    FreeTypeFontParameter parameter = new FreeTypeFontParameter();
    parameter.size = size;
    if (color != null) parameter.color = color;
    return parameter;
  }

}
