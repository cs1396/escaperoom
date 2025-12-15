package cs1396.escaperoom.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.JsonValue;

import cs1396.escaperoom.engine.assets.maps.MapMetadata;
import cs1396.escaperoom.engine.assets.maps.MapMetadata.MapLocation;
import cs1396.escaperoom.engine.assets.maps.MapMetadata.MapStats.ValidStats;
import cs1396.escaperoom.engine.assets.utils.Zip;
import cs1396.escaperoom.services.Types.ListMapsRequest;
import cs1396.escaperoom.services.Types.ListMapsResponse;
import cs1396.escaperoom.services.Types.ListPlayerRecordRequest;
import cs1396.escaperoom.services.Types.ListPlayerRecordResponse;
import cs1396.escaperoom.services.Types.PlayerRecordResponse;
import cs1396.escaperoom.services.User.Credentials;

public class Networking {
  // base API for all net calls
  private static final String BASE_API = "https://e9808rovje.execute-api.us-east-2.amazonaws.com/default";

  // measured status codes
  public static enum StatusCode {
    CodeUnknown,
    InternalException,
    ServerException,
    NoSuchUser,
    UserAlreadyExists,
    PageMissing,
    WrongPassword,
    Forbidden,
    OK;

    public static StatusCode FromInt(int code) {
      switch (code) {
        case 200:
          return OK;
        case 400:
          return NoSuchUser;
        case 401:
          return Forbidden;
        case 403:
          return WrongPassword;
        case 404:
          return PageMissing;
        case 409:
          return UserAlreadyExists;
        case 500:
          return ServerException;
        default:
          return CodeUnknown;
      }
    }
  }

  // util 'struct' for API call intermediates
  private static class RequestResponse {
    StatusCode code;
    boolean hasBody;
    InputStream body;
  }

  /**
   * Perform a networking API request to a URL with the given method.
   *
   * Returns a {@code RequestResponse}.
   */
  static CompletableFuture<RequestResponse> performRequest(String url, String method, boolean isAuthenticated) {
    RequestResponse rsp = new RequestResponse();
    if (isAuthenticated && !User.isLoggedIn()) {
      rsp.code = StatusCode.Forbidden;
      rsp.hasBody = false;
      return CompletableFuture.supplyAsync(() -> rsp);
    }

    CompletableFuture<RequestResponse> future = CompletableFuture.supplyAsync(() -> {
      HttpsURLConnection connection;
      int statusCode = 0;
      try {
        URL netUrl = URI.create(url).toURL();
        connection = (HttpsURLConnection) netUrl.openConnection();
        connection.setRequestMethod(method);

        if (isAuthenticated) {
          Credentials creds = User.getCredentials();
          connection.setRequestProperty("Authorization", String.format("%s@%s", creds.username, creds.access_key));
        }

        connection.setDoOutput(true);
        statusCode = connection.getResponseCode();
      } catch (Exception e) {
        rsp.code = StatusCode.InternalException;
        rsp.hasBody = false;
        rsp.body = null;
        System.out.println("errored (" + statusCode + "):");
        e.printStackTrace();
        return rsp;
      }

      try {
        rsp.body = connection.getInputStream();
        rsp.hasBody = true;
      } catch (Exception e) {
        rsp.body = null;
        rsp.hasBody = false;
      }

      rsp.code = StatusCode.FromInt(statusCode);

      return rsp;
    });

    return future;
  }

  private static class MultipartFields {
    HashMap<String, String> strings = new HashMap<>();
    HashMap<String, String> files = new HashMap<>();
  }

  public static CompletableFuture<RequestResponse> postJson(String url, String json,
      boolean isAuthenticated) {
    RequestResponse rsp = new RequestResponse();

    if (isAuthenticated && !User.isLoggedIn()) {
      System.out.println("you must be logged in to use this");
      rsp.code = StatusCode.Forbidden;
      rsp.hasBody = false;
      return CompletableFuture.supplyAsync(() -> rsp);
    }

    return CompletableFuture.supplyAsync(() -> {
      CloseableHttpClient httpClient = HttpClients.createDefault();
      HttpPost postRequest = new HttpPost(url);

      if (isAuthenticated) {
        Credentials creds = User.getCredentials();
        postRequest.setHeader("Authorization", String.format("%s@%s", creds.username, creds.access_key));
      }

      postRequest.setHeader("Content-Type", "application/json");
      postRequest.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));

      try {
        CloseableHttpResponse response = httpClient.execute(postRequest);
        HttpEntity httpResponse = response.getEntity();
        rsp.body = httpResponse.getContent();
        rsp.code = StatusCode.OK;
      } catch (IOException e) {
        rsp.code = StatusCode.InternalException;
        rsp.hasBody = false;
        rsp.body = null;
        System.out.println("errored (" + rsp.code + "):");
        e.printStackTrace();
        return rsp;
      }

      if (rsp.body == null)
        rsp.hasBody = false;
      else
        rsp.hasBody = true;

      return rsp;
    });
  }

  public static CompletableFuture<RequestResponse> HttpMultipartPost(String url, MultipartFields fields,
      boolean isAuthenticated) {
    RequestResponse rsp = new RequestResponse();

    if (isAuthenticated && !User.isLoggedIn()) {
      System.out.println("you must be logged in to use this");
      rsp.code = StatusCode.Forbidden;
      rsp.hasBody = false;
      return CompletableFuture.supplyAsync(() -> rsp);
    }

    return CompletableFuture.supplyAsync(() -> {
      CloseableHttpClient httpClient = HttpClients.createDefault();
      HttpPost postRequest = new HttpPost(url);

      if (isAuthenticated) {
        Credentials creds = User.getCredentials();
        postRequest.setHeader("Authorization", String.format("%s@%s", creds.username, creds.access_key));
      }

      MultipartEntityBuilder builder = MultipartEntityBuilder.create();

      for (Entry<String, String> pair : fields.strings.entrySet()) {
        builder.addTextBody(pair.getKey(), pair.getValue(), ContentType.TEXT_PLAIN);
      }

      try {
        for (Entry<String, String> pair : fields.files.entrySet()) {
          File f = new File(pair.getValue());
          builder.addBinaryBody(
              pair.getKey(),
              new FileInputStream(f),
              ContentType.APPLICATION_OCTET_STREAM,
              f.getName());
        }
      } catch (FileNotFoundException e) {
        rsp.code = StatusCode.InternalException;
        rsp.hasBody = false;
        rsp.body = null;
        System.out.println("errored (" + rsp.code + "):");
        e.printStackTrace();
        return rsp;
      }

      postRequest.setEntity(builder.build());
      try {
        CloseableHttpResponse response = httpClient.execute(postRequest);
        HttpEntity httpResponse = response.getEntity();
        rsp.body = httpResponse.getContent();
        rsp.code = StatusCode.OK;
      } catch (IOException e) {
        rsp.code = StatusCode.InternalException;
        rsp.hasBody = false;
        rsp.body = null;
        System.out.println("errored (" + rsp.code + "):");
        e.printStackTrace();
        return rsp;
      }

      if (rsp.body == null)
        rsp.hasBody = false;
      else
        rsp.hasBody = true;

      return rsp;
    });
  }

  /**
   * Networking API call to create a new user.
   *
   * Requires:
   * - user.username
   * - password
   *
   * Updates User parameter on success:
   * - clears password
   * - sets player_id
   * - sets access_key
   *
   * Returns {@code StatusCode} for success/fail handling:
   * - {@code StatusCode.OK}: success
   * - anything else: error
   */
  public static CompletableFuture<StatusCode> createUserAPI(User user, String password) {
    String url = BASE_API + "/user/" + user.username + "/" + password;

    return performRequest(url, "POST", false).thenApply((RequestResponse rsp) -> {
      if (rsp.code != StatusCode.OK) {
        return rsp.code;
      } else if (!rsp.hasBody) {
        return StatusCode.InternalException;
      }

      JsonReader reader = new JsonReader();
      JsonValue val = reader.parse(rsp.body);
      val = val.get("user");

      user.player_id = UUID.fromString(val.getString("id"));
      user.access_key = UUID.fromString(val.getString("access_key"));

      return StatusCode.OK;
    });

  }

  /**
   * Networking API call to log in to an existing user.
   *
   * Requires:
   * - user.username
   * - password
   *
   * Updates User parameter on success:
   * - clears password
   * - sets player_id
   * - sets access_key
   *
   * Returns {@code StatusCode} for success/fail handling:
   * - {@code StatusCode.OK}: success
   * - anything else: error
   */
  public static CompletableFuture<StatusCode> attemptLoginAPI(User user, String password) {
    String url = BASE_API + "/user/" + user.username + "/" + password;
    return performRequest(url, "GET", false).thenApply((RequestResponse rsp) -> {
      if (rsp.code != StatusCode.OK) {
        return rsp.code;
      } else if (!rsp.hasBody) {
        return StatusCode.InternalException;
      }

      JsonReader reader = new JsonReader();
      JsonValue val = reader.parse(rsp.body);
      val = val.get("user");

      user.player_id = UUID.fromString(val.getString("id"));
      user.access_key = UUID.fromString(val.getString("access_key"));

      return StatusCode.OK;
    });
  }

  /**
   * Networking API call to check if a user exists.
   *
   * Requires:
   * - user.username
   *
   * Returns {@code StatusCode} for success/fail handling:
   * - {@code StatusCode.OK}: success
   * - anything else: error
   */
  public static CompletableFuture<StatusCode> userExists(User user) {
    return userExists(user.username);
  }

  /**
   * Networking API call to check if a user exists.
   *
   * Returns {@code StatusCode} for success/fail handling:
   * - {@code StatusCode.OK}: success
   * - anything else: error
   */
  public static CompletableFuture<StatusCode> userExists(String username) {
    String url = BASE_API + "/user/" + username;
    return performRequest(url, "HEAD", false).thenApply((rsp) -> rsp.code);
  }

  public static class UploadResponse {
    public StatusCode code;
    public String mapID;

    public UploadResponse(StatusCode code) {
      this.code = code;
      this.mapID = null;
    }

    public UploadResponse(StatusCode code, String mapID) {
      this.code = code;
      this.mapID = mapID;
    }
  }

  /**
   * Networking API call to upload a user map.
   *
   * Preconditions:
   * - user must be logged in
   *
   * Requires:
   * - path to folder containing map data
   * - json string of metadata for the map
   *
   * Returns an {@code UploadResponse} which contains a success code and
   * potential map ID. If the {@code UploadResponse.code} is
   * {@code StatusCode.OK}, then the map ID exists and is valid. Otherwise,
   * the upload failed and there is no map ID provided.
   */
  public static CompletableFuture<UploadResponse> uploadUserMap(MapLocation locations, String metadata) {
    if (!User.isLoggedIn()) {
      return CompletableFuture.supplyAsync(() -> new UploadResponse(StatusCode.Forbidden));
    }

    // first zip the map
    if (!Zip.CreateArchive(locations.mapContentPath)) {
      System.err.printf("Failed to zip file `%s`\n", locations.mapContentPath);
      return CompletableFuture.supplyAsync(() -> new UploadResponse(StatusCode.InternalException));
    }

    MultipartFields fields = new MultipartFields();
    fields.strings.put("meta", metadata);
    fields.files.put("upload", String.format("%s.zip", locations.mapContentPath));
    fields.files.put("thumbnail", locations.mapThumbnailPath);

    // do the multipart
    return HttpMultipartPost(String.format("%s/map", BASE_API), fields, true).thenApply((rsp) -> {

      // response checking
      if (rsp.code != StatusCode.OK) {
        System.err.println("Got non-200 status code from uploading map");
        return new UploadResponse(StatusCode.ServerException);
      } else if (!rsp.hasBody) {
        System.err.println("Got no response body from uploading map");
        return new UploadResponse(StatusCode.ServerException);
      }

      // get the returned ID
      JsonReader reader = new JsonReader();
      JsonValue val = reader.parse(rsp.body);
      String mapID = val.getString("map_id");

      // remove the archive, as it has been uploaded
      new File(String.format("%s.zip", locations.mapContentPath)).delete();

      return new UploadResponse(StatusCode.OK, mapID);
    });

  }

  /**
   * Networking API call to download a user map.
   *
   * Requires:
   * - id of the map to download
   *
   * Returns {@code StatusCode} for success/fail handling:
   * - {@code StatusCode.OK}: success
   * - anything else: error
   */
  public static CompletableFuture<StatusCode> downloadUserMap(String mapID, String destination) {
    // make and perform get request
    String url = BASE_API + "/map/" + mapID;
    return performRequest(url, "GET", true).thenApply((rsp) -> {
      // ensure code and body
      if (rsp.code != StatusCode.OK) {
        System.err.printf("recieved non-200 response code: %s\n", rsp.code.toString());
        return rsp.code;
      } else if (!rsp.hasBody) {
        System.err.println("response contained no body");
        return StatusCode.InternalException;
      }

      // ensure map directory
      File mapDir = new File(destination);
      if (!mapDir.exists() || !mapDir.isDirectory()) {
        System.err.println("Map directory not found or not directory. Investigate immediately.");
        return StatusCode.InternalException;
      }

      // copy response to a temp file, and unzip
      try {
        File dl = File.createTempFile("mapdl", ".zip", mapDir);
        Files.copy(rsp.body, dl.toPath(), StandardCopyOption.REPLACE_EXISTING);

        if (!Zip.UnpackArchive(dl.getAbsolutePath(), mapDir)) {
          System.err.println("unable to unzip archive");
          return StatusCode.InternalException;
        }

        dl.delete();
      } catch (IOException e) {
        e.printStackTrace();
        return StatusCode.InternalException;
      }

      return StatusCode.OK;
    });
  }

  /**
   * Networking API call to list published maps' metadata.
   *
   * Preconditions:
   * - user must be logged in
   *
   * Requires:
   * - id of the map cleared
   * - cleartime in ms
   *
   * Returns {@code StatusCode} for success/fail handling:
   * - {@code StatusCode.OK}: success
   * - anything else: error
   */
  public static CompletableFuture<ListMapsResponse> listMapMetadata(ListMapsRequest request) {
    if (!User.isLoggedIn()) {
      return CompletableFuture.supplyAsync(() -> new ListMapsResponse(StatusCode.Forbidden));
    }

    String url = String.format("%s/map/metadata", BASE_API);
    String requestQuery = request.toString();
    if (requestQuery.length() > 0) {
      url += "?" + requestQuery;
    }

    return performRequest(url, "GET", true).thenApply((rsp) -> {
      if (rsp.code != StatusCode.OK) {
        System.out.println("List map response recieved a non-200 code: " + rsp.code.toString());
        return new ListMapsResponse(rsp.code);
      }
      if (!rsp.hasBody) {
        System.out.println("List map returned no body. Treating as error.");
        return new ListMapsResponse(StatusCode.InternalException);
      }

      Array<MapMetadata> mapMeta = new Array<>();
      try {
        JsonReader reader = new JsonReader();
        JsonValue val = reader.parse(rsp.body);
        val = val.get("metadata");

        Json json = new Json();
        for (JsonValue v : val) {
          mapMeta.add(json.readValue(MapMetadata.class, v));
        }

      } catch (Exception e) {
        e.printStackTrace();
        return new ListMapsResponse(StatusCode.InternalException);
      }

      return new ListMapsResponse(StatusCode.OK, mapMeta);
    });
  }

  /**
   * Networking API call to list global player records
   *
   * Preconditions:
   * - user must be logged in
   *
   * Returns an {@code ListPlayerRecordRequest} which contains a success code and
   * potential array of records. If the {@code ListPlayerRecordResponse.code} is
   * {@code StatusCode.OK}, then the array exists and is valid. Otherwise,
   * the fetch failed and there is no array provided.
   */
  public static CompletableFuture<ListPlayerRecordResponse> listPlayerRecords(ListPlayerRecordRequest request) {
    if (!User.isLoggedIn()) {
      return CompletableFuture.supplyAsync(() -> new ListPlayerRecordResponse(StatusCode.Forbidden));
    }

    String url = String.format("%s/leaderboard/users", BASE_API);
    String requestQuery = request.toString();
    if (requestQuery.length() > 0) {
      url += "?" + requestQuery;
    }

    return performRequest(url, "GET", true).thenApply((rsp) -> {
      if (rsp.code != StatusCode.OK) {
        System.out.println("List map response recieved a non-200 code: " + rsp.code.toString());
        return new ListPlayerRecordResponse(rsp.code);
      }
      if (!rsp.hasBody) {
        System.out.println("List map returned no body. Treating as error.");
        return new ListPlayerRecordResponse(StatusCode.InternalException);
      }

      Array<PlayerRecord> records = new Array<>();
      try {
        JsonReader reader = new JsonReader();
        JsonValue val = reader.parse(rsp.body);
        val = val.get("records");

        Json json = new Json();
        for (JsonValue v : val) {
          records.add(json.readValue(PlayerRecord.class, v));
        }

      } catch (Exception e) {
        e.printStackTrace();
        return new ListPlayerRecordResponse(StatusCode.InternalException);
      }

      return new ListPlayerRecordResponse(StatusCode.OK, records);
    });
  }

  /**
   * Networking API call to register a map completion.
   *
   * Preconditions:
   * - user must be logged in
   *
   * Requires:
   * - id of the map cleared
   * - cleartime in ms
   *
   * Returns {@code StatusCode} for success/fail handling:
   * - {@code StatusCode.OK}: success
   * - anything else: error
   */
  public static CompletableFuture<StatusCode> sendMapClear(String mapID, long clearTime) {
    if (!User.isLoggedIn()) {
      return CompletableFuture.supplyAsync(() -> StatusCode.Forbidden);
    }

    String url = String.format("%s/map/%s/clear/%d", BASE_API, mapID, clearTime);
    return performRequest(url, "POST", true).thenApply((rsp) -> {
      if (rsp.code != StatusCode.OK) {
        System.err.printf("Failed to send map clear, got code %s\n", rsp.code.toString());
      }

      return rsp.code;
    });
  }

  /**
   * Networking API call to send map stat updates.
   *
   * Preconditions:
   * - user must be logged in
   *
   * Requires:
   * - id of the map to update
   * - list of stats to increase. all given will increase by 1
   *
   * Returns {@code StatusCode} for success/fail handling:
   * - {@code StatusCode.OK}: success
   * - anything else: error
   */
  public static CompletableFuture<StatusCode> updateMapStats(String mapID, Array<ValidStats> stats) {
    if (!User.isLoggedIn()) {
      return CompletableFuture.supplyAsync(() -> StatusCode.Forbidden);
    }

    StringBuilder sb = new StringBuilder();
    for (ValidStats stat : stats) {
      if (sb.length() == 0) {
        sb.append(stat.toString());
      } else {
        sb.append('&');
        sb.append(stat.toString());
      }
    }

    String url = String.format("%s/map/%s/stat?%s", BASE_API, mapID, sb.toString());
    return performRequest(url, "POST", true).thenApply((rsp) -> {
      if (rsp.code != StatusCode.OK) {
        System.err.printf("Failed to send map clear, got code %s\n", rsp.code.toString());
      }

      return rsp.code;
    });
  }

  /**
   * Networking API call to download a maps metadata.
   *
   * Preconditions:
   * - user must be logged in
   *
   * Requires:
   * - id of the map to query
   *
   * Returns {@code StatusCode} for success/fail handling:
   * - {@code StatusCode.OK}: success
   * - anything else: error
   */
  public static CompletableFuture<StatusCode> downloadMapMetadata(String mapID, String destination) {
    if (!User.isLoggedIn()) {
      System.out.println("you must be logged in to use this");
      return CompletableFuture.supplyAsync(() -> StatusCode.Forbidden);
    }

    String url = String.format("%s/map/%s/metadata", BASE_API, mapID);
    return performRequest(url, "GET", true).thenApply((rsp) -> {
      if (rsp.code != StatusCode.OK) {
        System.err.printf("Failed to get map metadata, got code %s\n", rsp.code.toString());
        return rsp.code;
      }

      MapMetadata metadata;
      try {
        JsonReader reader = new JsonReader();
        JsonValue val = reader.parse(rsp.body);
        val = val.get("metadata");
        Json json = new Json();
        metadata = json.readValue(MapMetadata.class, val);
      } catch (Exception e) {
        e.printStackTrace();
        return StatusCode.InternalException;
      }

      // write the metadata to the map folder
      try {
        File metaDataFile = new File(destination + "/metadata.json");
        FileOutputStream fout = new FileOutputStream(metaDataFile);
        Json j = new Json();
        j.setOutputType(JsonWriter.OutputType.json);
        fout.write(j.toJson(metadata).getBytes());
        fout.close();
      } catch (Exception e) {
        e.printStackTrace();
        return StatusCode.InternalException;
      }

      return rsp.code;
    });
  }

  /**
   * Networking API call to download a maps thumbnail.
   *
   * Preconditions:
   * - user must be logged in
   *
   * Requires:
   * - id of the map to query
   *
   * Returns {@code StatusCode} for success/fail handling:
   * - {@code StatusCode.OK}: success
   * - anything else: error
   */
  public static CompletableFuture<StatusCode> downloadMapThumbnail(String mapID, String destination) {
    if (!User.isLoggedIn()) {
      System.out.println("you must be logged in to use this");
      return CompletableFuture.supplyAsync(() -> StatusCode.Forbidden);
    }

    String url = String.format("%s/map/%s/thumbnail", BASE_API, mapID);
    return performRequest(url, "GET", true).thenApply((rsp) -> {
      if (rsp.code != StatusCode.OK) {
        System.err.printf("Failed to get map thumbnail, got code %s\n", rsp.code.toString());
        return rsp.code;
      }

      File dl = new File(destination + "/thumbnail.png");
      try {
        if (!dl.createNewFile()) {
          System.err.printf("Failed to create thumbnail for map %s\n", mapID);
          return StatusCode.InternalException;
        }

        Files.copy(rsp.body, dl.toPath(), StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException e) {
        e.printStackTrace();
        return StatusCode.InternalException;
      }

      return rsp.code;
    });
  }

  /**
   * Networking API call to post a player record
   *
   * Preconditions:
   * - user must be logged in
   *
   * Returns {@code StatusCode} for success/fail handling:
   * - {@code StatusCode.OK}: success
   * - anything else: error
   */
  public static CompletableFuture<StatusCode> updatePlayerRecord(PlayerRecord record) {
    if (!User.isLoggedIn()) {
      return CompletableFuture.supplyAsync(() -> StatusCode.Forbidden);
    }

    return postJson(String.format("%s/leaderboard/user", BASE_API), record.toString(), true).thenApply((rsp) -> {

      // response checking
      if (rsp.code != StatusCode.OK) {
        System.err.println("Got non-200 status code from posting player record");
        JsonReader reader = new JsonReader();
        JsonValue val = reader.parse(rsp.body);
        String msg = val.getString("msg");
        System.out.println("msg:" + msg);
        return StatusCode.ServerException;
      } else if (!rsp.hasBody) {
        System.err.println("Got no response body from updating player record");
        return StatusCode.ServerException;
      }
      return rsp.code;
    });
  }

  /**
   * Networking API call to post a player record
   *
   * Preconditions:
   * - user must be logged in
   *
   * Returns an {@code PlayerRecordResponse} which contains a success code and
   * potential {@link PlayerRecord}. If the {@code PlayerRecordResponse.code} is
   * {@code StatusCode.OK}, then the records exists and is valid. Otherwise,
   * the fetch failed and there is no record provided.
   */
  public static CompletableFuture<PlayerRecordResponse> getPlayerRecord(String username) {
    if (!User.isLoggedIn()) {
      return CompletableFuture.supplyAsync(() -> new PlayerRecordResponse(StatusCode.Forbidden));
    }
    String url = String.format("%s/leaderboard/user/%s", BASE_API, username);
    return performRequest(url, "GET", true).thenApply((rsp) -> {
      // response checking
      if (rsp.code != StatusCode.OK) {
        System.err.println("Got non-200 status code from getting player record: " + rsp.code.name());
        return new PlayerRecordResponse(rsp.code);
      } else if (!rsp.hasBody) {
        System.err.println("Got no response body from uploading map");
        return new PlayerRecordResponse(StatusCode.ServerException);
      }

      try {
        JsonReader reader = new JsonReader();
        JsonValue val = reader.parse(rsp.body);
        val = val.get("record");

        Json json = new Json();
        PlayerRecord r = json.readValue(PlayerRecord.class, val);
        return new PlayerRecordResponse(rsp.code, r);

      } catch (Exception e) {
        e.printStackTrace();
        return new PlayerRecordResponse(StatusCode.ServerException);
      }

    });
  }
}
