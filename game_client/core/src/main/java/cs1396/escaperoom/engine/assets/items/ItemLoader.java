package cs1396.escaperoom.engine.assets.items;

import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.logging.Logger;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import cs1396.escaperoom.engine.assets.AssetManager;
import cs1396.escaperoom.engine.assets.utils.FileUtils;
import cs1396.escaperoom.ui.notifications.Notifier;

public class ItemLoader {
  public static Logger log = Logger.getLogger(ItemLoader.class.getName());
  public static class LoadedObjects {
    public static class ItemMap extends HashMap<String, HashMap<String, ItemTypeData>>{}
    // category -> {type name -> type data}
    private static ItemMap itemTypes = new ItemMap();
    private static ItemMap userItems = new ItemMap();

    private static boolean validName(String name){
      return !(name == null || name.isEmpty() || name.isBlank());
    }
    public static Optional<ItemTypeData> getItem(String category, String name){
      if (!validName(category) || !validName(name)){
        return Optional.empty();
      }

      HashMap<String, ItemTypeData>  map = userItems.get(category);

      if (map != null){
        ItemTypeData data = map.get(name);
        if (data != null){
          return Optional.of(data);
        }
      } 

      map = itemTypes.get(category);
      if (map != null){
        ItemTypeData data = map.get(name);
        if (data != null){
          return Optional.of(data);
        }
      } 

      return Optional.empty();
    }

    public static HashSet<String> getUserCategories(){
      HashSet<String> set = new HashSet<>();
      set.addAll(userItems.keySet());
      return set;
    }

    public static HashSet<String> getCategories(){
      HashSet<String> set = new HashSet<>();
      set.addAll(userItems.keySet());
      set.addAll(itemTypes.keySet());
      return set;
    }

    public static HashSet<ItemTypeData> getUserItems(String category) {
      HashSet<ItemTypeData> set = new HashSet<>();
      HashMap<String, ItemTypeData>  map = userItems.get(category);
      if (map != null){
        set.addAll(map.values());
      }
      return set;
    }

    public static HashSet<ItemTypeData> getItems(String category) {
      HashSet<ItemTypeData> set = new HashSet<>();
      HashMap<String, ItemTypeData>  map = userItems.get(category);
      if (map != null){
        set.addAll(map.values());
      }

      map = itemTypes.get(category);
      if (map != null){
        set.addAll(map.values());
      }
      return set;
    }
    public static void clearUserItems(){
      userItems.clear();
      AssetManager.instance().clearUserTextures();
    }

  }



  public static void LoadAllObjects() {
    loadTypes("objects", LoadedObjects.itemTypes);
  }

  public static void LoadUserObjects(String objectDir) {
    loadTypes(objectDir, LoadedObjects.userItems);
  }

  private static void loadTypes(String objectFolder, LoadedObjects.ItemMap itemMap) {
    for (String category : FileUtils.getFolders(objectFolder)) {

      if (!itemMap.containsKey(category)){
        itemMap.put(category, new HashMap<>());
      }

      for (String filename : FileUtils.getFiles(objectFolder + "/" + category)) {
        JsonValue object;
        try {
          FileReader fileReader = new FileReader(objectFolder + "/" + category + "/" + filename);
          JsonReader reader = new JsonReader();
          object = reader.parse(fileReader);
        } catch (Exception e) {
          Notifier.error(String.format("Failed to load item from %s", filename));
          continue;
        }
        ItemTypeData data = new ItemTypeData();
        data.read(new Json(), object);
        data.category = category;
        itemMap.get(category).put(data.name, data);
      }
    }
  }

}
