package org.cchmc.kluesuite.klue;

import org.cchmc.kluesuite.TimeTotals;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static java.lang.System.exit;

/**
 * Created by osboxes on 24/04/17.
 *
 * This cannot open a regular KidDatabase file
 */
public class KidDatabaseNoDnaBitString extends KidDatabaseMemory implements java.io.Serializable {

    public KidDatabaseNoDnaBitString(){
        super();
    }

    public KidDatabaseNoDnaBitString(KidDatabaseMemory kidDatabase){
        fileName = kidDatabase.fileName+".noDBS";
        last = kidDatabase.last;
        entries = kidDatabase.entries;
        nameIndex = kidDatabase.nameIndex;
        kingdoms = kidDatabase.kingdoms;
        sequences = new ArrayList<>();
//        sequences = null;
    }



    /**
     * When importFNA is called, does not save DNAbitString
     */
    public void importFNA(String filename) throws FileNotFoundException {
        int currentKID = -1;
        SuperString currentSeq = new SuperString();
        //String currentName = "";
        boolean ignore = true; //do not write empty sequence to database

        //skipping is holdover from copying code.  Here, it does nothing.
        boolean skipping = false;
//        boolean debug = false;

        TimeTotals tt = new TimeTotals();
        tt.start();

        System.out.println("\nFNA import begins " + tt.toHMS() + "\n");
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {

            for (String line; (line = br.readLine()) != null; ) {

                if (debug) {
                    System.err.println("Single line:: " + line);
                }

                // if blank line, it does not count as new sequence
                if (line.trim().length() == 0) {
                    if (debug) {
                        System.err.println("           :: blank line detected  ");
                    }
                    if (!skipping) {
                        if (!ignore) {
//                            storeSequence(currentKID, currentSeq, tt);
                        }
                    }
                    ignore = true;

                    // if line starts with ">", then it is start of a new reference sequence
                } else if (line.charAt(0) == '>') {
                    if (debug) {
                        System.err.println("           :: new entry detected  " + line);
                    }
                    // save previous iteration to database

                    if (!skipping) {
                        if (!ignore) {
//                            storeSequence(currentKID, currentSeq, tt);
                        }

                        // initialize next iteration

                        if (indexOf(line.trim()) == -1) {
                            //original.addWithTrim(new Kid(line.trim()));
                            //addNewKidEntry(line);
                            add(new Kid(line.trim()));
                        }

//                        currentKID = getKid(line.trim()); // original.indexOf(line.trim());
//                        if (currentKID == -1) {
//                            System.err.println("This sequence not found in database : " + line);
//                            listEntries(0);
//                            exit(0);
//                        }
                        //currentSeq = "";

//                        currentSeq = new SuperString();

                        ignore = false;
                    }
                } else {
                    if (!skipping) {
                        //currentSeq += line.trim();
//                        currentSeq.addWithTrim(line.trim());
                    }
                }

            } //end for

            br.close();

            if (!ignore) {
//                storeSequence(currentKID, currentSeq, tt);
            }
        } catch (FileNotFoundException e) {

        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    //cannot override?
//    @Override
//    private void writeObject(java.io.ObjectOutputStream stream) throws IOException {
//        stream.writeObject(fileName);
//        stream.writeObject(last);
//        stream.writeObject(entries);
//        stream.writeObject(nameIndex);
//        //stream.writeObject(sequences);
//        stream.writeObject(kingdoms);
//    }
//
//    @Override
//    private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
//        fileName = (String) stream.readObject();
//        last = (Integer) stream.readObject();
//        entries = (ArrayList<Kid>) stream.readObject();
//        nameIndex = (ArrayList<String>) stream.readObject();
//        //sequences = (ArrayList<DnaBitString>) stream.readObject();
//        kingdoms = (HashMap<Integer, String>) stream.readObject();
//    }




}
