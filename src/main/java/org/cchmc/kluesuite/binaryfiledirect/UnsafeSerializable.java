package org.cchmc.kluesuite.binaryfiledirect;

/**
 * Created by osboxes on 5/30/17.
 *
 * Interface for writing SMALL to MEDIUM objects, as entire object is copied in memory.
 *
 */
public interface UnsafeSerializable {

    public int getWriteUnsafeSize();

    /**
     * Always write the total bytes as first entry, then Serializer UUID;
     * @param um
     */
    public void writeUnsafe(UnsafeMemory um);

    /**
     * Always reads the Serializer UUID first, because size was pulled from stream to allocate the bytes.
     * @param um
     */
    public void readUnsafe(UnsafeMemory um) throws ClassCastException;





}
