package com.kars.prompt;

import dev.langchain4j.model.input.structured.StructuredPrompt;
import lombok.Data;

@Data
@StructuredPrompt("帮我分析下这个股票：{{name}},今天的走势，以及它未来{{num}}天的趋势走向")
public class StockPrompt {

    private String name;

    private Integer num;

}
