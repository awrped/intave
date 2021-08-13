package de.jpx3.intave.module;

import de.jpx3.intave.module.module.ExampleModule;

public final class Modules {
  private final ModulePool pool = new ModulePool();
  private final ModuleLoader loader = new ModuleLoader();

  public void prepareModules() {
    loader.setup();
  }

  public void proceedBoot(BootSegment bootSegment) {
    loader.loadRequests().forEach(pool::loadModule);
    pool.bootRequests(bootSegment).forEach(pool::enableModule);
  }

  public void shutdown() {
    pool.disableAll();
    pool.unloadAll();
  }

  // quick accessors

  public ExampleModule exampleModule() {
    return find(ExampleModule.class);
  }

  public <T extends Module> T find(Class<? extends Module> moduleClass) {
    return pool.lookup(moduleClass);
  }
}
