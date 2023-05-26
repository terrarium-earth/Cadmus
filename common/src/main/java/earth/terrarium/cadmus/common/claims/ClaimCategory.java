package earth.terrarium.cadmus.common.claims;

public enum ClaimCategory {
    PLAYER('p'),
    TEAM('t'),
    ADMIN('a');

    private final char prefix;

    ClaimCategory(char code) {
        this.prefix = code;
    }

    public char getPrefix() {
        return prefix;
    }
}
