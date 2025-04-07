package com.sparta.publicclassdev.domain.coderuns.runner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.publicclassdev.global.exception.CustomException;
import com.sparta.publicclassdev.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;


public class JavaCodeRunner implements CodeRunner {

    @Value("${code-runner.java.url}")
    private String javaRunnerUrl;
    
    @Override
    public String runCode(String code) {
        return runInDockerContainer(code);
    }
    
    private void validateCode(String code) {
        if (code.contains("System.exit") || code.contains("Runtime.getRuntime().exec") || code.contains("java.io.File")) {
            throw new CustomException(ErrorCode.INVALID_CODE);
        }
    }

    private String runInDockerContainer(String code) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonBody = objectMapper.writeValueAsString(Map.of("code", code));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(javaRunnerUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return response.body();

        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }
}
