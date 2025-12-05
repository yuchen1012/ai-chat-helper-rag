package com.ai.helper.controllers;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(RagController.BASE_URL)
public class RagController {
  final static String BASE_URL = "/rag";

  private final VectorStore store;

  public RagController(VectorStore store) {
    this.store = store;
  }

  @PostMapping("/import")
  public String add(@RequestParam("data") String data) {
    Document document = Document.builder().text(data).build();
    store.add(List.of(document));
    return "success";
  }

  @PostMapping("/search")
  public List<Document> search(@RequestParam("query") String query) {
    SearchRequest searchRequest = SearchRequest.builder().query(query).topK(2).build();
    return store.similaritySearch(searchRequest);
  }
}
