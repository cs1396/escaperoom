package cs1396.escaperoom.game.entities.properties.locks;

import cs1396.escaperoom.game.entities.properties.base.LockingMethod;

public enum LockingMethodType {
  KeyLock("key_lock", KeyLock.class),
  PassphraseLock("pass_phrase", PassphraseLock.class),
  TrivialLock("trivial", TrivialLock.class),
  CombinationLock("combo_lock", CombinationLock.class),
  PowerLock("power", PowerLock.class);
  public String key;
  public Class<? extends LockingMethod> clz;

  private LockingMethodType(String jsonKey, Class<? extends LockingMethod> clz){
    this.clz = clz;
    this.key = jsonKey;
  }
  public static LockingMethodType  fromString(String s){
    for (LockingMethodType t : LockingMethodType.values()){
      if (t.key.equals(s)){
        return t;
      }
    }
    return null;
  }

}
