package net.jflask.test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import net.jflask.ErrorHandler;
import net.jflask.Request;
import net.jflask.Route;
import net.jflask.SuccessHandler;
import org.junit.Assert;
import org.junit.Test;

public class HookTest extends AbstractAppTest {

  @Route("/barf")
  public String barf() {
    throw new RuntimeException("barf");
  }

  @Route("/hello/:name")
  public String getOk(String name) {
    return "Hello " + name;
  }

  /**
   * Check that 404 and other errors can be handled by ErrorHandlers.
   */
  @Test
  public void testErrorHook() throws Exception {

    final LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();

    app.addErrorHandler(new ErrorHandler() {
      @Override
      public void onError(int status, Request request, Throwable t) {
        queue.offer(status + " " + request.getRequestURI() + " " + t);
      }
    });

    try {
      client.get("/unknown");
    }
    catch (IOException e) {
    }

    Assert.assertEquals("404 /unknown null", queue.poll(1, TimeUnit.SECONDS));

    try {
      client.get("/barf");
    }
    catch (IOException e) {
    }

    Assert.assertEquals("500 /barf java.lang.RuntimeException: barf",
                        queue.poll(1, TimeUnit.SECONDS));
  }

  @Test
  public void testSuccessHook() throws Exception {
    final LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();

    app.addSuccessHandler(new SuccessHandler() {
      @Override
      public void onSuccess(Request r,
                            Method method,
                            Object[] args,
                            Object result) {
        queue.offer(r.getRequestURI() + " " + method.getName() +
                    Arrays.toString(args) + " " + result);
      }
    });

    client.get("/hello/world");

    Assert.assertEquals("/hello/world getOk[world] Hello world",
                        queue.poll(1, TimeUnit.SECONDS));

  }
}
