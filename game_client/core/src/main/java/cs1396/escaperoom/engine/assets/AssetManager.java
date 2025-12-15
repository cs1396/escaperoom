package cs1396.escaperoom.engine.assets;

import java.util.HashMap;
import java.util.Optional;
import java.util.logging.Logger;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;

/**
 * A wrapper around {@link com.badlogic.gdx.assets.AssetManager}
 */
public class AssetManager extends com.badlogic.gdx.assets.AssetManager {
  Logger log = Logger.getLogger(AssetManager.class.getName());
  Optional<TextureAtlas> userAtlas = Optional.empty();
  TextureAtlas defaultAtlas;
  HashMap<String, AtlasRegion> loadedTextures = new HashMap<>();

  private static AssetManager mgr;

  /**
   * @return the singleton {@link AssetManager}
   */
  public static AssetManager instance(){
    if (mgr == null){
      mgr = new AssetManager();
    }
    return mgr;
  }


  private AssetManager() { 
    load("texture_atlas/textureAtlas.atlas", TextureAtlas.class);
    finishLoadingAsset("texture_atlas/textureAtlas.atlas");
    this.defaultAtlas = get("texture_atlas/textureAtlas.atlas");
  }

  /**
   * @param atlas to register
   */
  public void registerUserAtlas(TextureAtlas atlas){
    userAtlas.ifPresent((ua) -> {
      // If we have a current user atlas, we need to invalidate the cache 
      // as new identifiers may reference the old atlas!
      //
      // Really kind of a classic stale cache situation
      loadedTextures.clear();
    });
    this.userAtlas = Optional.of(atlas);
  }

  public void clearUserTextures(){
    userAtlas.ifPresent((a) -> a.dispose());
    userAtlas = Optional.empty();
  }

  /**
   * Invalidate any cached textures in any of the {@link  TextureAtlas}s
   */
  public void invalidateTextureCache(){
    loadedTextures.clear();
  }

  /**
   * @param identifier the identifier of the region
   * @return the {@link AtlasRegion} or a placeholder texture if not found
   */
  public AtlasRegion getRegion(String identifier) {

    AtlasRegion cached = loadedTextures.get(identifier);
    if (cached != null){
      return cached;
    }

    AtlasRegion region = userAtlas.map((a) -> a.findRegion(identifier)).orElse(null);

    if (region != null){
      loadedTextures.put(identifier, region);
      return region;
    }  

    region = defaultAtlas.findRegion(identifier);
    if (region != null){
      loadedTextures.put(identifier, region);
      return region;
    }

    log.warning("Requested texture that does not exist! Requested: " + identifier);
    return defaultAtlas.findRegion("placeholder");
  }

  public Texture loadTextureBlocking(String path){
    try {
      AssetManager.instance().load(path, Texture.class);
      AssetManager.instance().finishLoadingAsset(path);
      return AssetManager.instance().get(path, Texture.class);
    } catch (Exception gdxre) {
      System.err.println("failed to load title img");
      return null;
    }
  }
}
