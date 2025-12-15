package cs1396.escaperoom.services;

public class GameStatistics {
  public boolean completedSucessfully = false;
  public PlayerStatistics player = new PlayerStatistics();
  public long timeMilliseconds = 0;

  public static class PlayerStatistics {
    public float distanceTraveled = 0.0f;
    public float avgSpeed = 0.0f;
    public int actionsPerformed = 0;
    public int itemsCollected = 0;
  }

}
