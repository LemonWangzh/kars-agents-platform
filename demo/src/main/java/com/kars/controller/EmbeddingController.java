package com.kars.controller;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import dev.langchain4j.store.embedding.filter.MetadataFilterBuilder;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Collections;
import jakarta.annotation.Resource;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("vector")
public class EmbeddingController {


    @Resource
    private EmbeddingModel embeddingModel;

    @Resource
    private QdrantClient qdrantClient;

    @Resource
    private EmbeddingStore<TextSegment> embeddingStore;

    private final String sourceData1 = """
                投机者的第一课，是学会等待。钱不是靠频繁操作赚来的，而是靠坐着等来的。
                我赚大钱从来不是因为我的想法，而是因为我坐得住。看对了方向的人很多，能拿住不动的人极少。
                市场只有一个方向，不是多头，也不是空头，而是正确的方向。别跟报价单争辩，别跟大盘讲道理——顺势而为，错了就认，快刀斩乱麻。
                华尔街没有新鲜事，今天发生的，过去发生过，将来还会发生。人性不变，投机永不消亡。
                记住：保住本金，比赚到钱更重要。活得够久，市场总会给你机会。
            """;

    @GetMapping("/embed")
    public String embed(){
        return embeddingModel.embed(sourceData1).content().toString();
    }

    @GetMapping("/create/collection")
    public void create(){
        Collections.VectorParams vectorParams = Collections.VectorParams.newBuilder().setDistance(Collections.Distance.Cosine)
                .setSize(1024).build();
        qdrantClient.createCollectionAsync("qdrant-test", vectorParams);
    }


    @GetMapping("/add")
    public String add(){
        TextSegment segment = TextSegment.from(sourceData1);
        segment.metadata().put("author", "Jesse Lauriston Livermore");
        return embeddingStore.add(embeddingModel.embed(sourceData1).content(), segment);
    }

    @GetMapping("/query1")
    public String query1(){
        Embedding embedding = embeddingModel.embed("Jesse Lauriston Livermore 说了什么").content();
        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(embedding).build();
        EmbeddingSearchResult<TextSegment> search = embeddingStore.search(request);
        if (CollectionUtils.isEmpty(search.matches())){
            return "empty search...";
        }
        System.out.println(search.matches());
        System.out.println(search.matches().get(0));
        return search.matches().get(0).embedded().text();
    }

    @GetMapping("/query2")
    public String query2(){
        Embedding embedding = embeddingModel.embed("Jesse Lauriston Livermore 说了什么").content();
        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(embedding).filter(MetadataFilterBuilder.metadataKey("author").isEqualTo("Jesse Lauriston Livermore")).build();
        EmbeddingSearchResult<TextSegment> search = embeddingStore.search(request);
        if (CollectionUtils.isEmpty(search.matches())){
            return "empty search...";
        }
        System.out.println(search.matches());
        System.out.println(search.matches().get(0));
        return search.matches().get(0).embedded().text();
    }


}
