package com.ancientcharmoffujianstyle.controller;

import com.alibaba.fastjson2.JSON;
import com.ancientcharmoffujianstyle.controller.base.WebController;
import com.ancientcharmoffujianstyle.domain.query.AiChatQuery;
import com.ancientcharmoffujianstyle.domain.query.AiMessage;
import com.ancientcharmoffujianstyle.service.Impl.AiChatService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/ai")
@Api(tags = "AI智能助手")
public class AiController extends WebController {

    private static final long STREAM_TIMEOUT = 180000L;

    @Autowired
    private AiChatService aiChatService;

    private final ExecutorService streamExecutor = Executors.newCachedThreadPool();

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ApiOperation("福建文旅AI流式对话")
    public SseEmitter chat(@RequestBody AiChatQuery query) {
        SseEmitter emitter = new SseEmitter(STREAM_TIMEOUT);

        Long userId = query == null ? null : query.getUserId();
        String message = query == null ? null : query.getMessage();
        List<AiMessage> history = query == null ? null : query.getHistory();

        if (message == null || message.trim().isEmpty()) {
            sendText(emitter, "请输入你想咨询的福建旅游或非遗问题。");
            return emitter;
        }
        if (!aiChatService.isConfigured()) {
            sendText(emitter, "AI 服务尚未配置，请在 application.yml 的 ai.api-key 中填入智谱 API Key。");
            return emitter;
        }

        streamExecutor.submit(() -> {
            try {
                List<Map<String, Object>> messages = aiChatService.buildMessages(userId, message, history);
                aiChatService.streamChat(messages, delta -> sendChunk(emitter, delta));
                sendDone(emitter);
                emitter.complete();
            } catch (Exception exception) {
                sendText(emitter, "AI 服务调用失败：" + exception.getMessage());
            }
        });
        return emitter;
    }

    private void sendChunk(SseEmitter emitter, String delta) {
        try {
            emitter.send(SseEmitter.event().data(JSON.toJSONString(Collections.singletonMap("c", delta))));
        } catch (IOException exception) {
            throw new IllegalStateException("连接已断开", exception);
        }
    }

    private void sendDone(SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event().data(JSON.toJSONString(Collections.singletonMap("done", true))));
        } catch (IOException exception) {
            throw new IllegalStateException("连接已断开", exception);
        }
    }

    private void sendText(SseEmitter emitter, String text) {
        try {
            emitter.send(SseEmitter.event().data(JSON.toJSONString(Collections.singletonMap("c", text))));
            emitter.send(SseEmitter.event().data(JSON.toJSONString(Collections.singletonMap("done", true))));
            emitter.complete();
        } catch (IOException exception) {
            emitter.completeWithError(exception);
        }
    }
}
