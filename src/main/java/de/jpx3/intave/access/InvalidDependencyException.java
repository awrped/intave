package de.jpx3.intave.access;

public final class InvalidDependencyException extends IntaveException {
  public InvalidDependencyException() {
    super();
  }

  public InvalidDependencyException(String message) {
    super(message);
  }

  public InvalidDependencyException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidDependencyException(Throwable cause) {
    super(cause);
  }

  protected InvalidDependencyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
