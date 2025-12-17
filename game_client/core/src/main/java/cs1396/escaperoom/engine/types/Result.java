package cs1396.escaperoom.engine.types;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public sealed interface Result<T, E> permits cs1396.escaperoom.engine.types.Result.Ok, cs1396.escaperoom.engine.types.Result.Err, cs1396.escaperoom.engine.types.Result.IOErr {

  public record Err<T, E>(E val) implements Result<T, E> {};
  public record Ok<T, E>(T val) implements Result<T, E> {
    public final static Ok<Void, ?> Unit = new Ok<>(null);
    public static <E> Ok<Void,E> unit(){
      return new Ok<>(null);
    }
  };
  public record IOErr<T>(String reason) implements Result<T, String> {
    public static <T> IOErr<T> withLog(String reason, Logger log) {
      log.severe(reason);
      return new IOErr<>(reason);
    }
  };

  default boolean isOk() {
    return this instanceof Ok<?, ?>;
  }

  default boolean isErr() {
    return this instanceof Err<?, ?>;
  }

  default T unwrap() {
    if (this instanceof Ok<T, E> ok) {
      return ok.val();
    }
    throw new IllegalStateException("called unwrap() on Err");
  }

  default E unwrapErr() {
    if (this instanceof Err<T, E> err) {
      return err.val();
    }
    throw new IllegalStateException("called unwrapErr() on Ok");
  }

  default T unwrapOr(T defaultValue) {
    return this instanceof Ok<T, E> ok ? ok.val() : defaultValue;
  }

  default <U> Result<U, E> map(Function<T, U> f) {
    if (this instanceof Ok<T, E> ok) {
      return new Ok<>(f.apply(ok.val()));
    }
    // SAFETY: Err contains no T
    return (Err<U, E>) this;
  }

  default <F> Result<T, F> mapErr(Function<E, F> f) {
    if (this instanceof Err<T, E> err) {
      return new Err<>(f.apply(err.val()));
    }
    // SAFETY: OK contains no E
    return (Ok<T, F>) this;
  }

  default <U> Result<U, E> flatMap(Function<T, Result<U, E>> f) {
    if (this instanceof Ok<T, E> ok) {
      return f.apply(ok.val());
    }
    // SAFETY: ERR contains no T
    return (Err<U, E>) this;
  }

  default <R> R match(Function<T, R> okFn, Function<E, R> errFn) {
    if (this instanceof Ok<T, E> ok) {
      return okFn.apply(ok.val());
    }
    // SAFETY: OK contains no E
    return errFn.apply(((Err<T, E>) this).val());
  }

  default Result<T, E> inspect(Consumer<T> f) {
    if (this instanceof Ok<T, E> ok) {
      f.accept(ok.val());
    }
    return this;
  }

  default Result<T, E> inspect_err(Consumer<E> f) {
    if (this instanceof Err<T, E> err) {
      f.accept(err.val());
    }
    return this;
  }

  default <U> Result<U, E> andThen(Function<T, Result<U, E>> f) {
    if (this instanceof Ok<T, E> ok) {
      return f.apply(ok.val());
    }
    // SAFETY: ERR contains no T
    return (Err<U, E>) this;
  }

  default Result<T, E> orElse(Function<E, Result<T, E>> f) {
    if (this instanceof Err<T, E> err) {
      return f.apply(err.val());
    }
    return this;
  }

  default <U> CompletableFuture<Result<U, E>> andThenAsync(
      Function<Void, CompletableFuture<Result<U, E>>> f) {
    if (this.isErr()){
      return CompletableFuture.completedFuture((Result<U, E>) this);
    }
    return f.apply(null);
  }


  static <T, E> Result<T, E> okOr(Optional<T> opt, E err) {
    return opt.<Result<T, E>>map(Ok::new).orElseGet(() -> new Err<>(err));
  }

  static <T, E> Result<T, E> okOrElse(Optional<T> opt, Supplier<E> errFn) {
    return opt.<Result<T, E>>map(Ok::new).orElseGet(() -> new Err<>(errFn.get()));
  }

  default java.util.Optional<T> ok() {
    if (this instanceof Ok<T, E> ok) {
        return Optional.of(ok.val());
    }
    return Optional.empty();
}

}

