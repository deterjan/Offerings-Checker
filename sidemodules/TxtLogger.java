package sidemodules;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 *  Simple txt logger
 */

public class TxtLogger
{
    FileWriter fw;
    BufferedWriter bw;

    public TxtLogger( String fileName)
    {
        try
        {
            File file = new File( fileName);

            // only creates new file if a file by that name doesn't exist
            file.createNewFile();

            fw = new FileWriter( file.getAbsoluteFile(), true);
            bw = new BufferedWriter( fw);

        } catch ( IOException e) {
            e.printStackTrace();
        }
    }

    public void writeLine( String s)
    {
        try {
            bw.write( s);
            bw.newLine();
        } catch ( IOException e) {
            e.printStackTrace();
        }
    }

    public void flush()
    {
        try {
            bw.flush();
        } catch ( IOException e) {
            e.printStackTrace();
        }
    }

    public void close()
    {
        try {
            bw.close();
        } catch ( IOException e) {
            e.printStackTrace();
        }
    }

}
