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
    private static final String INVALID_CONTEXT_RESPONSE =
            "I can only answer questions related to the Enigma machine's data and processing history.";
    private static final String DB_FETCH_FAILED_RESPONSE =
            "I could not fetch information from the database for that request after retrying. Please try asking something else.";

    public AIService(JdbcTemplate jdbcTemplate, RestTemplate restTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.restTemplate = restTemplate;
    }

    @Value("${enigma.ai.api-key}") //
    private String apiKey;

    // Refined System Prompt - Aggressive Counting Rules
    private static final String SYSTEM_PROMPT =
            "<SYSTEM_CONTEXT>\n" +
                    "You are an expert SQL assistant for an Enigma Machine management system.\n" +
                    "</SYSTEM_CONTEXT>\n\n" +
                    "<SCHEMA>\n" +
                    "- machines(id UUID PK, name TEXT, rotors_count INTEGER, abc TEXT)\n" +
                    "- machines_rotors(id UUID PK, machine_id UUID FK, rotor_id INTEGER, notch INTEGER, wiring_right TEXT, wiring_left TEXT)\n" +
                    "- machines_reflectors(id UUID PK, machine_id UUID FK, reflector_id TEXT, input TEXT, output TEXT)\n" +
                    "- processing(id UUID PK, machine_id UUID FK, session_id TEXT, code TEXT, input TEXT, output TEXT, time BIGINT)\n" +
                    "</SCHEMA>\n\n" +
                    "<VOCABULARY_AND_RELATIONS>\n" +
                    "- 'messages', 'encryptions', or 'processed' refer to the `processing` table.\n" +
                    "- 'components' refer to rotors and reflectors.\n" +
                    "- JOIN RULE: When joining `machines` to any other table, always use `machines.id = foreign_table.machine_id`.\n" +
                    "</VOCABULARY_AND_RELATIONS>\n\n" +
                    "<RULES>\n" +
                    "1. Return ONLY the raw SQL string. Do not use markdown code blocks (e.g., do not wrap in ```sql).\n" +
                    "2. ONLY generate SELECT queries. Prohibited: INSERT, UPDATE, DELETE, DROP, TRUNCATE.\n" +
                    "3. Time is in nanoseconds.\n" +
                    "4. Be highly forgiving of typos and spelling mistakes (e.g., 'masages'). Try your best to infer the user's intent.\n" +
                    "5. Return EXACTLY the string 'ERROR_INVALID_CONTEXT' ONLY if the user's prompt is completely unrelated to the Enigma system or data analysis.\n" +
                    "6. CRITICAL COUNTING RULE: NEVER use COUNT(DISTINCT rotor_id) or COUNT(DISTINCT reflector_id). Every row in machines_rotors and machines_reflectors is ALREADY a unique physical component. To answer 'how many unique rotors/reflectors' or 'total rotors', you MUST count the primary keys using COUNT(id) or COUNT(*).\n" +
                    "</RULES>\n\n" +
                    "<EXAMPLES>\n" +
                    "User: how many unique rotors are in the system?\n" +
                    "SQL: SELECT COUNT(id) FROM machines_rotors;\n\n" +
                    "User: what is the longest message processing time for the sanity machine?\n" +
                    "SQL: SELECT MAX(p.time) FROM processing p JOIN machines m ON p.machine_id = m.id WHERE m.name = 'sanity';\n\n" +
                    "User: write me a python script\n" +
                    "SQL: ERROR_INVALID_CONTEXT\n" +
                    "</EXAMPLES>";

    public AIResponseDTO handleAIQuery(String userQuery) {
        // Step 1: Text-to-SQL
        String generatedSql = generateInitialSql(userQuery);
        if ("ERROR_INVALID_CONTEXT".equals(generatedSql)) {
            return new AIResponseDTO(INVALID_CONTEXT_RESPONSE, null);
        }

        List<Map<String, Object>> dbResults;
        String executedSql = generatedSql;

        try {
            dbResults = executeReadOnlySql(generatedSql);
        } catch (Exception firstFailure) {
            String correctedSql = generateCorrectedSql(userQuery, generatedSql, firstFailure);
            if ("ERROR_INVALID_CONTEXT".equals(correctedSql)) {
                return new AIResponseDTO(INVALID_CONTEXT_RESPONSE, null);
            }

            try {
                dbResults = executeReadOnlySql(correctedSql);
                executedSql = correctedSql;
            } catch (Exception secondFailure) {
                return new AIResponseDTO(DB_FETCH_FAILED_RESPONSE, null);
            }
        }

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

        return new AIResponseDTO(finalAnswer, executedSql);
    }

    private String generateInitialSql(String userQuery) {
        String sqlPrompt = SYSTEM_PROMPT + "\nUser Request: " + userQuery;
        return sanitizeSql(callOpenAI(sqlPrompt));
    }

    private String generateCorrectedSql(String userQuery, String failedSql, Exception failure) {
        String correctionPrompt = SYSTEM_PROMPT + "\n" +
                "The previous SQL failed when executed.\n" +
                "Original user request: " + userQuery + "\n" +
                "Failed SQL: " + failedSql + "\n" +
                "Database/validation error: " + Optional.ofNullable(failure.getMessage()).orElse("No error details") + "\n" +
                "Analyze what was wrong and return a corrected SQL query that satisfies the original request. " +
                "Return ONLY raw SQL.";
        return sanitizeSql(callOpenAI(correctionPrompt));
    }

    private List<Map<String, Object>> executeReadOnlySql(String sql) {
        if (!isReadOnlySql(sql)) {
            throw new IllegalArgumentException("Only read-only SELECT queries are allowed.");
        }
        return jdbcTemplate.queryForList(sql);
    }

    private boolean isReadOnlySql(String sql) {
        String normalized = sql == null ? "" : sql.stripLeading().toUpperCase(Locale.ROOT);
        return normalized.startsWith("SELECT") || normalized.startsWith("WITH");
    }

    private String sanitizeSql(String rawSql) {
        return rawSql
                .replaceAll("(?i)```sql", "")
                .replace("```", "")
                .trim();
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
