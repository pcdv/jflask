package net.jflask;

public interface ResponseConverter<T> {

  void convert(T data, Response resp) throws Exception;
}
