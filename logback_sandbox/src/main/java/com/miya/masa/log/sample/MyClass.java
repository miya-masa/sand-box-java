package com.miya.masa.log.sample;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MyClass {

  public void info() {
    log.info("info message");
  }

  public void trace() {
    log.trace("trace message");
  }
}
