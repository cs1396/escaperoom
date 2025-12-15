package cs1396.escaperoom.game.entities.properties.values;

public class ReadableContents implements StringItemPropertyValue {
  private String inner;

  /**
   * Empty constructor for {@code Json.Serializable} compatability
   */
  public ReadableContents(){
    this("<nothing here>");
  }
  public ReadableContents(String contents){
    inner = contents;
  }
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ReadableContents){
      return this.inner.equals(((ReadableContents)obj).inner);
    }
    return super.equals(obj);
  }

  @Override
  public String getValue() {
    return inner;
  }
  @Override
  public void setValue(String value) {
    this.inner = value;
  }
}
