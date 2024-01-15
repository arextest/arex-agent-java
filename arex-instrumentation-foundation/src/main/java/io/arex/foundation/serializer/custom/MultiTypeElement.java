package io.arex.foundation.serializer.custom;

import com.google.gson.JsonElement;

public class MultiTypeElement {
    private JsonElement value;
    private String type;

    public MultiTypeElement() {
    }

    public MultiTypeElement(final JsonElement value, final String type) {
        this.value = value;
        this.type = type;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public JsonElement getValue() {
        return value;
    }

    public void setValue(JsonElement value) {
        this.value = value;
    }
}
