package  nl.ivonet.utility;

import java.io.File;
import java.lang.reflect.Field;

public final class Utils {

    /**
     * The most simple cdi like method.
     *
     * @param injectable the object you want to inject something in
     * @param fieldname the fieldname to inject to
     * @param value the value to assign to the fieldname
     */
    public static void injectField(final Object injectable, final String fieldname, final Object value) {
        try {
            final Field field = injectable.getClass()
                                          .getDeclaredField(fieldname);
            final boolean origionalValue = field.isAccessible();
            field.setAccessible(true);
            field.set(injectable, value);
            field.setAccessible(origionalValue);
        } catch (final NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Get a filname from the recourse folder.
     *
     * @param fileName the filename to get in src/test/resources
     * @return the absolute path to the filename
     */
    public static String getFileResource(final String fileName) {
        String abspath = new File(".").getAbsolutePath();
        abspath = abspath.substring(0, abspath.length() - 1);
        return new File(abspath + "src/test/resources/" + fileName).getAbsolutePath();
    }

    /**
     * The location of project/target
     *
     * @return string representation of the target folder
     */
    public static String getTargetLocation() {
        String abspath = new File(".").getAbsolutePath();
        abspath = abspath.substring(0, abspath.length() - 1);
        return new File(abspath + "target/").getAbsolutePath();
    }
}
