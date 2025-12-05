package com.ai.helper.controllers;

import com.ai.helper.advisors.LogAdvisor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(CoffeeController.BASE_URL)
public class CoffeeController {
  final static String BASE_URL = "/coffee";

  private final VectorStore vectorStore;
  private final ChatClient chatClient;

  public CoffeeController(VectorStore vectorStore, ChatClient.Builder builder, ToolCallbackProvider provider) {
    this.vectorStore = vectorStore;
    VectorStoreDocumentRetriever documentRetriever = VectorStoreDocumentRetriever.builder()
      .vectorStore(vectorStore)
      .topK(3)
      .similarityThreshold(0.5)
      .build();
    RetrievalAugmentationAdvisor retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor.builder()
      .documentRetriever(documentRetriever)
      .build();
    this.chatClient = builder
      .defaultAdvisors(retrievalAugmentationAdvisor)
      .defaultAdvisors(new LogAdvisor())
      .defaultToolCallbacks(provider.getToolCallbacks())
//      .defaultTools(new TimeTool())
      .build();
  }

  @PostMapping("/import")
  public String add() {
    try {
      // 读取classpath下的QA.csv文件
      ClassPathResource resource = new ClassPathResource("QA.csv");
      InputStreamReader reader = new InputStreamReader(resource.getInputStream());
      // 使用Apache Commons CSV解析CSV文件
      CSVParser csvParser = CSVFormat.DEFAULT
        .builder()
        .setHeader() // 第一行作为标题
        .setSkipHeaderRecord(true) // 跳过标题行
        .build()
        .parse(reader);
      List<Document> documents = new ArrayList<>();
      // 遍历每一行记录
      for (CSVRecord record : csvParser) {
        // 获取问题和回答字段
        String question = record.get("问题");
        String answer = record.get("回答");
        // 将问题和回答组合成文档内容
        String content = "问题: " + question + "\n回答: " + answer;
        // 创建Document对象
        Document document = new Document(content);
        // 添加到文档列表
        documents.add(document);
      }
      // 关闭解析器
      csvParser.close();
      // 将文档存入向量数据库
      vectorStore.add(documents);
      return "成功导入 " + documents.size() + " 条记录到向量数据库";
    } catch (IOException e) {
      e.printStackTrace();
      return "导入失败: " + e.getMessage();
    }
  }

  @PostMapping("/search")
  public List<Document> search(@RequestParam("query") String query) {
    SearchRequest searchRequest = SearchRequest.builder().query(query).similarityThreshold(0.88).topK(2).build();
    return vectorStore.similaritySearch(searchRequest);
  }

  @GetMapping("/rag-ask")
  public String askWithRag(@RequestParam("query") String query) {
    return chatClient.prompt()
      .system("你是咖啡店的服务员，你需要回答客户的问题。遇到时间相关的问题，可以使用工具来获取当前时间。")
      .user(query)
      .call().content();
  }

  @GetMapping("/fetcher-ask")
  public String askWithFetcher(@RequestParam("query") String query) {
    return chatClient.prompt()
      .system("你是一个网页爬取专家，你可以运用工具爬取指定网页的内容并进行总结。")
      .user(query)
      .call().content();
  }
}
