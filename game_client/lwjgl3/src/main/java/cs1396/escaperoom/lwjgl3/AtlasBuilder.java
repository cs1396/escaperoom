package cs1396.escaperoom.lwjgl3;

import java.io.File;

import com.badlogic.gdx.tools.texturepacker.TexturePacker;

public class AtlasBuilder {
  public AtlasBuilder() {}

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
  public static void buildAtlas(){
    File textureDir = new File("textures/entity_textures");
    File atlasDir = new File("texture_atlas");

    if (!atlasDir.exists()){
      if (!atlasDir.mkdir()){
        System.out.println("Failed to create atlas dir... exiting");
        System.exit(1);
      }
    }

    if (!textureDir.exists()){
        System.out.println("Texture path (" + textureDir.getPath() + ") does not exist...exiting");
        System.exit(1);
    }

    String atlasPath = atlasDir.getAbsolutePath() + "/textureAtlas.atlas";
    if (!needsBuild(textureDir,atlasPath )){
      return;
    }

    TexturePacker.Settings settings = new TexturePacker.Settings();
    settings.paddingX = 0;
    settings.paddingY = 0;
    settings.bleed = false;
    TexturePacker.process(settings,textureDir.getAbsolutePath(), atlasDir.getAbsolutePath(), "textureAtlas");

  }
}
