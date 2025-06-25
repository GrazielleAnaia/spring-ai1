package com.grazielleanaia.spring_ai1.chat;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class OpenAIChatControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ChatService chatService;

    @Mock
    private OpenAIChatController controller;

    private ObjectMapper objectMapper;
    private String url;

    @BeforeEach
    void setup() {
        controller = new OpenAIChatController(chatService);
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(controller).alwaysDo(print()).build();
        url = "/api/openai";
    }

    @Test
    void chatChatWithNullMessage() throws Exception {
        ChatRequest request = new ChatRequest(null);
        String aiResponse = "No message is provided";
        when(chatService.chat(null)).thenReturn(aiResponse);
        mockMvc.perform(post(url + "/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(aiResponse));
        verify(chatService, times(1)).chat(null);
    }

    @ParameterizedTest
    @CsvSource({ "'Hello, how are you?', 'I''m doing great, thank you for asking!'",
            "'', 'I didn''t receive any message.'",
            "'What is Spring Boot?', 'Spring Boot is a framework that makes it easy to create stand-alone, production-grade Spring based Applications.'"})
    void testChatWithDifferentMessages(String userMessage, String expectedResponse) throws Exception {
        ChatRequest request = new ChatRequest(userMessage);
        when (chatService.chat(userMessage)).thenReturn(expectedResponse);

        mockMvc.perform(post(url + "/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(expectedResponse));
        verify(chatService, times(1)).chat(userMessage);
    }

    @Test
    void chatWithLongMessage() throws Exception {
        String userMessage = "This is a very long message that contains multiple sentences and covers various topics" +
         "It is designed to test how chat controller handles long input messages and ensures that." +
         "the system processes them without any issue";
        String aiResponse = " Thank you for your detailed longer input";
        ChatRequest request = new ChatRequest(userMessage);
        when(chatService.chat(userMessage)).thenReturn(aiResponse);
        mockMvc.perform(post(url + "/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(aiResponse));
        verify(chatService, times(1)).chat(userMessage);
    }

    @Test
    void chatWithSpecialCharacterMessage() throws Exception {
        String userMessage = "Can you handle message with special character? @#$/?%^&*()";
        String aiResponse = "This output can handle messages having special charaters";
        ChatRequest request = new ChatRequest(userMessage);
        when(chatService.chat(userMessage)).thenReturn(aiResponse);
        mockMvc.perform(post(url + "/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(aiResponse));
        verify(chatService, times(1)).chat(userMessage);
    }

    @ParameterizedTest
    @ValueSource(strings = {"{invalid json}", "{\"message\": }"})
    void testChatWith_InvalidJsonRequest(String invalidJson) throws Exception {
        mockMvc.perform(post(url + "/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
        verify(chatService, never()).chat(anyString());
    }

    @Test
    void testChat_WithMissingContentType() throws Exception {
        String userMessage = "Hi";
        ChatRequest request = new ChatRequest(userMessage);
        mockMvc.perform(post(url + "/chat")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnsupportedMediaType());
        verify(chatService, never()).chat(anyString());
    }

    @Test
    void testChat_WithWrongHttpMethod() throws Exception {
        mockMvc.perform(get(url +"/chat"))
                .andExpect(status().isMethodNotAllowed());
        verify(chatService, never()).chat(anyString());
    }

    @Test
    void testChat_ResponseStructure() throws Exception {
        String userMessage = "Structure test";
        String aiResponse = "Response for structure test";
        ChatRequest request = new ChatRequest(userMessage);
        when(chatService.chat(userMessage)).thenReturn(aiResponse);

        mockMvc.perform(post(url + "/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.message").value(aiResponse));
        verify(chatService, times(1)).chat(userMessage);
    }






}
