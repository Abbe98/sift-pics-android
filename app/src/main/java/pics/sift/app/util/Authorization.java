package pics.sift.app.util;

import android.content.Context;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonReader;

import java.io.StringReader;

import pics.sift.app.data.Device;

public class Authorization {
    private static final String KEY_TYPE = "type";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";

    public static Authorization getAnonymous(Context context) {
        String password = SHA256.encode(Device.getUniqueIdentifier(context));

        return new Authorization(Type.ANONYMOUS, SHA1.encode(password), password);
    }

    public static Authorization parse(String str) {
        if(str != null) {
            try {
                JsonElement element = new JsonParser().parse(new JsonReader(new StringReader(str)));

                if(element.isJsonObject()) {
                    return new Authorization(element.getAsJsonObject());
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private Type m_type;
    private String m_username;
    private String m_password;

    public Authorization(JsonObject attributes) {
        JsonPrimitive primitive;

        m_type = Type.parse(((primitive = attributes.getAsJsonPrimitive(KEY_TYPE)) != null && primitive.isNumber()) ? primitive.getAsInt() : Type.UNKNOWN.getCode());
        m_username = ((primitive = attributes.getAsJsonPrimitive(KEY_USERNAME)) != null && primitive.isString()) ? primitive.getAsString() : null;
        m_password = ((primitive = attributes.getAsJsonPrimitive(KEY_PASSWORD)) != null && primitive.isString()) ? primitive.getAsString() : null;
    }

    protected Authorization(Type type, String username, String password) {
        m_type = type;
        m_username = username;
        m_password = password;
    }

    public JsonObject getAttributes() {
        JsonObject attributes = new JsonObject();

        attributes.addProperty(KEY_TYPE, m_type.getCode());

        if(m_username != null) {
            attributes.addProperty(KEY_USERNAME, m_username);
        }

        if(m_password != null) {
            attributes.addProperty(KEY_PASSWORD, m_password);
        }

        return attributes;
    }

    public Type getType() {
        return m_type;
    }

    public String getUsername() {
        return m_username;
    }

    public String getPassword() {
        return m_password;
    }

    @Override
    public String toString() {
        return getAttributes().toString();
    }

    public static enum Type {
        ANONYMOUS(0),
        UNKNOWN(-1);

        private final int m_code;

        public static Type parse(int code) {
            for(Type type : values()) {
                if(type.getCode() == code) {
                    return type;
                }
            }

            return UNKNOWN;
        }

        private Type(int code) {
            m_code = code;
        }

        public int getCode() {
            return m_code;
        }
    }
}
