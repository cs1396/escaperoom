package cs1396.escaperoom.ui;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

/**
 * Utility font classs for building custom one-off styles
 */
public class FontManager {
  public enum FontStyle {
    Regular("reg"),
    Light("light"),
    Bold("bold"),
    ;

    private HashMap <Integer, BitmapFont> sizes = new HashMap<>();
    private FreeTypeFontGenerator generator;

    private FontStyle(String fileName){
      generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/" + fileName + ".ttf"));
    }
  }

  public static BitmapFont size(int pixels){
    return size(FontStyle.Regular, pixels);
  }

  public static BitmapFont size(FontStyle style, int pixels){
    if (!style.sizes.containsKey(pixels)){
      FreeTypeFontParameter parameter = new FreeTypeFontParameter();
      parameter.size = pixels;
      BitmapFont font = style.generator.generateFont(parameter);
      style.sizes.put(pixels, font);
    }

    return style.sizes.get(pixels);
  }
}
