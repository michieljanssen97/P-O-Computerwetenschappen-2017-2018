package be.kuleuven.cs.robijn.common;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.stream.Collectors;

public class Resources {
    /**
     * Loads the file at path 'resourceName', reads its text and returns this as a string.
     * The path should start with a '/' and is relative to the 'resources' folder in the project.
     * @param resourceName the path of the resource being loaded
     * @return the contents of the resource, as a string.
     */
    public static String loadTextResource(String resourceName){
        try(BufferedReader in = new BufferedReader(new InputStreamReader(getResourceStream(resourceName)))){
            return in.lines().collect(Collectors.joining("\n"));
        }catch (IOException ex){
            throw new UncheckedIOException("Failed to load the resource under path '" + resourceName + "'.", ex);
        }
    }

    /**
     * Loads the image at path 'resourceName' from the application resources.
     * The path should start with a '/' and is relative to the 'resources' folder in the project.
     * @param resourceName the path of the resource being loaded
     * @return the contents of the resource, as an image.
     */
    public static BufferedImage loadImageResource(String resourceName){
        try(InputStream stream = getResourceStream(resourceName)){
            return ImageIO.read(stream);
        }catch (IOException ex){
            throw new UncheckedIOException("Failed to load the resource under path '" + resourceName + "'.", ex);
        }
    }

    /**
     * Returns a new InputStream for the resource 'resourceName' from the application resources.
     * The path should start with a '/' and is relative to the 'resources' folder in the project.
     * @param resourceName the path of the resource being loaded
     * @return the contents of the resource in an InputStream.
     */
    public static InputStream getResourceStream(String resourceName){
        return Resources.class.getResourceAsStream(resourceName);
    }

    /**
     * Returns the URL for the resource 'resourceName' from the application resources.
     * The path should start with a '/' and is relative to the 'resources' folder in the project.
     * @param resourceName the path of the resource being loaded
     * @return the url of the specified resource
     */
    public static URL getResourceURL(String resourceName){
        return Resources.class.getResource(resourceName);
    }
}
