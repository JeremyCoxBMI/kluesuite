*****
To build KLUE database (k-mer disk based database)

You need to determine a database name, which will be the prefix to all database files.
The database is called by its prefix name for simplicity, represented as {prefix name} below

1) run multiple instances of buildDatabasePiece.sh in parallel
2) run combineDatabasePieces.sh

3) delete temporary files
    rm -r *deleteme*

4) recommend keeping a database backup in case of corruption, although not required.  (It does take space and time to create)

    tar -zcf {prefix name}.gzip {prefix name}*


    Alternatively, if you have space, just make a copy

    mkdir backup
    cp {prefix name}* backup/

5) to restore
    rm -r {prefix name}.kidDB.diskall {prefix name}.kidDB.kmer {prefix name}.kidDB.kmer.startEnd
    tar -zxf {prefix name}.gzip

*****
To run KLAT (alignment tool)
    klat.sh

*****
To run WILDklat (short sequence alignment/search using wild cards)
    wildklat.sh
        (not implemented yet)
