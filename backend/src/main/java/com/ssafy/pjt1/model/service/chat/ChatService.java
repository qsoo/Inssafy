package com.ssafy.pjt1.model.service.chat;

import java.io.IOException;
import java.util.List;

import com.ssafy.pjt1.model.dto.chat.ChatMessage;

public interface ChatService {

    public List<ChatMessage> getMessage(int startNum, int endNum, String room_id) throws IOException;

    public void insertMessage(ChatMessage message) throws IOException;
}
