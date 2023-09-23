package miku.npop.modLauncher;

import cpw.mods.modlauncher.ClassTransformer;
import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.NPOPClassTransformer;
import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.IncompatibleEnvironmentException;
import joptsimple.OptionSpecBuilder;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class ModLauncherCore implements ITransformationService {
    public static final Field transformers;
    public static final Field pluginHandler;
    public static final Field transformingClassLoader;
    public static final Field auditTrail;
    public static final Field classLoader;

    public static final NPOPClassTransformer transformer;

    static {

        try {
            classLoader = Launcher.class.getDeclaredField("classLoader");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        try {
            auditTrail = ClassTransformer.class.getDeclaredField("auditTrail");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        try {
            transformingClassLoader = ClassTransformer.class.getDeclaredField("transformingClassLoader");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        try {
            pluginHandler = ClassTransformer.class.getDeclaredField("pluginHandler");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        try {
            transformers = ClassTransformer.class.getDeclaredField("transformers");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        auditTrail.setAccessible(true);
        transformingClassLoader.setAccessible(true);
        pluginHandler.setAccessible(true);
        transformers.setAccessible(true);
        classLoader.setAccessible(true);

        try {
            transformer = new NPOPClassTransformer((ClassTransformer) classLoader.get(Launcher.INSTANCE));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    @Nonnull
    @Override
    public String name() {
        return null;
    }

    @Override
    public void arguments(BiFunction<String, String, OptionSpecBuilder> argumentBuilder) {
        ITransformationService.super.arguments(argumentBuilder);
    }

    @Override
    public void argumentValues(OptionResult option) {
        ITransformationService.super.argumentValues(option);
    }

    @Override
    public void initialize(IEnvironment environment) {

    }

    @Override
    public void beginScanning(IEnvironment environment) {

    }

    @Override
    public List<Map.Entry<String, Path>> runScan(IEnvironment environment) {
        return ITransformationService.super.runScan(environment);
    }

    @Override
    public void onLoad(IEnvironment env, Set<String> otherServices) throws IncompatibleEnvironmentException {

    }

    @Nonnull
    @Override
    public List<ITransformer> transformers() {
        return null;
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
