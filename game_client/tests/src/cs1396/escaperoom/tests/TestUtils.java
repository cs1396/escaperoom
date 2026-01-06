package cs1396.escaperoom.tests;

import cs1396.escaperoom.engine.types.Result;


import java.lang.AssertionError;
import java.util.function.Function;
import java.util.function.Predicate;

public class TestUtils {
  public static<T, E> void assertOk(Result<T,E> result){
    if (result.isErr()){
      throw new AssertionError(result.unwrapErr().toString());
    }
  }

  public static<T, E> void assertOkAnd(Result<T,E> result, Predicate<T> pred, Function<T, String> errGenerator){
    if (result.isErr()){
      throw new AssertionError(result.unwrapErr().toString());
    } else {
      if (!pred.test(result.unwrap())){
        throw new AssertionError(errGenerator.apply(result.unwrap()));
      }
    }
  }

  public static<T, E> void assertErr(Result<T,E> result){
    if (result.isOk()){
      throw new AssertionError();
    } 
  }

  public static<T, E> void assertErr(Result<T,E> result, Function<T, String> errGenerator){
    if (result.isOk()){
      throw new AssertionError(errGenerator.apply(result.unwrap()));
    } 
  }
}
