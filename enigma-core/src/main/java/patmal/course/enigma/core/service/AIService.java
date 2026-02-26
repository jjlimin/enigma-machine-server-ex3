package patmal.course.enigma.core.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import patmal.course.enigma.core.dto.ai.AIResponseDTO;

import java.util.*;

@Service
public class AIService {
    private final JdbcTemplate jdbcTemplate;
    private final RestTemplate restTemplate;

    public AIService(JdbcTemplate jdbcTemplate, RestTemplate restTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.restTemplate = restTemplate;
    }

    @Value("${enigma.ai.api-key}") //
    private String apiKey;

    // Refined System Prompt based on your ERD
    private static final String SYSTEM_PROMPT =
            "You are an expert SQL assistant for an Enigma Machine management system. " +
                    "Schema:\n" +
                    "- machines(id UUID PK, name TEXT, rotors_count INTEGER, abc TEXT)\n" +
                    "- machines_rotors(id UUID PK, machine_id UUID FK, rotor_id INTEGER, notch INTEGER, wiring_right TEXT, wiring_left TEXT)\n" +
                    "- machines_reflectors(id UUID PK, machine_id UUID FK, reflector_id (enum), input TEXT, output TEXT)\n" +
                    "- processing(id UUID PK, machine_id UUID FK, session_id TEXT, code TEXT, input TEXT, output TEXT, time BIGINT)\n\n" +
                    "Rules:\n" +
                    "1. Return ONLY the raw SQL string. Do not use markdown code blocks (e.g., do not wrap in ```sql).\n" +
                    "2. ONLY generate SELECT queries. Prohibited: INSERT, UPDATE, DELETE, DROP, TRUNCATE.\n" +
                    "3. Time is in nanoseconds.\n" +
                    "4. Be highly forgiving of typos, spelling mistakes (e.g., 'masages' instead of machines/messages), and poor grammar. Always try your best to infer the user's intent and map it to the provided schema to generate a valid SELECT query.\n" +
                    "5. Return EXACTLY the string 'ERROR_INVALID_CONTEXT' ONLY if the user's prompt is completely unrelated to the Enigma system or data analysis (e.g., 'how are you', 'write a poem', 'what is the weather'). If there is any logical way to query the database based on the prompt, generate the SQL.";

    public AIResponseDTO handleAIQuery(String userQuery) {
        // Step 1: Text-to-SQL
        String sqlPrompt = SYSTEM_PROMPT + "\nUser Request: " + userQuery;
        String generatedSql = callOpenAI(sqlPrompt)
                .replaceAll("```sql|```", "") // Clean AI Markdown
                .trim();

        if ("ERROR_INVALID_CONTEXT".equals(generatedSql)) {
            return new AIResponseDTO(
                    "I can only answer questions related to the Enigma machine's data and processing history.",
                    null
            );
        }

        // Security Check: Ensure it is a read-only SELECT
        if (!generatedSql.toUpperCase().startsWith("SELECT")) {
            throw new SecurityException("Only SELECT queries are allowed.");
        }

        // Step 2: Execute SQL dynamically
        List<Map<String, Object>> dbResults = jdbcTemplate.queryForList(generatedSql);

        // Step 3: Natural Language Response
        String finalPrompt = String.format(
                "User Question: %s\nDatabase Results: %s\n" +
                        "Summarize the results into a helpful, conversational answer. " +
                        "CRITICAL: Your response will be injected directly into a JSON string value. " +
                        "Do NOT use any markdown formatting. " +
                        "Do NOT use asterisks (*), hashtags (#), or bullet points. " +
                        "Do NOT use explicit newline characters (\\n) or carriage returns. " +
                        "Write the entire summary as a single, continuous, plain-text paragraph.",
                userQuery, dbResults.toString()
        );
        String finalAnswer = callOpenAI(finalPrompt);

        return new AIResponseDTO(finalAnswer, generatedSql);
    }

    private String callOpenAI(String prompt) {
        String url = "https://api.openai.com/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey); //

        Map<String, Object> body = new HashMap<>();
        body.put("model", "gpt-4o-mini");
        body.put("messages", List.of(Map.of("role", "user", "content", prompt)));
        body.put("temperature", 0.0);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");

        return message.get("content").toString().trim();
    }
}