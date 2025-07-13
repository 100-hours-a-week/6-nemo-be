package kr.ai.nemo.unit.global.error.exception;

public class LockAcquisitionFailedException extends RuntimeException {
  public LockAcquisitionFailedException(String message) {
    super(message);
  }

  public LockAcquisitionFailedException(String message, Throwable cause) {
    super(message, cause);
  }
}
