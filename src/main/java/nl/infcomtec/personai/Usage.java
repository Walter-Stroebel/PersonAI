package nl.infcomtec.personai;

import com.google.gson.JsonObject;

/**
 * Keep track of token usage.
 * @author Walter Stroebel
 */
public class Usage {

    public final int promptTokens;
    public final int completionTokens;
    public final int totalTokens;

    public Usage(JsonObject usage) {
        promptTokens = usage.get("prompt_tokens").getAsInt();
        completionTokens = usage.get("completion_tokens").getAsInt();
        totalTokens = usage.get("total_tokens").getAsInt();
    }

}
