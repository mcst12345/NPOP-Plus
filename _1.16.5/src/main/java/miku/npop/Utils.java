package miku.npop;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class Utils {
    private static final Unsafe UNSAFE;
    private static final boolean windows;
    private static final boolean MacOS;

    static {
        try {
            Field unsafe_field = Unsafe.class.getDeclaredField("theUnsafe");
            unsafe_field.setAccessible(true);
            UNSAFE = (Unsafe) unsafe_field.get(null);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        String osName = System.getProperty("os.name");
        windows = osName.startsWith("Windows");
        MacOS = osName.startsWith("Mac") || osName.startsWith("Darwin");

    }

    public static Unsafe getUnsafe() {
        return UNSAFE;
    }

    public static boolean isWindows() {
        return windows;
    }

    public static boolean isMacOS() {
        return MacOS;
    }
}
