package org.cchmc.kluesuite.masterklue;



import org.apache.commons.io.monitor.FileEntry;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by osboxes on 5/24/17.
 *
 *
 * Settings_OLD defines data layout for the mapping data structure.
 *
 * Static Settings_OLD Classes contain this struct-like class
 *
 * SettingsFile class is passed the Settings_OLD instance to write to a file
 *
 */

/**
 * YESNO = string with word :: standardizes input/output to "yes" or "no"
 */

enum VarType {STRING, INTEGER, COMMENT, KIDDB, KLUEDB, YESNO, DOUBLE};
enum KidDatabaseType {MEMORY, ROCKS};
enum KlueType {LOCAL, LOCAL_16_PARTS, SERVER, SERVER_16_PARTS};

public class SettingsFile {

    String filename;
    String settingsName;


//    private static HashMap<Integer,String> commentMap;
//    private static HashMap<String,Object> human2variable;
//    private static HashMap<String,VarType> human2type;
//    private static ArrayList<SettingsFileEntry> variableList;


    public SettingsFile(String fileName, String settingsName){
        this.filename = fileName;
        this.settingsName = settingsName;

        commentMap = new HashMap<Integer,String>();
        human2variable = new HashMap<String,Object>();
        human2type = new HashMap<String,VarType>();
        variableList = new ArrayList<SettingsFileEntry>();


        variableList.add(new SettingsFileEntry(5,"# **Please use absolute paths to prevent errors when file is moved**\n",null,VarType.COMMENT));
        variableList.add(new SettingsFileEntry(10,"# Path to this settings File.  Settings_OLD group: "+settingsName,null,VarType.COMMENT));
        variableList.add(new SettingsFileEntry(20, "filename", filename, VarType.STRING ));

        processVariableList();
    }

    public void add(SettingsFileEntry sfe){
        variableList.add(sfe);
    }

    /**
     * Required for initialization and updates AFTER addWithTrim()
     */
    public void processVariableList(){
        for (SettingsFileEntry fe : variableList){
            commentMap.put(fe.ord,fe.hn);
            human2variable.put(fe.hn, fe.var);
            human2type.put(fe.hn,fe.vt);
        }
    }

    /**
     * Contains all information tied together
     * Numeric order of entries, human readable string, variable address, variable type
     */
    protected ArrayList<SettingsFileEntry> variableList;


    /**
     * integer order determines order written to file
     * maps to either
     *    comment (starts with '#')
     * OR
     *    contains a "human" string for a variable
     *
     */
    protected HashMap<Integer,String> commentMap;

    /**
     * maps human readable string to corresponding object
     */
    protected HashMap<String,Object> human2variable;


    /**
     * maps human readable string to corresponding object data type
     */
    protected HashMap<String,VarType> human2type;



    public Object getValue(String humanName){
        return human2variable.get(humanName);
    }


    public VarType getType(String humanName){
        return human2type.get(humanName);
    }


    public void setVariable(String humanName, String value){
        VarType vt = human2type.get(humanName);
        switch (vt) {
            case INTEGER:
                human2variable.put(humanName, Integer.parseInt(value));
                break;
            case STRING:
                human2variable.put(humanName, value);
                break;
            case COMMENT:
                human2variable.put(humanName, null);
                break;
            case KIDDB:
                human2variable.put(humanName, KidDatabaseType.valueOf(value));
                break;
            case KLUEDB:
                human2variable.put(humanName, KlueType.valueOf(value));
                break;
            case YESNO:
                if (value.equals("yes")||value.equals("y")||value.equals("Yes")||value.equals("Y")||value.equals("YES") ) {
                    human2variable.put(humanName, "yes");
                }
                else {
                    human2variable.put(humanName, "no");
                }
                break;
            default:
                break;  //IGNORE comments
        }
    }

    /**
     *
     * @param file
     * @return
     */
    public boolean loadSettings(String file) throws Exception {

        boolean result = true;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {

            int k = 1;
            for (String line; (line = br.readLine()) != null; ) {
                if (line.length() > 1 && line.charAt(0) != '#') {  //ignore comments
                    String[] parsed = line.trim().split("=");
                    if (parsed.length == 2 || parsed.length == 1) {
                        if (human2variable.get(parsed[0]) != null) {
                            if(parsed.length == 1)
                                setVariable(parsed[0], "");
                            else
                                setVariable(parsed[0], parsed[1]);
                        } else {
                            System.err.println("Settings_OLD.loadSettings ("+settingsName+"):: unexpected variable name being ignored");
                            System.err.println("\t" + parsed[0]);
                        }
                    } else {
                        System.err.println("Settings_OLD.loadSettings ("+settingsName+"):: unexpected line format");
                        System.err.println("\toffending line is:");
                        System.err.println("\t" + line.trim());
                        throw new Exception("Settings_OLD load error");
                    }
                }  //end if != '#'

            } // end for
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            result = false;
            throw new Exception("FileNotFoundException\t"+e);
        } catch (IOException e) {
            result = false;
            e.printStackTrace();
            throw new Exception("IOException\t"+e);
        }

        return result;
    }


    public void saveSettings(String fileName) throws Exception {
        OutputStream output = null;

        FileWriter fw = null;
        BufferedWriter writer = null;

        boolean inComment = false;

        try {

            fw = new FileWriter(fileName);
            writer = new BufferedWriter(fw);
            ArrayList<Integer> keys = new ArrayList<Integer>(commentMap.keySet());

            Collections.sort(keys);


            for (int k = 0; k< keys.size(); k++){
                int key = keys.get(k);
                String outStr =  commentMap.get(key);

                if (outStr.charAt(0) == '#'){
                    if (!inComment ) writer.write("\n");
                    writer.write(outStr+"\n");
                    inComment = true;
                } else {
                    inComment = false;
                    writer.write("\t"+outStr + "=");
                    switch (getType(outStr)) {
                        case INTEGER:
                            writer.write(Integer.toString((int) human2variable.get(outStr)) + "\n");
                            break;
                        case STRING:
//                            writer.write(human2variable.get(outStr) + "\n");
//                            break;
                        case KIDDB:
                        case KLUEDB:
                        case YESNO:
                            writer.write(human2variable.get(outStr) + "\n");
                            break;
                        default:
                            break;
                    }
                }
            }

        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
            throw new Exception("IOException\t"+x);
        } finally {

            try {

                if (writer != null)
                    writer.close();

                if (fw != null)
                    fw.close();

            } catch (IOException ex) {

                ex.printStackTrace();

            }

        }

    }




//    /**
//     * Contains all information tied together
//     * Numeric order of entries, human readable string, variable address, variable type
//     */
//    public ArrayList<SettingsFileEntry> getVariableList(){
//        return variableList;
//    }
//
//
//    /**
//     * integer order determines order written to file
//     * maps to either
//     *    comment (starts with '#')
//     * OR
//     *    contains a "human" string for a variable
//     *
//     */
//    public HashMap<Integer,String> getCommentMap(){
//        return commentMap;
//    }
//
//    /**
//     * maps human readable string to corresponding object
//     */
//    public HashMap<String,Object> getHuman2Variable(){
//        return human2variable;
//    }
//
//
//    /**
//     * maps human readable string to corresponding object data type
//     */
//    public HashMap<String,VarType> getHuman2Type(){
//        return human2type;
//    }




}
