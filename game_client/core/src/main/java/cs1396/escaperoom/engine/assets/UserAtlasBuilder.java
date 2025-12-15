package cs1396.escaperoom.engine.assets;

import java.io.File;
import java.util.Optional;
import java.util.logging.Logger;

import com.badlogic.gdx.tools.texturepacker.TexturePacker;

public class UserAtlasBuilder {
  static Logger log = Logger.getLogger(UserAtlasBuilder.class.getName());

  public UserAtlasBuilder() {}

  /**
   * We need to build the atlas if 
   * 1. the atlas does not exist
   * 2. there are textures in the user texture directory which 
   * are newer than the atlas
   */
  private static boolean needsBuild(File textureDir, String atlasPath){
    File atlasFile = new File(atlasPath);
    if (!atlasFile.exists()){
      return true;
    }

    for (File f : textureDir.listFiles()){
      if (f.lastModified() > atlasFile.lastModified()){
        return true;
      }
    }

    return false;

  }

  public static Optional<String> buildAtlas(String textureDirPath){
    File textureDir = new File(textureDirPath);

    if (!textureDir.exists()){
        log.warning("Texture path (" + textureDir.getPath() + ") does not exist");
        return Optional.empty();
    }

    String atlasDirPath = textureDir.getParent() + "/texture_atlas";

    File atlasDir = new File(atlasDirPath);

    if (!atlasDir.exists()){
      if (!atlasDir.mkdir()){
        log.warning("Failed to create atlas dir: " + atlasDir.getAbsolutePath());
        return Optional.empty();
      }
    }

    String atlasPath = atlasDir.getAbsolutePath() + "/atlas.atlas";

    if (!needsBuild(textureDir, atlasPath)){
      return Optional.of(atlasPath);
    }

    TexturePacker.Settings settings = new TexturePacker.Settings();
    settings.paddingX = 0;
    settings.paddingY = 0;
    settings.maxWidth = 2048;
    settings.maxHeight = 2048;
    settings.bleed = false;
    TexturePacker.process(settings, textureDir.getAbsolutePath(), atlasDir.getAbsolutePath(), "atlas");
    AssetManager.instance().invalidateTextureCache();

    return Optional.of(atlasPath);
  }
}
