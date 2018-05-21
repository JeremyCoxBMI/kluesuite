package org.cchmc.kluesuite.masterklue;

/**
 * Created by osboxes on 5/24/17.
 */
public class SettingsFileEntry {

    public int ord;
    public String hn;
    public Object var;
    public VarType vt;

    public SettingsFileEntry(int ordinal, String humanName, Object variable, VarType type){
        ord = ordinal;
        hn = humanName;
        var = variable;
        vt = type;
    }


}
