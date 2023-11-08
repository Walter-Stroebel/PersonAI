package nl.infcomtec.personai;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
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
    private static final File apiKeyFile = new File(System.getProperty("user.home"), "openai_aug.key");
    public static final ConcurrentLinkedDeque<Usage> usages = new ConcurrentLinkedDeque<>();
    public static final String MODEL_NAME = "gpt-4-1106-preview";
    private static final String SYSTEM_PROMPT1 = "You are a helpful assistant. Always provide a relevant and proactive response.";
    /**
     * For debugging, make this an opened file.
     */
    public static RandomAccessFile traceFile;
    public static final int MAX_TOKENS = 4096;
    public static final int TEMPERATURE = 0;
    public final static double ITC = 0.01 / 1000;
    public final static double OTC = 0.03 / 1000;

    /**
     * OpenAI key <b>NEVER IN GIT!!!</b>
     *
     * @return the key.
     */
    public static String getKey() {
        try (FileReader reader = new FileReader(apiKeyFile)) {
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
        two.add(new Message(Message.ROLES.system, null == system ? SYSTEM_PROMPT1 : system));
        two.add(new Message(Message.ROLES.user, question));
        return makeRequest(two);
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
        TraceLogger tl = new TraceLogger();
        tl.log(MODEL_NAME);
        tl.log(API_ENDPOINT);
        tl.log(apiKeyFile.getAbsolutePath());
        long start = System.currentTimeMillis();
        try {
            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .callTimeout(30, TimeUnit.SECONDS)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS);
            OkHttpClient client = builder.build();
            JsonObject messageJson = new JsonObject();
            messageJson.addProperty("model", MODEL_NAME);
            messageJson.addProperty("temperature", TEMPERATURE);
            messageJson.addProperty("max_tokens", MAX_TOKENS);
            messageJson.addProperty("top_p", 1);
            messageJson.addProperty("frequency_penalty", 0);
            messageJson.addProperty("presence_penalty", 0);
            JsonArray messageArray = new JsonArray();
            for (Message e : messages) {
                messageArray.add(e.get());
            }
            messageJson.add("messages", messageArray);
            MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
            RequestBody requestBody = RequestBody.create(messageJson.toString(), mediaType);
            tl.log(messageJson.toString());

            Request request = new Request.Builder()
                    .url(API_ENDPOINT)
                    .addHeader("Authorization", "Bearer " + getKey())
                    .post(requestBody)
                    .build();
            try (Response response = client.newCall(request).execute()) {
                tl.log("" + response);
                if (!response.isSuccessful()) {
                    tl.log("Unexpected code " + response);
                    throw new IOException("Unexpected code " + response);
                }
                if (null == response.body()) {
                    tl.log("Null body " + response);
                    throw new IOException("Null body " + response);
                }
                String responseBody = response.body().string();
                tl.log(responseBody);
                JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
                usages.add(new Usage(jsonResponse.getAsJsonObject("usage")));
                // just to avoid ChatGPT commenting on this
                if (usages.size() > 100000) {
                    // nobody wants them I guess
                    usages.clear();
                }
                tl.log(String.format("Request duration was %.3f seconds\n", (System.currentTimeMillis() - start) / 1000.0));
                return jsonResponse.getAsJsonArray("choices").get(0).getAsJsonObject().get("message").getAsJsonObject().get("content").getAsString();
            }
        } catch (IOException ex) {
            tl.log(String.format("Request failed after %.3f seconds\n", (System.currentTimeMillis() - start) / 1000.0));
            throw new IOException(ex.getMessage());
        }
    }
}
