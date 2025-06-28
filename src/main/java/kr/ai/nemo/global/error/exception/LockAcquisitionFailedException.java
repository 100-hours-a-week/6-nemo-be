package kr.ai.nemo.global.error.exception;

public class LockAcquisitionFailedException extends RuntimeException {
  public LockAcquisitionFailedException(String message) {
    super(message);
  }

  public LockAcquisitionFailedException(String message, Throwable cause) {
    super(message, cause);
  }
}
