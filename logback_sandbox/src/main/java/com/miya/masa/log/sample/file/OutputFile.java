package com.miya.masa.log.sample.file;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OutputFile {
  public void print() {
    log.info("info message");
  }

  public void printTrace() {
    log.trace("trace message");
  }
}
