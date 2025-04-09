package dryven.model.binding.form;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import dryven.config.DatabaseConfigurationSource;
import dryven.model.di.DependencyService;
import dryven.model.di.LocalThreadStorage;
import dryven.persistence.provider.PersistenceProvider;
import dryven.request.controller.paramtransform.baseimpl.EntityInjectingBinder;

public class DecodedFormModelCollection {

    private Map<Class<?>, Object> _models;

    private EntityInjectingBinder _entityLoader;

    public DecodedFormModelCollection(DatabaseConfigurationSource dbConfig) {
        super();
        _models = new HashMap<Class<?>, Object>();
        _entityLoader = new EntityInjectingBinder(dbConfig);
    }

    @SuppressWarnings("unchecked")
    public <T> T getOrCreateModel(Class<T> clazz, int pk, LocalThreadStorage storage) {
        Object model = _models.get(clazz);
        if (model == null) {
            if (pk == 0) {
                try {
                    Constructor<?> ctor = clazz.getConstructor(new Class<?>[] {});
                    boolean accessible = ctor.isAccessible();
                    if (!accessible) {
                        ctor.setAccessible(true);
                    }
                    model = ctor.newInstance(new Object[] {});
                    if (!accessible) {
                        ctor.setAccessible(false);
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Could not get model default constructor: " + clazz.getName());
                }
            } else {
                model = _entityLoader.loadEntity(clazz, pk, storage);
            }
            _models.put(clazz, model);
        }
        return (T) model;
    }

    @SuppressWarnings("unchecked")
    public <T> T getModel(Class<T> clazz) {
        Object model = _models.get(clazz);
        if (model == null) {
            for (Class<?> c : _models.keySet()) {
                if (clazz.isAssignableFrom(c)) {
                    return (T) _models.get(c);
                }
            }
        }
        return (T) model;
    }

    public Iterable<Class<?>> getModelTypes() {
        return _models.keySet();
    }
}
