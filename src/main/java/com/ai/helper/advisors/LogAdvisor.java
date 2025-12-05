package com.ai.helper.advisors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.messages.Message;

import java.util.*;

@Slf4j
public class LogAdvisor implements BaseAdvisor {

  private static final Map<String, List<Message>> memory = new HashMap<>();

  @Override
  public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
    log.info("call model chat.");
    return chatClientRequest;
  }

  @Override
  public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
    log.info("call end.");
    return chatClientResponse;
  }

  @Override
  public int getOrder() {
    return 0;
  }
}
