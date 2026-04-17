package com.github.stepwise.service;

import java.io.InputStream;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import chat.giga.client.GigaChatClient;
import chat.giga.http.client.HttpClientException;
import chat.giga.model.ModelName;
import chat.giga.model.completion.ChatMessage;
import chat.giga.model.completion.ChatMessageRole;
import chat.giga.model.completion.CompletionRequest;
import chat.giga.model.completion.CompletionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class GigaChatService {

    private final StorageService storageService;

    private final GigaChatClient client;

    private static final int MAX_TEXT_LENGTH = 7000;

    public String summarizeReport(Long studentId, Long projectId, Long itemId, Long historyId, String filename) {
        log.info("Summarizing report for itemId: {}, historyId: {}", itemId, historyId);

        String pdfText = extractTextFromPdf(studentId, projectId, itemId, historyId, filename);
        return callGigaChat(pdfText);
    }

    private String extractTextFromPdf(Long studentId, Long projectId, Long itemId, Long historyId, String filename) {
        try (InputStream is = storageService.downloadExplanatoryFile(studentId, projectId, itemId, historyId, filename);
                PDDocument document = Loader.loadPDF(is.readAllBytes())) {

            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document).trim();

            if (text.length() > MAX_TEXT_LENGTH) {
                log.warn("PDF text truncated from {} to {} chars", text.length(), MAX_TEXT_LENGTH);
                text = text.substring(0, MAX_TEXT_LENGTH);
            }

            return text;
        } catch (Exception e) {
            log.error("Failed to extract text from PDF", e);
            throw new RuntimeException("Cannot extract text from PDF: " + e.getMessage());
        }
    }

    private String callGigaChat(String pdfText) {
        String prompt = """
                Ты — помощник преподавателя. Тебе дан текст пункта пояснительной записки студента.
                Сделай краткий структурированный пересказ: основная тема, ключевые разделы, выводы.
                Объём пересказа — 3–5 абзацев. Отвечай на русском языке.

                Текст отчёта:
                """ + pdfText;

        CompletionResponse res = null;

        try {
            res = client.completions(CompletionRequest.builder()
                    .model(ModelName.GIGA_CHAT_MAX_2)
                    .message(ChatMessage.builder()
                            .content(prompt)
                            .role(ChatMessageRole.USER)
                            .build())
                    .build());
        } catch (HttpClientException ex) {
            System.out.println(ex.statusCode() + " " + ex.bodyAsString());
        }

        if (res == null || res.choices() == null || res.choices().isEmpty()) {
            throw new RuntimeException("Пустой ответ от GigaChat");
        }

        return res.choices().get(0).message().content();

    }

}
