package cs1396.escaperoom.engine.assets;

import java.io.File;
import java.util.Optional;
import java.util.logging.Logger;

import com.badlogic.gdx.tools.texturepacker.TexturePacker;

import cs1396.escaperoom.engine.types.Result;
import cs1396.escaperoom.engine.types.Result.IOErr;
import cs1396.escaperoom.engine.types.Result.Ok;

public class UserAtlasBuilder {
  static Logger log = Logger.getLogger(UserAtlasBuilder.class.getName());

  public UserAtlasBuilder() {}

  /**
   * We need to build the atlas if 
   * 1. the atlas does not exist
   * 2. there are textures in the user texture directory which 
   * are newer than the atlas
   */
  private static boolean needsBuild(File textureDir, File atlas){
    if (!atlas.exists()) return true; 

    for (File f : textureDir.listFiles()){
      if (f.lastModified() > atlas.lastModified()) return true;
    }

    return false;
  }

  public static Result<File, String> buildAtlas(String textureDirPath){
    File textureDir = new File(textureDirPath);

    if (!textureDir.exists()){
      return IOErr.withLog("Texture path (" + textureDir.getPath() + ") does not exist", log);
    }

    String atlasDirPath = textureDir.getParent() + "/texture_atlas";

    File atlasDir = new File(atlasDirPath);

    if (!atlasDir.exists()){
      if (!atlasDir.mkdir()){
        return IOErr.withLog("Failed to create atlas dir: " + atlasDir.getAbsolutePath(), log);
      }
    }

    File atlas = new File(atlasDir, "atlas.atlas");

    if (!needsBuild(textureDir, atlas)){
      return new Ok<>(atlas);
    }

    try {
      TexturePacker.Settings settings = new TexturePacker.Settings();
      settings.paddingX = 0;
      settings.paddingY = 0;
      settings.maxWidth = 2048;
      settings.maxHeight = 2048;
      settings.bleed = false;
      TexturePacker.process(settings, textureDir.getAbsolutePath(), atlasDir.getAbsolutePath(), "atlas");
      AssetManager.instance().invalidateTextureCache();
      return new Ok<>(atlas);
    } catch (Exception e){
      e.printStackTrace();
      return IOErr.withLog("Failed to pack atlas", log);
    }
  }
}
