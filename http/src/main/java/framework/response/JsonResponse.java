package framework.response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonResponse extends Response {
    private final Object content;
    private final Gson gson;

    public JsonResponse(Object content) {
        this.content = content;
        this.gson = new GsonBuilder().setPrettyPrinting().create();  // dodajemo pretty printing
        this.header.add("Content-Type", "application/json");
    }

    @Override
    public String render() {
        StringBuilder responseContent = new StringBuilder();

        // Dodajemo HTTP status liniju
        responseContent.append("HTTP/1.1 200 OK\r\n");

        // Dodajemo Content-Type header ako već nije dodat
        if (!this.header.getKeys().contains("Content-Type")) {
            this.header.add("Content-Type", "application/json");
        }

        // Prvo konvertujemo content u JSON da znamo dužinu
        String jsonContent = gson.toJson(content);
        this.header.add("Content-Length", String.valueOf(jsonContent.length()));

        // Dodajemo sve header-e
        for (String key : this.header.getKeys()) {
            responseContent.append(key).append(": ").append(this.header.get(key)).append("\r\n");
        }

        // Prazna linija koja označava kraj header-a
        responseContent.append("\r\n");

        // Dodajemo JSON sadržaj
        responseContent.append(jsonContent);

        return responseContent.toString();
    }
}
