package com.miya.masa.log.sample;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.OptionalDouble;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import lombok.extern.slf4j.Slf4j;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;

import com.miya.masa.log.sample.file.OutputFile;
import com.miya.masa.log.sample.loglevel.LogLevelCheck;

@Slf4j
public class MyClassTest {
  private static final Logger LOG_LEVEL = LoggerFactory.getLogger(LogLevelCheck.class);
  private static final Logger LOG_OUTPUTFILE = LoggerFactory.getLogger(OutputFile.class);
  private static final Logger LOGSTDOUT = LoggerFactory.getLogger("stdout");
  private static final Logger LOG_SIZE = LoggerFactory.getLogger("sizebased");
  private static final Logger LOG_TIME_SIZE = LoggerFactory.getLogger("sizeAndTimebased");


  @Before
  public void setup() {
    Paths.get("logs").forEach(e -> deleteFile(e));
  }

  private void deleteFile(Path path) {
    LOGSTDOUT.info(path.toString());
    try {
      if (Files.isDirectory(path)) {
        Files.list(path).forEach(e -> deleteFile(e));
      }
      Files.deleteIfExists(path);
    } catch (IOException e1) {
      log.error(e1.getMessage(), e1);
    }
  }

  @Test
  public void test_logMessage() {
    outputLog(log);
  }

  @Test
  public void test_LoggerContext() throws Exception {
    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
    StatusPrinter.print(lc);
  }

  @Test
  public void test_rollingFile() throws Exception {
    long time = System.currentTimeMillis();
    int i = 0;
    while (System.currentTimeMillis() - time < 10000) {
      log.info("number {}", i++);
    }
  }

  @Test
  public void test_rollingFile_sizebased() throws Exception {
    IntStream.iterate(0, i -> i + 1).limit(100000).forEach(p -> {
      LOGSTDOUT.info("count {}", p);
      LOG_SIZE.info("count {}", p);
    });
  }

  @Test
  public void test_fileAppender() throws Exception {
    outputLog(LOG_OUTPUTFILE);
  }

  @Test
  public void test_timeAndSize() throws Exception {
    IntStream.iterate(0, i -> i + 1).limit(100000).forEach(i -> LOG_TIME_SIZE.info("count {} ", i));
  }

  private void outputLog(Logger logger) {
    logger.trace("trace message");
    logger.debug("debug message");
    logger.info("info message");
    logger.warn("warn message");
    logger.error("error message");
  }

  @Test
  public void test_logLevel() throws Exception {
    ExecutorService ser = Executors.newFixedThreadPool(10);
    CountDownLatch latch = new CountDownLatch(100);
    IntStream.iterate(0, i -> i + 1).limit(100).forEach(i -> {
      ser.execute(() -> {
        int userNum = i % 4;
        MDC.put("u_id", "user" + userNum);
        String reqId = Long.toHexString(System.nanoTime());
        MDC.put("req_id", reqId);
        LOG_OUTPUTFILE.info("message 1 ");
        LOG_OUTPUTFILE.info("message 2 {}", i);
        MDC.remove("u_id");
        MDC.remove("req_id");
        latch.countDown();
      });
    });
    latch.await();
  }

  @Test
  public void test_performance() throws Exception {
    IntStream.iterate(0, i -> i + 1).limit(100000).forEach(i -> LOGSTDOUT.info("hoge " + "fuga"));
    System.out.println(LOG_OUTPUTFILE.getClass().getCanonicalName());
    benchmark(() -> IntStream.iterate(0, i -> i + 1).limit(10000)
        .forEach(i -> LOG_OUTPUTFILE.info("hoge " + "fuga" + "piyo" + "hoge" + "fuga" + "piyo")));

    benchmark(() -> IntStream.iterate(0, i -> i + 1).limit(10000).forEach(i -> {
      if (LOG_OUTPUTFILE.isInfoEnabled()) {
        LOG_OUTPUTFILE.info("hoge " + "fuga" + "piyo" + "hoge" + "fuga" + "piyo");
      }
    }));

    benchmark(() -> IntStream
        .iterate(0, i -> i + 1)
        .limit(10000)
        .forEach(
            i -> LOG_OUTPUTFILE.info(new StringBuilder("hoge ").append("fuga").append("piyo")
                .append("hoge").append("fuga").append("piyo").toString())));

    benchmark(() -> IntStream
        .iterate(0, i -> i + 1)
        .limit(10000)
        .forEach(
            i -> LOG_OUTPUTFILE.info("{} {} {} {} {} {}", "hoge", "fuga", "piyo", "hoge", "fuga",
                "piyo")));


  }

  private void benchmark(Runnable exe) {
    OptionalDouble ave = IntStream.iterate(0, i -> i + 1).limit(200).mapToLong(i -> {
      long start = System.currentTimeMillis();
      exe.run();
      return System.currentTimeMillis() - start;
    }).average();
    System.out.println(ave.getAsDouble());
  }
}
