package be.kuleuven.cs.robijn.common;

import java.io.*;
import java.util.stream.Collectors;

public class Resources {
    /**
     * Loads the file at path 'resourceName', reads its text and returns this as a string.
     * The path should start with a '/' and is relative to the 'resources' folder in the project.
     * @param resourceName the path of the resource being loaded
     * @return the contents of the resource, as a string.
     */
    public static String loadTextResource(String resourceName){
        try(BufferedReader in = new BufferedReader(new InputStreamReader(Resources.class.getResourceAsStream(resourceName)))){
            return in.lines().collect(Collectors.joining("\n"));
        }catch (IOException ex){
            throw new UncheckedIOException("Failed to load the resource under path '" + resourceName + "'.", ex);
        }
    }
}