/*
 */
package nl.infcomtec.personai;

import com.google.gson.JsonObject;

/**
 * Simple OpenAI API message array element.
 *
 * @author walter
 */
public class Message {

    public enum ROLES {
        system, user, assistant
    };
    public final ROLES role;
    public final String content;

    public Message(ROLES role, String message) {
        this.role = role;
        this.content = message;
    }

    public JsonObject get() {
        JsonObject ret = new JsonObject();
        ret.addProperty("role", role.name());
        ret.addProperty("content", content);
        return ret;
    }
}
