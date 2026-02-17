package de.evoxy.nanoguard.database;

import de.evoxy.flux.annotations.Entity;
import de.evoxy.flux.annotations.Index;

@Entity(tableName = "blocked_addresses")
public class BlockedAddressesTable {

    @Index
    public String id;

    @Index
    public String address;

    @Index
    public long timestamp;

    @Index
    public String information;

}
