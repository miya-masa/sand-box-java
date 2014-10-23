package com.miya.masa.log.sample.loglevel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogLevelCheck {

  public void info() {
    log.info("info message");
  }

  public void trace() {
    log.trace("trace message");
  }
}
