package de.evoxy.nanoguard.database;

import de.evoxy.flux.annotations.Entity;
import de.evoxy.flux.annotations.Index;

@Entity(tableName = "caught_players")
public class CaughtPlayersTable {

    @Index
    public String bound_address_id;

    @Index
    public String playerUniqueId;

    @Index
    public long timestamp;

}
