package net.jflask;

/**
 * Experimental. Must be returned by URL handlers to indicate that no further
 * processing must be performed with current request (i.e. eventual headers and
 * response data have already been sent and response code has been set).
 *
 * @author pcdv
 */
public interface CustomResponse {
  CustomResponse INSTANCE = new CustomResponse() {};
}
