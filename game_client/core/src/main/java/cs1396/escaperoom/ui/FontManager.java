package cs1396.escaperoom.ui;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

/**
 * Utility font classs for building custom one-off styles
 */
public class FontManager {
  public enum FontStyle {
    Regular("reg"),
    Light("light"),
    Bold("bold"),
    ;

    private HashMap <Integer, HashMap<Color, BitmapFont>> fonts = new HashMap<>();
    private FreeTypeFontGenerator generator;

    private FontStyle(String fileName){
      generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/" + fileName + ".ttf"));
    }
  }

  public static BitmapFont get(FontBuilder builder){

    if (builder.style.fonts.get(builder.size) == null){
      builder.style.fonts.put(builder.size, new HashMap<>());
    }

    if (builder.style.fonts.get(builder.size).get(builder.color) == null){
      BitmapFont font = builder.style.generator.generateFont(builder.asParams());
      builder.style.fonts.get(builder.size).put(builder.color, font);
    }

    return builder.style.fonts.get(builder.size).get(builder.color);
  }

}
