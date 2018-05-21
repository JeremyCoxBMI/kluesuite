


-- Installation Prerequisites
    RocksDB requires several depencies for efficiency.

    https://github.com/facebook/rocksdb/blob/master/INSTALL.md

    zlib - a library for data compression.
    bzip2 - a library for data compression.
    snappy - a library for fast data compression.
    zstandard - Fast real-time compression algorithm.

    Everything else is included in the kluesuite.jar, or for developers, the Maven Project.

    You need -->Oracle<-- Java 1.8.
    We have experienced crashes using other JVM's, which were allergic to large data structures.

-- KLUE ID (or kid) is the primary key (for the Database) used to store a DNA sequence.
The assignment is arbitrary when a database is built.  **Therefore your kid is not my kid.**
Hopefully, in the future, some sort of offical database will be constructed with predefined kid's.
The need to distribute is also due to the time required to compile a database, they will be distributed via download.




-- Programming notes

NOTE TO SELF:
    vKLAT should be version2 KLUE based, whereas Metagenome would be version3 based.  Compeletely DIFFERENT set-ups!
    We must design around it.
    FORWARDONLY FLAG?
    Create some sort of definitions file for a database?


NOTE:
    Need to go over the files need to install for rocksDB (dependencies)



KLUE DATABASE is actually several database files, each file representing the equivalent of a table,
sometimes implemented as a key-value store.

    1)  KidDB (Kid database)
            a database containing critical information about each arbitrary KID assigned, such as taxonomy information
            Java binary/serialized format

    2)  DnaBitString database
            this stores the uncompressed bit representation of all reference sequences in the database
            this bit representation is that same that is used for K-mers, except DnaBitString saves all valid FASTA characters
            RocksDB format (as serialized values)

    3)  K-mer database
            The extremely large portion of the database, mapping Kmer sequences to positions
            RocksDB format
            IMPORTANT NOTE: After about 1.5 - 2.0 GB of FastA import, slows down.  Best to import as multiple small databases, then combine.

    4)  Variant Database (Optional)
            This contains information on the variants added to the database.  Note that building and processing this database
            Java binary/serialized format
            ALSO adds new (k-mer, position) pairs to the k-mer database

    5)  REBUILD IN ORDER
            OPTIONS: BuildRocks16Combine,  CombineRocksDatabases


    RocksDB is stored as a folder full of data files.
    K-mer database is further stored as 16 separate RocksDB databases.
        The primary motivation for this engineering choice is (1) to enable parallelism and (2) allow database to be easily
        split across hard drives without needing to use sym- or hard- links, which could slow the look-up system.

        Note that parallelism can easily be achieved with read-only databases by opening multiple database copies.
        This is not possible when writing to the database.


    K-mer database can be configured to store only the forward direction or both the forward and reverse direction.
    The latter can look up k-mers twice as fast, but requires twice as much space.
    Please note any

FAQ
    I get an error that "No Locks are available".  What does this mean?
    This means your program has opened the same database twice.


BUILDING A DATABASE

A KLUE database consists of multiple parts, each which must be built in a certain order.

    0)  Identify which reference sequences prepared as FASTA files you wish to include.  You will also need to generate
        KID entry information for each reference sequence.

    UPDATE:    Step 2 normally precedes step 1, because step 2 builds a KidDB and a DnaBitString DB simultaneously.

    1)  Build KidDB from tsv file
            It may require some manipulation on your part to convert the sequence identities to a importable format.

            Program: mainprograms.BuildKidDbFromTSV.java

            Script: BuildKidDbFromTSV.sh

    2)  Build DnaBitString Database

            Program:    mainprograms.BuildDatabaseFromFasta.java

            Script:     BuildDatabaseFromFasta.sh

    3)  Build K-mer database from DnaBitString Database

            Program:    mainprograms.BuildKmerFromDnaBitString.java

            Script:     BuildKmerFromDnaBitString.sh

        Here, heavy parrallelism can be employed to build many small subdatabases, which will be agglomerated in step 4.
        *16 part KLUE is default

    4)  Copy K-mer database into final version
        Building a K-mer database requires random write access, which yields a performance penalty for look-ups.
        Therefore, the entire database is copied, in-order, to its final destination.  Can open many subsidiary files

            Programs:   CopyKlueDatabase.java
                        CombineKlueDatabases.java


MISCELLANY

This error "org.rocksdb.RocksDBException: IO error: lock /mnt/vmdk/kluesuite/rocksdbdnabitstring/LOCK: Resource temporarily unavailable"
means that RocksDB is trying to open the same database file twice.
You may open the same database multiple times IFF all instances are read only.  For most cases, KLUE is read, not written.


TO DO LIST
    (in progress)  Build client/server modules.

    SHOULD Iterator be part of KLUE definition or RocksKLUE?  Clearly the latter, but how do we implement for 16 part?
        Need an iterator interface designated for KLUE?

    Design KLUE database settings file
        NOTE: have line to define dbs // RECALL this gets hard coded in the advanced KID
        ==> a builder takes this and returns a KLUE object?


    Design the programs to build databases for user


    Want a settings file system to define all relevant files and booleans for a KLUE database on disk.
        Generate and use the files.
        This settings file is used to open databases.  (Similar to BLAST nal file)
    Create 16 part KLUE using KLUE-3
        Why is this a problem?  Eh?
        Build metagenome database.
    Finalize / test Short Kmer
    KID database / importing tools
    Variant KLUE: multiple input formats

    KLAT and vKLAT:  full command line parameter specification
    Try to solve E-value mapping problem.



JOE TO DO LIST
    Goals: add important functionality and also familiarize self with system

    * Maybe we should define our database file settings format NOW?  rather than later?


    * Test KLUE server
    * In general, probably need clean up KLAT to make it more JAVA-rific
    * (So this is a whole design thing to get this and new pieces playing nice)
    * FileImport and FileExport classes
        * datastream module (feel free to rename)
        * FileExport needs to be flexible -- unclear how many columns and what we will make
        * maybe use builders with options?
        * modify KLAT to take (input) gzipped FastQ or FastA files
            * binary file stream --> text file stream --> functions  (maybe better way?)
        * modify KLAT to take (input) pair end reads
            * some (minor) modification of KLAT alignment protocol -- TBD; maybe we reformat functions; messy
            * new Test classes for KLAT to match new functions?
        * add FastQ support to KLAT (currently does FastA as hackalicious, no class)

        * option to create gzipped output file (desired?  IDK) for "BLAST format6"
            * Need to clarify in manual -- "BLAST format6 - like"
        * Add SAM and BAM support for OUTPUT
            * Where in SAM is variant information (if any?)

    * KLAT command line options
