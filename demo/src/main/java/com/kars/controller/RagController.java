package com.kars.controller;

import com.kars.assistant.AiAssistant;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/rag")
public class RagController {

    @Resource(name = "rpgAiAssistant")
    private AiAssistant aiAssistant;

    @Resource(name = "quadrantEmbeddingStore")
    private EmbeddingStore<TextSegment> embeddingStore;

    @Resource
    private EmbeddingModel embeddingModel;

    @GetMapping("/add")
    public String add(){
        List<Document> documents = FileSystemDocumentLoader.loadDocuments("/Users/heizi/work/knowledge");
        EmbeddingStoreIngestor.builder().embeddingModel(embeddingModel)
                        .embeddingStore(embeddingStore).build().
                ingest(documents);
        return "加载成功";
    }

    @GetMapping("/search")
    public Flux<String> search(@RequestParam(name = "prompt") String prompt){
        return aiAssistant.chatFlux(prompt);
    }

}
