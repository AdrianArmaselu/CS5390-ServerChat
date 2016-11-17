package edu.utdallas.cs5390.chat.framework.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by adisor on 11/16/2016.
 */
public abstract class ContextualProtocol implements Protocol {
    private Map<String, Object> context;

    public ContextualProtocol() {
        this.context = new HashMap<>();
    }

    public Object getContextValue(String key) {
        return context.get(key);
    }

    public void setContextValue(String key, Object value) {
        context.put(key, value);
    }
}
