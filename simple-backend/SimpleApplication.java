import java.io.*;
import java.net.*;
import java.util.*;

public class SimpleApplication {
    private static final int PORT = 8080;
    private static Map<String, String> users = new HashMap<>();
    private static Map<String, String> sessions = new HashMap<>();
    
    static {
        // Initialize test users
        users.put("admin", "admin123");
        users.put("user", "user123");
    }
    
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Simple backend service started on port: " + PORT);
        
        while (true) {
            Socket clientSocket = serverSocket.accept();
            new Thread(() -> handleRequest(clientSocket)).start();
        }
    }
    
    private static void handleRequest(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            
            String requestLine = in.readLine();
            if (requestLine == null) return;
            
            System.out.println("Received request: " + requestLine);
            
            // Parse request
            String[] parts = requestLine.split(" ");
            String method = parts[0];
            String path = parts[1];
            
            // Read request headers
            Map<String, String> headers = new HashMap<>();
            String line;
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                String[] headerParts = line.split(": ", 2);
                if (headerParts.length == 2) {
                    headers.put(headerParts[0], headerParts[1]);
                }
            }
            
            // Handle CORS
            StringBuilder response = new StringBuilder();
            response.append("HTTP/1.1 200 OK\r\n");
            response.append("Content-Type: application/json; charset=utf-8\r\n");
            response.append("Access-Control-Allow-Origin: http://localhost:5176\r\n");
            response.append("Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS\r\n");
            response.append("Access-Control-Allow-Headers: Content-Type, Authorization\r\n");
            response.append("Access-Control-Allow-Credentials: true\r\n");
            
            // Handle OPTIONS preflight request
            if ("OPTIONS".equals(method)) {
                response.append("\r\n");
                out.print(response.toString());
                out.flush();
                return;
            }
            
            // Handle login request
            if ("/api/auth/login".equals(path) && "POST".equals(method)) {
                // Read request body
                String contentLength = headers.get("Content-Length");
                if (contentLength != null) {
                    int length = Integer.parseInt(contentLength);
                    char[] buffer = new char[length];
                    in.read(buffer);
                    String body = new String(buffer);
                    
                    // Parse JSON (simple processing)
                    String username = extractJsonValue(body, "username");
                    String password = extractJsonValue(body, "password");
                    
                    if (username != null && password != null && users.containsKey(username) && users.get(username).equals(password)) {
                        String token = UUID.randomUUID().toString();
                        sessions.put(token, username);
                        
                        String jsonResponse = String.format(
                            "{\"code\": 200, \"message\": \"Login successful\", \"data\": {\"token\": \"%s\", \"username\": \"%s\"}}",
                            token, username
                        );
                        response.append("Content-Length: ").append(jsonResponse.getBytes().length).append("\r\n");
                        response.append("\r\n");
                        response.append(jsonResponse);
                    } else {
                        String jsonResponse = "{\"code\": 401, \"message\": \"Invalid username or password\"}";
                        response.append("Content-Length: ").append(jsonResponse.getBytes().length).append("\r\n");
                        response.append("\r\n");
                        response.append(jsonResponse);
                    }
                }
            }
            // Get user info
            else if ("/api/auth/userinfo".equals(path) && "GET".equals(method)) {
                String authorization = headers.get("Authorization");
                if (authorization != null && authorization.startsWith("Bearer ")) {
                    String token = authorization.substring(7);
                    String username = sessions.get(token);
                    
                    if (username != null) {
                        String jsonResponse = String.format(
                            "{\"code\": 200, \"message\": \"Success\", \"data\": {\"username\": \"%s\", \"name\": \"%s\"}}",
                            username, username
                        );
                        response.append("Content-Length: ").append(jsonResponse.getBytes().length).append("\r\n");
                        response.append("\r\n");
                        response.append(jsonResponse);
                    } else {
                        String jsonResponse = "{\"code\": 401, \"message\": \"Unauthorized\"}";
                        response.append("Content-Length: ").append(jsonResponse.getBytes().length).append("\r\n");
                        response.append("\r\n");
                        response.append(jsonResponse);
                    }
                } else {
                    String jsonResponse = "{\"code\": 401, \"message\": \"Unauthorized\"}";
                    response.append("Content-Length: ").append(jsonResponse.getBytes().length).append("\r\n");
                    response.append("\r\n");
                    response.append(jsonResponse);
                }
            }
            // Dashboard stats
            else if ("/api/dashboard/stats".equals(path) && "GET".equals(method)) {
                String jsonResponse = "{\"code\": 200, \"message\": \"Success\", \"data\": {\"totalUsers\": 156, \"totalOrders\": 89, \"totalRevenue\": 12580, \"growthRate\": 12.5}}";
                response.append("Content-Length: ").append(jsonResponse.getBytes().length).append("\r\n");
                response.append("\r\n");
                response.append(jsonResponse);
            }
            // Chart data
            else if ("/api/dashboard/chart".equals(path) && "GET".equals(method)) {
                String jsonResponse = "{\"code\": 200, \"message\": \"Success\", \"data\": {\"categories\": [\"Jan\", \"Feb\", \"Mar\", \"Apr\", \"May\", \"Jun\"], \"data\": [120, 200, 150, 80, 70, 110]}}";
                response.append("Content-Length: ").append(jsonResponse.getBytes().length).append("\r\n");
                response.append("\r\n");
                response.append(jsonResponse);
            }
            else {
                String jsonResponse = "{\"code\": 404, \"message\": \"Interface not found\"}";
                response.append("Content-Length: ").append(jsonResponse.getBytes().length).append("\r\n");
                response.append("\r\n");
                response.append(jsonResponse);
            }
            
            out.print(response.toString());
            out.flush();
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private static String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\"";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex == -1) return null;
        
        int colonIndex = json.indexOf(":", keyIndex);
        if (colonIndex == -1) return null;
        
        int valueStart = colonIndex + 1;
        while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
            valueStart++;
        }
        
        if (valueStart >= json.length()) return null;
        
        char quote = json.charAt(valueStart);
        if (quote == '"') {
            int valueEnd = json.indexOf("\"", valueStart + 1);
            if (valueEnd == -1) return null;
            return json.substring(valueStart + 1, valueEnd);
        }
        
        return null;
    }
}