package com.condires.adventure.companion.airtable;

import com.sybit.airtableandroid.Table;
import com.sybit.airtableandroid.exception.AirtableException;

import java.util.List;
/*
 * mit diesem Daten Objekt wird der Airtabel Request an den Airtable Service übermittelt udn das Reusltat
 * wieder empfangen.
 */
public class AirtableCall {
    public static final int SELECT_ALL = 0;
    public static final int SELECT_ID = 1;
    public static final int SELECT_FILTER = 2;
    public static final int CREATE_NEW = 3;
    public static final int UPDATE = 4;
    public static final int DELETE = 5;
    public static final int EXITS = 6;
    public static final int NAMELIST = 7;

    Table table;
    int action;
    String id;
    String[] fields = {"Name"};
    List result;
    Object inpObj;
    Object outObj;
    AirtableException airtableException;

    public AirtableCall(Table table) {
        this.table = table;
    }

    public AirtableCall(Table table, int action) {
        this.table = table;
        this.action = action;
    }

    public AirtableCall(Table table, int action, String id) {
        this.table = table;
        this.action = action;
        this.id = id;
    }
    public AirtableCall(Table table, int action, String id, String[] fields) {
        this.table = table;
        this.action = action;
        this.id = id;   // ist hier Filter
        this.fields = fields;
    }
    public AirtableCall(Table table, int action, Object newObject) {
        this.table = table;
        this.action = action;

        this.inpObj = newObject;
    }


    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public int getAction() {
        return action;
    }

    public Object getInputObject() {
        return inpObj;
    }
    public Object getOutputObject() {
        return outObj;
    }
    public void setAction(int action) {
        this.action = action;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String[] getFields() {
        return fields;
    }

    public void setFields(String[] fields) {
        this.fields = fields;
    }

    public List getResult() {
        return result;
    }

    public void setResult(List result) {
        this.result = result;
    }

    public void setResult(Object result) {
        this.outObj = result;
    }

    public AirtableException getAirtableException() {
        return airtableException;
    }

    public void setAirtableException(AirtableException airtableException) {
        this.airtableException = airtableException;
    }

    @Override
    public String toString() {
        return "AirtableCall{" +
                "table=" + table +
                ", action=" + action +
                ", inpObj='" + inpObj + '\'' +
                ", outObj='" + outObj + '\'' +
                ", id='" + id + '\'' +
                ", result=" + result +
                '}';
    }
}

