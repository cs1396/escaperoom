package cs1396.escaperoom.tests;

import java.lang.reflect.Method;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

import com.badlogic.gdx.Gdx;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.backends.headless.HeadlessFiles;

// Based on https://github.com/TomGrill/gdx-testing
// but updated for JUnit 5 (Extension versus Runner)
public class GdxTestExtension 
    implements BeforeAllCallback, AfterAllCallback, InvocationInterceptor, ApplicationListener {

  private static HeadlessApplication app;
  private static final Object lock = new Object();
  private static Runnable pending;
  private static Throwable testFailure;

  @Override
  public void beforeAll(ExtensionContext context) {
    Gdx.files = new HeadlessFiles();
    Gdx.gl20 = new GL20Mock();
    Gdx.gl = Gdx.gl20;

    if (app == null) {
      HeadlessApplicationConfiguration conf = new HeadlessApplicationConfiguration();
      app = new HeadlessApplication(this, conf);
    }
  }

  @Override
  public void interceptTestMethod(
      Invocation<Void> invocation,
      ReflectiveInvocationContext<Method> invocationContext,
      ExtensionContext extensionContext) throws Throwable {

    synchronized (lock) {
      pending = () -> {
        try {
          invocation.proceed();
        } catch (Throwable e) {
          testFailure = e;
        }
      };
    }

    waitForRender();

    synchronized (lock){
      if (testFailure != null) {
        Throwable t = testFailure;
        testFailure = null;
        throw t;
      }
    }
  }

  private void waitForRender() throws InterruptedException {
    long start = System.currentTimeMillis();
    while (true) {
      synchronized (lock) {
        if (pending == null)
          return;
      }
      if (System.currentTimeMillis() - start > 5000){
        throw new RuntimeException("Timed out waiting for render()");
      }
      Thread.sleep(10);
    }
  }

  @Override
  public void render() {
    synchronized (lock) {
      if (pending != null) {
        pending.run();
        pending = null;
      }
    }
  }

  @Override
  public void afterAll(ExtensionContext context) {
    if (app != null) {
      app.exit();
      app = null;
    }
  }

  @Override
  public void create() {}

  @Override
  public void resize(int width, int height) {}

  @Override
  public void pause() {}

  @Override
  public void resume() {}

  @Override
  public void dispose() {}
}
