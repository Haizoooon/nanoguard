package de.evoxy.nanoguard.database;

import de.evoxy.flux.annotations.Entity;
import de.evoxy.flux.annotations.Index;

@Entity(tableName = "whitelisted_addresses")
public class WhitelistedTable {

    @Index
    public String type;

    @Index
    public String value;

}
