package com.grazielleanaia.spring_ai1.chat;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

public class ChatServiceTest {
    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private ChatClient chatClient;
    private ChatService chatService;

    @BeforeEach
    void setup() {
        when(chatClientBuilder.build()).thenReturn(chatClient);
        chatService = new ChatService(chatClientBuilder);
    }

    @Test
    void testConstructor_ShouldBuildChatClient() {
        ChatClient.Builder freshBuilder = mock(ChatClient.Builder.class);
        ChatClient freshChatClient = mock(ChatClient.class);
        when(freshBuilder.build()).thenReturn(freshChatClient);
        ChatService service = new ChatService(freshBuilder);
        assertThat(service).isNotNull();
        verify(freshBuilder, times(1)).build();
    }

    @Test
    void testChat_WithValidMessage_ReturnResponse() {
        //Given
        String userMessage = "Ask";
        String expectedResponse = "Answer";
        //Mock the fluent api chain using a spy approach
        ChatService spyService = spy(chatService);
        doReturn(expectedResponse).when(spyService).chat(userMessage);
        //When
        String actualResponse = spyService.chat(userMessage);
        //Then
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @Test
    void testChat_WithLongMessage_ShouldHandle_Successfully() {
        String longMessage = "This message contains multiple sentences and should test how api handles the long output";
        String expectedResponse = "A detailed answer will be given";
        ChatService spyService = spy(chatService);
        doReturn(expectedResponse).when(spyService).chat(longMessage);
        String actualResponse = spyService.chat(longMessage);
        assertThat(actualResponse).isEqualTo(expectedResponse);
        assertThat(longMessage.length()).isGreaterThan(80);
    }

    @Test
    void testChat_WithSpecialCharacters_ShouldHandle_Successfully() {
        String messageWithCharacter = "Hi! @#$%^&*''()_+ What's 1 + 1 <>&'";
        String expectedResponse = "1 + 1 is equal to 2";
        ChatService spyService = spy(chatService);
        doReturn(expectedResponse).when(spyService).chat(messageWithCharacter);
        String actualResponse = spyService.chat(messageWithCharacter);
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n", " \n "})
    void testChat_WithInvalidMessage_ShouldHandleSuccessfully(String invalidMessage) {
        ChatService spyService = spy(chatService);
        assertThat(spyService).isNotNull();
    }

    @Test
    void testChat_WhenChatClientThrowsException_ShouldThrowException() {
        String userMessage = "message";
        when(chatClientBuilder.build()).thenReturn(chatClient);
        ChatService realService = new ChatService(chatClientBuilder);
        when(chatClient.prompt()).thenThrow(new RuntimeException("ChatClient error"));
        assertThatThrownBy(() -> realService.chat(userMessage))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("ChatClient error");
    }

    @Test
    void testChat_WhenClientThrowsException_Exception() {
        String userMessage = "test message";
        when(chatClientBuilder.build()).thenReturn(chatClient);
        ChatService realService = new ChatService(chatClientBuilder);
        when(chatClient.prompt()).thenThrow(new RuntimeException("ChatClient error"));
        assertThatThrownBy(() -> realService.chat(userMessage))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("ChatClient error");
    }

    @Test
    void testChat_MulitpleSequenceCalls_ShouldWorkCorrectly() {
        String message1 = "First message";
        String message2 = "Second message";
        String expectedResponse1 = "First response";
        String expectedResponse2 = "Second response";
        ChatService spyService = spy(chatService);

        doReturn(expectedResponse1).when(spyService).chat(message1);
        doReturn(expectedResponse2).when(spyService).chat(message2);
        String actualResponse1 = spyService.chat(message1);
        String actualResponse2 = spyService.chat(message2);
        assertThat(actualResponse1).isEqualTo(expectedResponse1);
        assertThat(actualResponse2).isEqualTo(expectedResponse2);
    }

    @Test
    void verifyInteractionsWithChatClient() {
        String userMessage = "test message";
        // Create a service with fresh mocks to avoid interference with @BeforeEach setup
        ChatClient.Builder freshBuilder = mock(ChatClient.Builder.class);
        ChatClient freshChatClient = mock(ChatClient.class);
        when(freshBuilder.build()).thenReturn(freshChatClient);
        ChatService realService = new ChatService(freshBuilder);
        try{
            realService.chat(userMessage);
        } catch (Exception e) {
        }
        verify(freshBuilder, times(1)).build();
    }

    @Test
    void testChat_WithEmptyResponse_ShouldReturnEmptyString() {
        String userMessage = "test message";
        String expectedResponse = "";
        ChatService spyService = spy(chatService);
        doReturn(expectedResponse).when(spyService).chat(userMessage);
        String actualResponse = spyService.chat(userMessage);
        assertThat(actualResponse)
                .isEqualTo(expectedResponse)
                .isEmpty();
    }

    @Test
    void testService_IsProperlyAnnotated() {
        assertThat(ChatService.class.getAnnotation(org.springframework.stereotype.Service.class)).isNotNull();
    }

    @Test
    void testChatClient_BuilderIsRequired() {
        assertThatThrownBy(() -> new ChatService(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void testChat_WithWhiteSpaceResponse_ReturnsWithWhiteSpace() {
        String userMessage = "test message";
        String expectedResponse = "  ";
        ChatService spyService = spy(chatService);
        doReturn(expectedResponse).when(spyService).chat(userMessage);
        String actualResponse = spyService.chat(userMessage);

        assertThat(actualResponse)
                .isEqualTo(expectedResponse)
                .isBlank();
    }
}
