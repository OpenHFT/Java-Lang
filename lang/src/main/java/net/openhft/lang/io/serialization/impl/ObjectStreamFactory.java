package net.openhft.lang.io.serialization.impl;

import java.io.*;

/**
 * Created by ruedi on 22.06.14.
 */
public interface ObjectStreamFactory {

    public ObjectOutput getObjectOutput(OutputStream out) throws IOException;
    public ObjectInput getObjectInput(InputStream in) throws IOException;

}
