package com.kars.controller;

import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.UserMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mcp")
public class McpController {

    private static final String NPX_COMMAND = System.getenv().getOrDefault("MCP_NPX_COMMAND", "npx");

    private final StreamingChatModel streamingChatModel;

    public McpController(@Qualifier("streamingChatModel") StreamingChatModel streamingChatModel) {
        this.streamingChatModel = streamingChatModel;
    }

    @GetMapping("/chat")
    public Flux<String> chat(@RequestParam("question") String question) {
        String baiduMapApiKey = System.getenv("BAIDU_MAP_API_KEY");
        if (baiduMapApiKey == null || baiduMapApiKey.isBlank()) {
            return Flux.error(new IllegalStateException("BAIDU_MAP_API_KEY is not configured"));
        }

        McpTransport transport = new StdioMcpTransport.Builder()
                .command(List.of(NPX_COMMAND, "-y", "@baidumap/mcp-server-baidu-map"))
                .environment(Map.of("BAIDU_MAP_API_KEY", baiduMapApiKey))
                .logEvents(true)
                .build();

        McpClient mcpClient = new DefaultMcpClient.Builder()
                .transport(transport)
                .build();

        McpToolProvider toolProvider = McpToolProvider.builder()
                .mcpClients(mcpClient)
                .build();

        McpAssistant assistant = AiServices.builder(McpAssistant.class)
                .streamingChatModel(streamingChatModel)
                .toolProvider(toolProvider)
                .build();

        return assistant.chat(question)
                .doFinally(signalType -> {
                    try {
                        mcpClient.close();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    interface McpAssistant {
        Flux<String> chat(@UserMessage String question);
    }

}
