package de.jpx3.intave.fakeplayer;

import static de.jpx3.intave.fakeplayer.PlayerNameHelper.randomString;

public final class PlayerNameConfigAccess {
  private final static int MAXIMUM_CHARACTERS = 16;
  private final static String MISLEADING_PREFIX_FORMAT = "Prefix of section %s is null";
  private final static String MISLEADING_TABLIST_FORMAT = "Tablist prefix of section %s is null";
  private final static String INVALID_NAME_FORMAT = "Prefix of section %s is too long (%s > " + MAXIMUM_CHARACTERS + ")";
  private final static String INVALID_PREFIX_FORMAT = "Tablist prefix of section %s is too long (%s > " + MAXIMUM_CHARACTERS + ")";
  private final static String PATH = "names.";
  private final static String PREFIX = ".prefix";
  private final static String TABLIST = ".tablist";

  public static PlayerName resolveNewPlayerName() {
    String name = randomString();
    return new PlayerName(name, name, "");
//    FileConfiguration fileConfiguration = FakePlayerPlugin.singletonInstance().getConfig();
//    String section = randomSection();
//    String name = fileConfiguration.getString(PATH + section + PREFIX);
//    String tabList = fileConfiguration.getString(PATH + section + TABLIST);
//    Preconditions.checkArgument(name != null, String.format(MISLEADING_PREFIX_FORMAT, name));
//    Preconditions.checkArgument(tabList != null, String.format(MISLEADING_TABLIST_FORMAT, name));
//    int nameLength = name.length();
//    int tabListLength = tabList.length();
//    Preconditions.checkState(nameLength <= MAXIMUM_CHARACTERS, String.format(INVALID_NAME_FORMAT, section, nameLength));
//    Preconditions.checkState(tabListLength <= MAXIMUM_CHARACTERS, String.format(INVALID_PREFIX_FORMAT, section, tabListLength));
//    return new PlayerName(randomString(), translateColor(name), translateColor(tabList));
  }

//  private static String randomSection() {
//    FileConfiguration fileConfiguration = FakePlayerPlugin.singletonInstance().getConfig();
//    ArrayList<String> configurationSection = Lists.newArrayList(
//      fileConfiguration.getConfigurationSection(PATH).getKeys(false)
//    );
//    int id = ThreadLocalRandom.current().nextInt(0, configurationSection.size());
//    return configurationSection.get(id);
//  }

  public static final class PlayerName {
    private final String name, tabListName, prefix;

    public PlayerName(String name, String tabListName, String prefix) {
      this.name = name;
      this.tabListName = tabListName;
      this.prefix = prefix;
    }

    public String name() {
      return name;
    }

    public String tabListName() {
      return tabListName;
    }

    public String prefix() {
      return this.prefix;
    }
  }
}