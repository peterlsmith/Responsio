package com.paradoxwebsolutions.assistant;

import com.paradoxwebsolutions.core.ApplicationError;
import com.paradoxwebsolutions.core.ResourceAPI;

import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;




/**
 * 
 * <p>Instances of this class are used to isolate identify specific class (e.g. custsom action classes)
 * from the rest of the system.
 *
 * @author Peter Smith
 */
public class IdentityArchive implements ResourceAPI {

    private ZipFile zip;

    public IdentityArchive(final String archiveName) throws ApplicationError {
        try {
            zip = new ZipFile(archiveName);
        }
        catch (Exception x) {
            throw new ApplicationError(String.format("Unable to open zip archive '%s'", archiveName), x);
        }
    }

    @Override
    public InputStream getInputStream(final String filename) throws ApplicationError {
        try {
            ZipEntry entry = zip.getEntry(filename);
            if (entry == null) throw new ApplicationError(String.format("File '%s' not found in archive", filename));

            return zip.getInputStream(entry);
        }
        catch (Exception x) {
            throw new ApplicationError(String.format("Unable to read file '%s' from zip archive", filename), x);
        }
    }

    public String[] getFiles() {
        return zip.stream().map((ze) -> ze.getName()).toArray(String[]::new);
    }

    public String[] getFiles(final String regex) {
        return zip.stream().map((ze) -> ze.getName()).filter((n) -> n.matches(regex)).toArray(String[]::new);
    }
}
