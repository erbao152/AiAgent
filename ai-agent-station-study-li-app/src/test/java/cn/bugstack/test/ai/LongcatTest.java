package cn.bugstack.test.ai;

import cn.bugstack.domain.agent.adapter.repository.IAgentRepository;
import cn.bugstack.domain.agent.model.valobj.AiClientToolMcpVO;
import cn.bugstack.infrastructure.dao.IAiClientToolMcpDao;
import cn.bugstack.infrastructure.dao.po.AiClientToolMcp;
import com.alibaba.fastjson.JSON;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author erbao
 * @description:
 * @Date 2026/4/23 20:39
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class LongcatTest {
    @Resource
    private IAgentRepository repository;

    @Autowired
    private PgVectorStore pgVectorStore;
    @Test
    public void LongcatTest(){
        log.info("开始构建api");
        OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl("https://api.deepseek.com")
                .apiKey("sk-8b71b4cf2f6b41c89f5cc4ba25538ee6")
                .completionsPath("v1/chat/completions")
                .embeddingsPath("v1/embeddings")
                .build();

        log.info("开始构建model");

        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("deepseek-v4-pro")
                        .build())
                .build();

        log.info("开始构建client");
        ChatClient chatClient = ChatClient.builder(chatModel)
                .defaultSystem("回答问题，并且在问题结尾换行打印一句“hello！erbao”")
                .build();

        log.info("开始使用client");
        String res = chatClient.prompt()
                .user("1+1=?")
                .call()
                .content();
        log.info("结果为: {}",res);
    }

    @Test
    public void chat() {
        log.info("开始构建api");
        OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl("https://api.longcat.chat/openai/")
                .apiKey("ak_2V04Kv6qU7pu5nD5qi7AE8lJ27K4X")
                .completionsPath("v1/chat/completions")
                .embeddingsPath("v1/embeddings")
                .build();

        log.info("开始构建model");

        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("LongCat-Flash-Thinking-2601")
                        .build())
                .build();

        String message = "王大瓜今年几岁";

        String SYSTEM_PROMPT = """
                Use the information from the DOCUMENTS section to provide accurate answers but act as if you knew this information innately.
                If unsure, simply state that you don't know.
                Another thing you need to note is that your reply must be in Chinese!
                DOCUMENTS:
                    {documents}
                """;

        SearchRequest request = SearchRequest.builder()
                .query(message)
                .topK(5)
                .filterExpression("knowledge == '知识库名称-v4'")
                .build();

        List<Document> documents = pgVectorStore.similaritySearch(request);

        String documentsCollectors = null == documents ? "" : documents.stream().map(Document::getText).collect(Collectors.joining());

        Message ragMessage = new SystemPromptTemplate(SYSTEM_PROMPT).createMessage(Map.of("documents", documentsCollectors));

        ArrayList<Message> messages = new ArrayList<>();
        messages.add(new UserMessage(message));
        messages.add(ragMessage);

        ChatResponse chatResponse = chatModel.call(new Prompt(
                messages,
                OpenAiChatOptions.builder()
                        .model("LongCat-Flash-Thinking-2601")
                        .build()));

        log.info("测试结果:{}", JSON.toJSONString(chatResponse));
    }
    @Test
    public void mcp_test(){
        log.info("开始构建api");
        OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl("https://api.longcat.chat/openai/")
                .apiKey("ak_2V04Kv6qU7pu5nD5qi7AE8lJ27K4X")
                .completionsPath("v1/chat/completions")
                .embeddingsPath("v1/embeddings")
                .build();

        List<AiClientToolMcpVO> aiClientToolMcpVOS = repository.AiClientToolMcpVOByModelIds(Collections.singletonList("8001"));
        List<McpSyncClient> mcpSyncClients = new ArrayList<>();
        for (AiClientToolMcpVO aiClientToolMcpVO : aiClientToolMcpVOS) {
            McpSyncClient mcpSyncClient = createMcpSyncClient(aiClientToolMcpVO);
            mcpSyncClients.add(mcpSyncClient);
        }
        log.info("开始构建mcp工具");
        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("LongCat-Flash-Thinking-2601")
                        .toolCallbacks(new SyncMcpToolCallbackProvider(mcpSyncClients).getToolCallbacks())
                        .build())
                .build();

        String userInput = """
                我需要你帮我生成一篇文章，要求如下；
                                
                1. 场景为互联网大厂java求职者面试
                2. 面试管提问 Java 核心知识、JUC、JVM、多线程、线程池、HashMap、ArrayList、Spring、SpringBoot、MyBatis、Dubbo、RabbitMQ、xxl-job、Redis、MySQL、Linux、Docker、设计模式、DDD等不限于此的各项技术问题。
                3. 按照故事场景，以严肃的面试官和搞笑的水货程序员谢飞机进行提问，谢飞机对简单问题可以回答，回答好了面试官还会夸赞。复杂问题胡乱回答，回答的不清晰。
                4. 每次进行3轮提问，每轮可以有3-5个问题。这些问题要有技术业务场景上的衔接性，循序渐进引导提问。最后是面试官让程序员回家等通知类似的话术。
                5. 提问后把问题的答案，写到文章最后，最后的答案要详细讲述出技术点，让小白可以学习下来。
                                
                根据以上内容，不要阐述其他信息，请直接提供；文章标题、文章内容、文章标签（多个用英文逗号隔开）、文章简述（100字）
                                
                将以上内容发布文章到CSDN。     
                                
                之后进行，微信公众号消息通知，平台：CSDN、主题：为文章标题、描述：为文章简述、跳转地址：从发布文章到CSDN获取 url
                
                使用mcp工具时，请严格按照mcp工具所需的json格式调用
                """;

        ChatClient chatClient = ChatClient.builder(chatModel)
                .build();
        String content = chatClient
                .prompt(userInput)
                .call()
                .content();
        System.out.println(content);
    }




    private McpSyncClient createMcpSyncClient(AiClientToolMcpVO aiClientToolMcpVO){
        String transportType = aiClientToolMcpVO.getTransportType();

        // 判断类型
        switch (transportType){
            case "sse" -> {
                AiClientToolMcpVO.TransportConfigSse transportConfigSse = aiClientToolMcpVO.getTransportConfigSse();
                // http://127.0.0.1:9999/sse?apikey=DElk89iu8Ehhnbu
                // 由baseUri + sseEndpoint 组成
                String originalBaseUri = transportConfigSse.getBaseUri();
                String baseUri;
                String sseEndpoint;

                int queryParamStartIndex = originalBaseUri.indexOf("sse");
                if (queryParamStartIndex != -1){
                    baseUri = originalBaseUri.substring(0,queryParamStartIndex - 1);
                    sseEndpoint = originalBaseUri.substring(queryParamStartIndex - 1);
                }else {
                    baseUri = originalBaseUri;
                    sseEndpoint = transportConfigSse.getSseEndpoint();
                }

                sseEndpoint = StringUtils.isBlank(sseEndpoint) ? "/sse" : sseEndpoint;

                HttpClientSseClientTransport clientSseClientTransport = HttpClientSseClientTransport
                        .builder(baseUri)
                        .sseEndpoint(sseEndpoint)
                        .build();

                McpSyncClient mcpSyncClient = McpClient.sync(clientSseClientTransport).requestTimeout(Duration.ofMinutes(aiClientToolMcpVO.getRequestTimeout())).build();

                var init = mcpSyncClient.initialize();
                System.out.println("SSE MCP Initialized: " + init);

                return mcpSyncClient;
            }
            case "stdio" -> {
                AiClientToolMcpVO.TransportConfigStdio transportConfigStdio = aiClientToolMcpVO.getTransportConfigStdio();
                Map<String, AiClientToolMcpVO.TransportConfigStdio.Stdio> stdioMap = transportConfigStdio.getStdio();
                AiClientToolMcpVO.TransportConfigStdio.Stdio stdio = stdioMap.get(aiClientToolMcpVO.getMcpName());

                var stdioParams = ServerParameters.builder(stdio.getCommand())
                        .args(stdio.getArgs())
                        .env(stdio.getEnv())
                        .build();

                var mcpClient = McpClient.sync(new StdioClientTransport(stdioParams))
                        .requestTimeout(Duration.ofSeconds(aiClientToolMcpVO.getRequestTimeout())).build();
                var init_stdio = mcpClient.initialize();

                log.info("Tool Stdio MCP Initialized {}", init_stdio);
                return mcpClient;
            }
        }

        throw new RuntimeException("err! transportType " + transportType + " not exist!");

    }
}
