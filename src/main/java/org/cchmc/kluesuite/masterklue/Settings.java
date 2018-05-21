package org.cchmc.kluesuite.masterklue;

/**
 * Created by osboxes on 5/25/17.
 *
 * DEPRECATED
 */
public abstract class Settings {

    /**
     * helper class for File I/O
     */
    protected static SettingsFile set;

    /**
     * Private class so can't be instantiated
     */
    protected Settings(){}

    public static void loadSettings(){

        try {
            set.loadSettings(set.filename);
        } catch (Exception e) {
            e.printStackTrace();
        }
        set.processVariableList();
    }

    public static void loadSettings(String filename){

        try {
            set.loadSettings(filename);
        } catch (Exception e) {
            e.printStackTrace();
        }
        set.processVariableList();
        if (set.getValue("filename") != filename){
            System.err.println("WARNING : Settings_OLD filename setting does not match the path loaded");
        }
    }

    public static void saveSettings(){
        try {
            set.saveSettings(set.filename);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveSettings(String filename){
        set.setVariable("filename", filename);

        try {
            set.saveSettings(filename);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getFileName(){
        return set.filename;
    }

    public static void setFileName(String file){
        set.filename = file;
    }

    public static void initialize(){}


}
