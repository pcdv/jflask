package net.jflask.test;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;
import net.jflask.ErrorHandler;
import net.jflask.Request;
import net.jflask.Route;
import net.jflask.SunRequest;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author pcdv
 */
public class ErrorHandlerTest extends AbstractAppTest {

  @Route("/")
  public String foo() {
    throw new IllegalStateException("fail");
  }

  /**
   * Check that a error handler can customize the response sent to the client.
   */
  @Test
  public void testHandlerSendsContent() throws Exception {
    app.addErrorHandler(new ErrorHandler() {
      @Override
      public void onError(int status, Request request, Throwable t) {
        // this is a hack (future version should allow to do it in a clean way)
        HttpExchange ex = ((SunRequest) request).getExchange();
        try {
          ex.sendResponseHeaders(500, 0);
          ex.getResponseBody().write("hello".getBytes());
          ex.close();
        }
        catch (IOException ignored) {
        }
      }
    });

    try {
      client.get("/");
    }
    catch (IOException e) {
      Assert.assertEquals("hello", e.getMessage());
    }
  }
}
