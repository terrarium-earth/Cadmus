package earth.terrarium.cadmus.api.claims;

import earth.terrarium.cadmus.api.teams.TeamId;

public record ClaimData(TeamId team, boolean isChunkLoaded) {}
