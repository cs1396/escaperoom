package cs1396.escaperoom.services;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import cs1396.escaperoom.services.Networking.StatusCode;

public class User {
  public static class Credentials {
    public String username;
    public UUID access_key;

    private Credentials(String username, UUID access_key) {
      this.username = username;
      this.access_key = access_key;
    }
  }

  private static User current = new User();

  protected String username;
  protected UUID player_id, access_key;
  protected PlayerRecord record;

  public static PlayerRecord getRecord(){
    return current.record;
  }

  private User() {
  }

  public static Credentials getCredentials() {
    return new Credentials(current.username, current.access_key);
  }

  /**
   * Using the Networking APIs, log the user in.
   * Operates in-place, so the user calling this function will be logged in if
   * successful.
   *
   * Requires:
   * - username
   * - password
   *
   * Updates on success:
   * - sets username
   * - sets player_id
   * - sets access_key
   *
   * Returns {@code StatusCode} for success/fail handling:
   * - {@code StatusCode.OK}: success
   * - anything else: error
   */
  public static CompletableFuture<StatusCode> AttemptLogin(String username, String password) {
    current.username = username;
    return Networking.attemptLoginAPI(current, password).thenCompose((code) -> {
      if (code != StatusCode.OK) {
        return CompletableFuture.supplyAsync(() -> code);
      }

      return Networking.getPlayerRecord(current.username).thenApply((prr) -> {
        // if this player doesn't have a record, create one
        if (prr.code == StatusCode.PageMissing) {
          current.record = new PlayerRecord();
          current.record.username = current.username;
          return StatusCode.OK;
        } else {
          if (prr.code == StatusCode.OK) {
            current.record = prr.record;
          }
          return prr.code;
        }
      });
    });
  }

  public static void logOut() {
    current.username = null;
    current.access_key = null;
    current.player_id = null;
  }

  /**
   * Using the Networking APIs, create a new user and log them in.
   * If a user with the name already exists, this function will fail
   * Operates in-place, so the user calling this function will be logged in if
   * successful.
   *
   * Requires:
   * - username
   * - password
   *
   * Updates on success:
   * - clears password
   * - sets player_id
   * - sets access_key
   *
   * Returns {@code StatusCode} for success/fail handling:
   * - {@code StatusCode.OK}: success
   * - anything else: error
   */
  public static CompletableFuture<StatusCode> createAccount(String username, String password) {
    current.username = username;
    return Networking.createUserAPI(current, password);
  }

  /**
   * Using the Networking APIs, check if a user exists by checking their username.
   *
   * Requires:
   * - username
   *
   * Returns {@code StatusCode} for success/fail handling:
   * - {@code StatusCode.OK}: success
   * - anything else: error
   */
  public static CompletableFuture<StatusCode> hasAccount(String username) {
    return Networking.userExists(username);
  }

  /**
   * Check if the user is logged in.
   */
  public static boolean isLoggedIn() {
    return current.access_key != null;
  }
}
