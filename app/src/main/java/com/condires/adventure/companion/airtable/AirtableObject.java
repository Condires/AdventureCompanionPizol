package com.condires.adventure.companion.airtable;

import java.util.Date;
/*
 * stellt sicher, dass alle Klassen, die ein Mapping mit Airtable haben, über die benötigten
 * Felder und die getTableName funktion verfügen
 */
public interface AirtableObject {
    public String getTableName();

    public void setId(String id);
    public String getId();

    public Date getCreatedTime();
    public void setCreatedTime(Date createdTime);
}
