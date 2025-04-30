package com.cubeia.bookkeeping.exception;

public class SameAccountTransferException extends RuntimeException {

  public SameAccountTransferException() {
    super("Cannot transfer to the same wallet");
  }


}
