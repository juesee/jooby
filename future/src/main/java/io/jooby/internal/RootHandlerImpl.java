package io.jooby.internal;

import io.jooby.Context;
import io.jooby.ErrorHandler;
import io.jooby.Handler;
import io.jooby.RootHandler;
import io.jooby.StatusCode;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.util.function.Function;

public class RootHandlerImpl implements RootHandler {
  private final Handler next;
  private final ErrorHandler err;
  private final Function<Throwable, StatusCode> statusCode;
  private final Logger log;

  public RootHandlerImpl(Handler next, ErrorHandler err, Logger log, Function<Throwable, StatusCode> statusCode) {
    this.next = next;
    this.log = log;
    this.err = err;
    this.statusCode = statusCode;
  }

  @Override public void apply(@Nonnull Context ctx) {
    try {
      next.apply(ctx);
    } catch (Throwable x) {
      if (!ctx.isResponseStarted()) {
        err.apply(ctx, x, statusCode.apply(x));
      } else {
        log.error("execution of {} {} resulted in exception", ctx.method(), ctx.path(), x);
      }
    }
  }
}
