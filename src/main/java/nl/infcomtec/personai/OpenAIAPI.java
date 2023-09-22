package nl.infcomtec.personai;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * OpenAI API stuff.
 *
 * @author Walter Stroebel
 */
public class OpenAIAPI {

    private static final String API_ENDPOINT = "https://api.openai.com/v1/chat/completions";
    public static final ConcurrentLinkedDeque<Usage> usages = new ConcurrentLinkedDeque<>();

    /**
     * OpenAI key <b>NEVER IN GIT!!!</b>
     *
     * @return the key.
     */
    public static String getKey() {
        try ( FileReader reader = new FileReader(new File(System.getProperty("user.home"), "openai_aug.key"))) {
            StringBuilder sb = new StringBuilder();
            int ch;
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }
            return sb.toString().trim();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Ask a question.
     *
     * @param system System prompt (optional).
     * @param question to ask the assistant.
     * @return the assistant's answer.
     * @throws IOException if something failed.
     */
    public static String makeRequest(String system, String question) throws IOException {
        ArrayList<Message> two = new ArrayList<>();
        two.add(new Message(Message.ROLES.system, null == system ? "You are a helpful assistant." : system));
        two.add(new Message(Message.ROLES.user, question));
        return makeRequest(two);
    }

    /**
     * Quick &amp; dirty token usage.
     *
     * @return Total tokens since last call.
     */
    public static int getTotalTokenUsage() {
        List<Usage> snapshot = new ArrayList<>(usages);
        usages.clear();
        int ret = 0;
        for (Usage u : snapshot) {
            ret += u.totalTokens;
        }
        return ret;
    }

    /**
     * Ask some questions.
     *
     * @param messages questions to ask the assistant, should be
     * system,[user,assistant]*,user.
     * @return the assistant's answer.
     * @throws IOException if something failed.
     */
    public static String makeRequest(Message[] messages) throws IOException {
        return makeRequest(Arrays.asList(messages));
    }

    /**
     * Ask some questions.
     *
     * @param messages questions to ask the assistant, should be
     * system,[user,assistant]*,user.
     * @return the assistant's answer.
     * @throws IOException if something failed.
     */
    public static String makeRequest(List<Message> messages) throws IOException {
        OkHttpClient client = new OkHttpClient();
        JsonObject messageJson = new JsonObject();
        messageJson.addProperty("model", "gpt-3.5-turbo-16k");
        JsonArray messageArray = new JsonArray();
        for (Message e : messages) {
            messageArray.add(e.get());
        }
        messageJson.add("messages", messageArray);
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(messageJson.toString(), mediaType);

        Request request = new Request.Builder()
                .url(API_ENDPOINT)
                .addHeader("Authorization", "Bearer " + getKey())
                .post(requestBody)
                .build();
        try ( Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            if (null == response.body()) {
                throw new IOException("Null body " + response);
            }
            @SuppressWarnings("null")
            String responseBody = response.body().string();
            JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
            usages.add(new Usage(jsonResponse.getAsJsonObject("usage")));
            // just to avoid ChatGPT commenting on this
            if (usages.size() > 100000) {
                // nobody wants them I guess
                usages.clear();
            }
            return jsonResponse.getAsJsonArray("choices").get(0).getAsJsonObject().get("message").getAsJsonObject().get("content").getAsString();
        }
    }
}
