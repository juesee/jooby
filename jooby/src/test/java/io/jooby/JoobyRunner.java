package io.jooby;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class JoobyRunner {

  private final Supplier<App> provider;

  private final List<ExecutionMode> modes = new ArrayList<>();

  public JoobyRunner(Consumer<App> configurer) {
    this(() -> {
      App app = new App();
      configurer.accept(app);
      return app;
    });
  }

  public JoobyRunner(Supplier<App> provider) {
    this.provider = provider;
  }

  public JoobyRunner mode(ExecutionMode... mode) {
    modes.addAll(Arrays.asList(mode));
    return this;
  }

  public void ready(Consumer<WebClient> onReady, Supplier<Server>... servers) {
    if (modes.size() == 0) {
      modes.add(ExecutionMode.WORKER);
    }
    List<Supplier<Server>> serverList = new ArrayList<>();
    if (servers.length == 0) {
      ServiceLoader.load(Server.class).stream()
          .forEach(provider -> serverList.add(() -> provider.get()));
    } else {
      serverList.addAll(Arrays.asList(servers));
    }
    for (ExecutionMode mode : modes) {
      for (Supplier<Server> serverFactory : serverList) {
        Server server = serverFactory.get();
        try {
          App app = this.provider.get().mode(mode);
          server.port(9999)
              .deploy(app)
              .start();

          onReady.accept(new WebClient(9999));
        } finally {
          server.stop();
        }
      }
    }
  }
}
