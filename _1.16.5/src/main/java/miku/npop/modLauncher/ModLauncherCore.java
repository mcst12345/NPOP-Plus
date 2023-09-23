package miku.npop.modLauncher;

import cpw.mods.modlauncher.*;
import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import joptsimple.OptionSpecBuilder;
import sun.misc.Unsafe;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class ModLauncherCore implements ITransformationService {
    public static final ClassLoader AppClassLoader = Launcher.class.getClassLoader();
    public static final Unsafe UNSAFE;
    public static final Field transformersField;
    public static final Field pluginHandlerField;
    public static final Field transformingClassLoaderField;
    public static final Field auditTrailField;
    public static final Field classLoaderField;
    public static final Field classTransformerField;
    public static final Field transformationServicesHandlerField;
    private static final Object Handler;
    public static final Method findClass;
    public static final Class<?> NPOPTransformationServicesHandlerClass;

    public static final TransformingClassLoader classLoader;
    public static final Object transformationServicesHandler;

    static {
        System.out.println("NPOP loading in modLauncher mode.");

        Field unsafe_field;
        try {
            unsafe_field = Unsafe.class.getDeclaredField("theUnsafe");
            classLoaderField = Launcher.class.getDeclaredField("classLoader");
            auditTrailField = ClassTransformer.class.getDeclaredField("auditTrail");
            transformingClassLoaderField = ClassTransformer.class.getDeclaredField("transformingClassLoader");
            pluginHandlerField = ClassTransformer.class.getDeclaredField("pluginHandler");
            transformersField = ClassTransformer.class.getDeclaredField("transformers");
            classTransformerField = TransformingClassLoader.class.getDeclaredField("classTransformer");
            transformationServicesHandlerField = Launcher.class.getDeclaredField("transformationServicesHandler");

            findClass = AppClassLoader.getClass().getDeclaredMethod("loadClass",String.class,boolean.class);

        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        unsafe_field.setAccessible(true);
        auditTrailField.setAccessible(true);
        transformingClassLoaderField.setAccessible(true);
        pluginHandlerField.setAccessible(true);
        transformersField.setAccessible(true);
        classLoaderField.setAccessible(true);
        classTransformerField.setAccessible(true);
        transformationServicesHandlerField.setAccessible(true);

        findClass.setAccessible(true);

        try {
            NPOPTransformationServicesHandlerClass = (Class<?>) findClass.invoke(AppClassLoader,"cpw.mods.modlauncher.NPOPTransformationServicesHandler",true);
        } catch (Throwable t){
            throw new RuntimeException(t);
        }

        try {
            UNSAFE = (Unsafe) unsafe_field.get(null);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        try {
            long tmp = UNSAFE.objectFieldOffset(classLoaderField);
            classLoader = (TransformingClassLoader) UNSAFE.getObjectVolatile(Launcher.INSTANCE,tmp);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }




        try {
            System.out.println("Getting the original transformationServicesHandler.");

            long temp = UNSAFE.objectFieldOffset(transformationServicesHandlerField);
            transformationServicesHandler = UNSAFE.getObjectVolatile(Launcher.INSTANCE,temp);

            System.out.println("Constructing NPOPTransformationServicesHandler.");

            long tmp = UNSAFE.objectFieldOffset((Field) NPOPTransformationServicesHandlerClass.getField("transformStoreField").get(null));
            TransformStore transformStore = (TransformStore) UNSAFE.getObjectVolatile(transformationServicesHandler,tmp);

            tmp = UNSAFE.objectFieldOffset((Field) NPOPTransformationServicesHandlerClass.getField("serviceLookupField").get(null));
            Map<String, TransformationServiceDecorator> serviceLookup = (Map<String, TransformationServiceDecorator>) UNSAFE.getObjectVolatile(transformationServicesHandler,tmp);

            tmp = UNSAFE.objectFieldOffset((Field) NPOPTransformationServicesHandlerClass.getField("transformationServicesField").get(null));
            ServiceLoader<ITransformationService> transformationServices = (ServiceLoader<ITransformationService>) UNSAFE.getObjectVolatile(transformationServicesHandler,tmp);

            Constructor<?> constructor = NPOPTransformationServicesHandlerClass.getConstructor(TransformStore.class,Map.class,ServiceLoader.class);

            Handler = constructor.newInstance(transformStore,serviceLookup,transformationServices);

            System.out.println("NPOPTransformationServicesHandler created. Replacing the original one.");

            UNSAFE.putObjectVolatile(Launcher.INSTANCE,temp,Handler);

            System.out.println("Success. transformationServicesHandler replaced.");
        }catch (Throwable t){
            throw new RuntimeException(t);
        }
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
