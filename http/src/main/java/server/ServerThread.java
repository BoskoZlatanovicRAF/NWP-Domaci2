package server;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import framework.response.JsonResponse;
import framework.response.Response;
import framework.request.enums.Method;
import framework.request.Header;
import framework.request.Helper;
import framework.request.Request;
import framework.request.exceptions.RequestNotValidException;
import framework.route.RouteHandler;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ServerThread implements Runnable{
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private RouteHandler routeHandler;  // dodajemo RouteHandler
    private final Gson gson;

    public ServerThread(Socket socket, RouteHandler routeHandler) {  // modifikujemo konstruktor
        this.socket = socket;
        this.routeHandler = routeHandler;
        this.gson = new Gson();
        try {
            in = new BufferedReader(
                    new InputStreamReader(
                            socket.getInputStream()));

            out = new PrintWriter(
                    new BufferedWriter(
                            new OutputStreamWriter(
                                    socket.getOutputStream())), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            Request request = this.generateRequest();
            if(request == null) {
                in.close();
                out.close();
                socket.close();
                return;
            }

            // Koristimo RouteHandler za obradu zahteva
            Response response = routeHandler.handleRequest(request);

            out.println(response.render());

            in.close();
            out.close();
            socket.close();

        } catch (Exception e) {  // hvatamo sve izuzetke
            e.printStackTrace();
            // možda bi trebalo poslati error response klijentu
        }
    }
    private Request generateRequest() throws IOException, RequestNotValidException {
        String command = in.readLine();
        if(command == null) {
            return null;
        }

        System.out.println("Received command: " + command);  // debug

        String[] actionRow = command.split(" ");
        Method method = Method.valueOf(actionRow[0]);
        String route = actionRow[1];
        Header header = new Header();
        HashMap<String, String> parameters = Helper.getParametersFromRoute(route);

        // Čitamo headere
        StringBuilder bodyBuilder = new StringBuilder();
        String line;
        while (!(line = in.readLine()).isEmpty()) {
            System.out.println("Header line: " + line);  // debug
            if (line.contains(": ")) {
                String[] headerParts = line.split(": ", 2);
                header.add(headerParts[0], headerParts[1]);
            }
        }

        // Za POST zahteve, čitamo body
        if (method.equals(Method.POST)) {
            String contentType = header.get("Content-Type");
            String contentLengthStr = header.get("Content-Length");

            if (contentLengthStr != null) {
                int contentLength = Integer.parseInt(contentLengthStr);
                char[] bodyChars = new char[contentLength];
                in.read(bodyChars, 0, contentLength);
                String body = new String(bodyChars);

                System.out.println("Received body: " + body);  // debug

                if (contentType != null && contentType.contains("application/json")) {
                    try {
                        TypeToken<Map<String, Object>> typeToken = new TypeToken<Map<String, Object>>() {};
                        Map<String, Object> jsonMap = gson.fromJson(body, typeToken.getType());
                        for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {
                            // Konvertuj double u int ako je broj
                            if (entry.getValue() instanceof Double) {
                                double value = (Double) entry.getValue();
                                parameters.put(entry.getKey(), String.valueOf((int)value));
                            } else {
                                parameters.put(entry.getKey(), String.valueOf(entry.getValue()));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RequestNotValidException("Invalid JSON body: " + e.getMessage());
                    }
                }
                 else {
                    parameters.putAll(Helper.getParametersFromString(body));
                }
            }
        }


        return new Request(method, route, header, parameters);
    }
}
