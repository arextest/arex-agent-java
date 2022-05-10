package io.arex.foundation.api;

import java.util.List;

public abstract class ModuleInstrumentation {

    private List<TypeInstrumentation> types;
    private final String moduleName;
    protected final ModuleDescription target;

    protected ModuleInstrumentation(String name, ModuleDescription description) {
        this.moduleName = name;
        this.target = description;
    }

    public String name() {
        return moduleName;
    }

    public boolean validate() {
        types = instrumentationTypes();
        return types != null && types.size() > 0;
    }

    public abstract List<TypeInstrumentation> instrumentationTypes();

    public List<TypeInstrumentation> getInstrumentationTypes() {
        return types;
    }

}
