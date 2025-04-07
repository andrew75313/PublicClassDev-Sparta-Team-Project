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

public class JavaScriptCodeRunner implements CodeRunner {

    @Value("${code-runner.javascript.url.url}")
    private String javascriptRunnerUrl;

    @Override
    public String runCode(String code) {
        validateCode(code);
        return runInDockerContainer(code);
    }

    private void validateCode(String code) {
        if (code.contains("require('child_process')") || code.contains("process.exit") || code.contains("fs.") || code.contains("require('fs')")) {
            throw new CustomException(ErrorCode.INVALID_CODE);
        }
    }

    private String runInDockerContainer(String code) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonBody = objectMapper.writeValueAsString(Map.of("code", code));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(javascriptRunnerUrl))
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
