package cs1396.escaperoom.services;

import java.util.Optional;

import com.badlogic.gdx.utils.Array;

import cs1396.escaperoom.engine.assets.maps.MapMetadata;
import cs1396.escaperoom.services.Networking.StatusCode;

public class Types {

  public static class PlayerRecordResponse {
    public StatusCode code;
    public PlayerRecord record;

    public PlayerRecordResponse(StatusCode code) {
      this.code = code;
      this.record = null;
    }

    public PlayerRecordResponse(StatusCode code, PlayerRecord record) {
      this.code = code;
      this.record = record;
    }
  }

  public static class ListPlayerRecordResponse {
    public StatusCode code;
    public Array<PlayerRecord> records;

    public ListPlayerRecordResponse(StatusCode code) {
      this.code = code;
      this.records = null;
    }

    public ListPlayerRecordResponse(StatusCode code, Array<PlayerRecord> metadata) {
      this.code = code;
      this.records = metadata;
    }
  }

  public static class ListMapsResponse {
    public StatusCode code;
    public Array<MapMetadata> metadata;

    public ListMapsResponse(StatusCode code) {
      this.code = code;
      this.metadata = null;
    }

    public ListMapsResponse(StatusCode code, Array<MapMetadata> metadata) {
      this.code = code;
      this.metadata = metadata;
    }

  }

  public static class Filters {
    public enum Combination {
      And,
      Or;

      @Override
      public String toString() {
        switch (this) {
          case And:
            return "and";
          case Or:
            return "or";
        }
        return "null";
      }
    }

    private Array<Filter> filters = new Array<>();
    private Optional<Combination> combination = Optional.empty();

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();

      for (Filter filter : filters) {
        if (sb.length() != 0) {
          sb.append("&");
        }
        sb.append(filter.toString());
      }

      if (filters.size > 1) {
        combination.ifPresent(combo -> {
          if (sb.length() != 0) {
            sb.append("&");
          }
          sb.append("filters_approach=");
          sb.append(combo.toString());
        });
      }

      return sb.toString();
    }

    public Filters(Combination combination, Filter... filters) {
      this.combination = Optional.of(combination);
      this.filters = Array.with(filters);
    }

    public Filters(Filter... filters) {
      this.combination = Optional.empty();
      this.filters = Array.with(filters);
    }

  }

  public static class Filter {
    public static enum Method {
      LessThanEqualTo,
      LessThan,
      Equals,
      NotEquals,
      GreaterThan,
      GreaterThanEqualTo;

      @Override
      public String toString() {
        switch (this) {
          case Equals:
            return "eq";
          case GreaterThan:
            return "gt";
          case GreaterThanEqualTo:
            return "gte";
          case LessThan:
            return "lt";
          case LessThanEqualTo:
            return "lte";
          case NotEquals:
            return "ne";
        }
        return "null";
      }
    }

    private String field;
    private Method method;
    private Optional<String> sconst;
    private Optional<Integer> iconst;

    @Override
    public String toString() {
      if (sconst.isPresent()) {
        return String.format("filter_field=%s&filter_method=%s&filter_value=%s&filter_type=str", field,
            method.toString(), sconst.get());
      } else {
        return String.format("filter_field=%s&filter_method=%s&filter_value=%s&filter_type=int", field,
            method.toString(), iconst.get());
      }
    }

    public Method getMethod() {
      return this.method;
    }

    public String getField() {
      return this.field;
    }

    public Filter(String field, Method method, String value) {
      this.field = field;
      this.method = method;
      this.sconst = Optional.of(value);
      this.iconst = Optional.empty();
    }

    public Filter(String field, Method method, Integer value) {
      this.field = field;
      this.method = method;
      this.sconst = Optional.empty();
      this.iconst = Optional.of(value);
    }
  }

  public static class Sort {
    public static enum Direction {
      Ascending,
      Descending;

      @Override
      public String toString() {
        switch (this) {
          case Ascending:
            return "asc";
          case Descending:
            return "desc";
        }
        return "null";
      }
    }

    private String field;
    private Optional<Direction> direction;

    @Override
    public String toString() {
      if (direction.isPresent()) {
        return String.format("sort_field=%s&sort_dir=%s", field, direction.get().toString());
      } else {
        return String.format("sort_field=%s", field);
      }
    }

    public Sort(String field) {
      this.field = field;
      this.direction = Optional.empty();
    }

    public Sort(String field, Direction direction) {
      this.field = field;
      this.direction = Optional.of(direction);
    }
  }

  public abstract static class ListableRequest {
    protected Optional<Filters> filters = Optional.empty();
    protected Optional<Sort> sort = Optional.empty();
    protected Optional<Integer> limit = Optional.empty();

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();

      filters.ifPresent(f -> {
        sb.append(f.toString());
      });
      sort.ifPresent(s -> {
        if (sb.length() != 0) {
          sb.append("&");
        }
        sb.append(s.toString());
      });
      limit.ifPresent(l -> {
        if (sb.length() != 0) {
          sb.append("&");
        }
        sb.append("limit=");
        sb.append(l);
      });

      return sb.toString();
    }

    public ListableRequest withSort(Sort s) {
      this.sort = Optional.of(s);
      return this;
    }

    public ListableRequest withFilters(Filter... filters) {
      this.filters = Optional.of(new Filters(filters));
      return this;
    }

    public ListableRequest withFilters(Filters filters) {
      this.filters = Optional.of(filters);
      return this;
    }

    public ListableRequest withLimit(int limit) {
      this.limit = Optional.of(limit);
      return this;
    }
  }

  public interface IsField {
    public String AsJsonKey();

    public FilterConstType getConstType();

    public String displayName();

    public IsField[] getValues();
    public IsField defaultSort();
  }

  public enum FilterConstType {
    String,
    Integer,
  }

  public static class ListPlayerRecordRequest extends ListableRequest {
    public enum Field implements IsField {
      Username(FilterConstType.String),
      Attempts(FilterConstType.Integer),
      Clears(FilterConstType.Integer),
      UniqueClears(FilterConstType.Integer);

      private FilterConstType constType;

      @Override
      public String toString() {
        switch (this) {
          case Attempts:
            return "attempts";
          case Username:
            return "username";
          case Clears:
            return "clears";
          case UniqueClears:
            return "uniqueClears";
        }
        return "";
      }

      private Field(FilterConstType constType) {
        this.constType = constType;
      }

      @Override
      public FilterConstType getConstType() {
        return this.constType;
      }

      @Override
      public String AsJsonKey() {
        return this.toString();
      }

      @Override
      public String displayName() {
        return this.name();
      }

      @Override
      public IsField[] getValues() {
        return values();
      }

	  @Override
	public IsField defaultSort() {
        return UniqueClears;
	}
    }
  }

  public static class ListMapsRequest extends ListableRequest {
    public enum Field implements IsField {
      Username(FilterConstType.String),
      Downloads(FilterConstType.Integer),
      Attempts(FilterConstType.Integer),
      UpVotes(FilterConstType.Integer),
      DownVotes(FilterConstType.Integer),
      FastestTime(FilterConstType.String),
      RecordHolder(FilterConstType.String);

      private FilterConstType constType;

      @Override
      public String toString() {
        switch (this) {
          case Attempts:
            return "stats.attempts";
          case DownVotes:
            return "stats.downvotes";
          case Downloads:
            return "stats.downloads";
          case FastestTime:
            return "stats.record.fastestms";
          case Username:
            return "stats.creator";
          case RecordHolder:
            return "stats.record.username";
          case UpVotes:
            return "stats.upvotes";
        }
        return "";
      }

      private Field(FilterConstType constType) {
        this.constType = constType;
      }

      public FilterConstType getConstType() {
        return this.constType;
      }

      @Override
      public String AsJsonKey() {
        return this.toString();
      }

      @Override
      public String displayName() {
        return this.name();
      }

      @Override
      public IsField[] getValues() {
        return values();
      }

	  @Override
	public IsField defaultSort() {
        return Username;
	}
    }
  }
  

}
