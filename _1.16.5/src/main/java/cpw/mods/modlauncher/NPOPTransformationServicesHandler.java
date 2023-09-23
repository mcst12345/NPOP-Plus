package cpw.mods.modlauncher;

import cpw.mods.modlauncher.api.ITransformationService;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.ServiceLoader;

import static miku.npop.modLauncher.ModLauncherCore.UNSAFE;
import static miku.npop.modLauncher.ModLauncherCore.classTransformerField;

public class NPOPTransformationServicesHandler extends TransformationServicesHandler{
    public static final Field transformationServicesField;
    public static final Field serviceLookupField;
    public static final Field transformStoreField;

    static {

        try {
            transformStoreField = TransformationServicesHandler.class.getDeclaredField("transformStore");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        try {
            serviceLookupField = TransformationServicesHandler.class.getDeclaredField("serviceLookup");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        try {
            transformationServicesField = TransformationServicesHandler.class.getDeclaredField("transformationServices");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        serviceLookupField.setAccessible(true);
        transformationServicesField.setAccessible(true);
        transformStoreField.setAccessible(true);
    }

    public NPOPTransformationServicesHandler(TransformStore transformStore, Map<String, TransformationServiceDecorator> serviceLookup, ServiceLoader<ITransformationService> transformationServices) {
        super(transformStore);
        try {
            long tmp = UNSAFE.objectFieldOffset(transformationServicesField);
            UNSAFE.putObjectVolatile(this,tmp,transformationServices);
        } catch (Throwable t){
            throw new RuntimeException(t);
        }
        try {
            long tmp = UNSAFE.objectFieldOffset(serviceLookupField);
            UNSAFE.putObjectVolatile(this,tmp,serviceLookup);
        } catch (Throwable t){
            throw new RuntimeException(t);
        }
    }

    @Override
    TransformingClassLoader buildTransformingClassLoader(final LaunchPluginHandler pluginHandler, final TransformingClassLoaderBuilder builder, final Environment environment) {
        TransformingClassLoader classLoader = super.buildTransformingClassLoader(pluginHandler,builder,environment);
        try {
            System.out.println("Constructing NPOPClassTransformer.");
            long tmp = UNSAFE.objectFieldOffset(classTransformerField);
            NPOPClassTransformer transformer = new NPOPClassTransformer((ClassTransformer) UNSAFE.getObjectVolatile(classLoader, tmp));
            System.out.println("NPOPClassTransformer created. Inject it into classLoader.");
            tmp = UNSAFE.objectFieldOffset(classTransformerField);
            UNSAFE.putObjectVolatile(classLoader,tmp, transformer);
            System.out.println("Success. NPOP-Plus is loaded.");
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return classLoader;
    }
}
