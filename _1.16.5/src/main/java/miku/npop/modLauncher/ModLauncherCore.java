package miku.npop.modLauncher;

import cpw.mods.modlauncher.ClassTransformer;
import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.NPOPClassTransformer;
import cpw.mods.modlauncher.TransformingClassLoader;
import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import joptsimple.OptionSpecBuilder;
import sun.misc.Unsafe;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class ModLauncherCore implements ITransformationService {
    public static final Unsafe UNSAFE;
    public static final Field transformersField;
    public static final Field pluginHandlerField;
    public static final Field transformingClassLoaderField;
    public static final Field auditTrailField;
    public static final Field classLoaderField;
    public static final Field classTransformerField;
    public static final NPOPClassTransformer transformer;
    public static final TransformingClassLoader classLoader;

    static {
        System.out.println("NPOP loading in modLauncher mode.");

        try {
            classLoaderField = Launcher.class.getDeclaredField("classLoader");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        try {
            auditTrailField = ClassTransformer.class.getDeclaredField("auditTrail");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        try {
            transformingClassLoaderField = ClassTransformer.class.getDeclaredField("transformingClassLoader");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        try {
            pluginHandlerField = ClassTransformer.class.getDeclaredField("pluginHandler");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        try {
            transformersField = ClassTransformer.class.getDeclaredField("transformers");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        auditTrailField.setAccessible(true);
        transformingClassLoaderField.setAccessible(true);
        pluginHandlerField.setAccessible(true);
        transformersField.setAccessible(true);
        classLoaderField.setAccessible(true);

        try {
            classLoader = (TransformingClassLoader) classLoaderField.get(Launcher.INSTANCE);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        try {
            classTransformerField = TransformingClassLoader.class.getDeclaredField("classTransformer");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        Field unsafe_field;
        try {
            unsafe_field = Unsafe.class.getDeclaredField("theUnsafe");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        unsafe_field.setAccessible(true);

        try {
            UNSAFE = (Unsafe) unsafe_field.get(null);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        try {
            System.out.println("Constructing NPOPClassTransformer.");
            transformer = new NPOPClassTransformer((ClassTransformer) classTransformerField.get(classLoader));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        try {
            System.out.println("Replacing ClassTransformer in ClassLoader.");
            long tmp = UNSAFE.objectFieldOffset(classTransformerField);
            UNSAFE.putObjectVolatile(classLoader,tmp,transformer);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        System.out.println("NPOP loaded.");
    }
    @Nonnull
    @Override
    public String name() {
        return "NoPrivateOrProtected-Plus";
    }

    @Override
    public void arguments(@Nonnull BiFunction<String, String, OptionSpecBuilder> argumentBuilder) {
        ITransformationService.super.arguments(argumentBuilder);
    }

    @Override
    public void argumentValues(@Nonnull OptionResult option) {
        ITransformationService.super.argumentValues(option);
    }

    @Override
    public void initialize(@Nonnull IEnvironment environment) {

    }

    @Override
    public void beginScanning(@Nonnull IEnvironment environment) {

    }

    @Override
    public List<Map.Entry<String, Path>> runScan(@Nonnull IEnvironment environment) {
        return ITransformationService.super.runScan(environment);
    }

    @Override
    public void onLoad(@Nonnull IEnvironment env,@Nonnull Set<String> otherServices) {

    }

    @Nonnull
    @Override
    public List<ITransformer> transformers() {
        return Collections.emptyList();
    }

    @Override
    public Map.Entry<Set<String>, Supplier<Function<String, Optional<URL>>>> additionalClassesLocator() {
        return ITransformationService.super.additionalClassesLocator();
    }

    @Override
    public Map.Entry<Set<String>, Supplier<Function<String, Optional<URL>>>> additionalResourcesLocator() {
        return ITransformationService.super.additionalResourcesLocator();
    }
}
