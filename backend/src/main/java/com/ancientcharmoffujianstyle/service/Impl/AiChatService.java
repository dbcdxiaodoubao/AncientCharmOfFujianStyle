package com.ancientcharmoffujianstyle.service.Impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ancientcharmoffujianstyle.domain.entity.Fyinfo;
import com.ancientcharmoffujianstyle.domain.entity.UserBrowseHistory;
import com.ancientcharmoffujianstyle.domain.entity.UserFavorite;
import com.ancientcharmoffujianstyle.domain.entity.UserPreference;
import com.ancientcharmoffujianstyle.domain.query.AiMessage;
import com.ancientcharmoffujianstyle.domain.vo.RecommendationVo;
import com.ancientcharmoffujianstyle.mapper.FyinfoMapper;
import com.ancientcharmoffujianstyle.mapper.SysUserMapper;
import com.ancientcharmoffujianstyle.mapper.UserBrowseHistoryMapper;
import com.ancientcharmoffujianstyle.mapper.UserFavoriteMapper;
import com.ancientcharmoffujianstyle.mapper.UserPreferenceMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 古韵闽风 - 福建文旅 AI 智能助手服务
 * 通过提示词将对话限制在福建旅游/非遗/路线话题，
 * 并把用户的偏好、收藏、浏览与系统推荐注入上下文，实现个性化路线推荐。
 */
@Service
public class AiChatService {

    private static final Map<Long, String> CITY_NAMES = new HashMap<>();

    static {
        CITY_NAMES.put(1L, "漳州市");
        CITY_NAMES.put(2L, "厦门市");
        CITY_NAMES.put(3L, "泉州市");
        CITY_NAMES.put(4L, "莆田市");
        CITY_NAMES.put(5L, "福州市");
        CITY_NAMES.put(6L, "宁德市");
        CITY_NAMES.put(7L, "南平市");
        CITY_NAMES.put(8L, "三明市");
        CITY_NAMES.put(9L, "龙岩市");
    }

    private static final String SYSTEM_PROMPT =
            "你是「古韵闽风」福建文旅智能助手，服务于福建省的非物质文化遗产与旅游出行。\n"
            + "请严格遵守以下规则：\n"
            + "1. 只回答与福建省旅游、非遗、景点、美食、城市以及路线规划相关的问题。\n"
            + "2. 当用户询问与福建文旅无关的内容时，礼貌拒绝，并用一句话引导回福建非遗或旅游话题。\n"
            + "3. 当用户请求推荐路线时，请给出城市顺序，并说明每个城市可体验的非遗项目，尽量结合下方提供的用户画像。\n"
            + "4. 回答使用中文，简洁友好、分点清晰，不要输出代码。";

    private static final int MAX_HISTORY_MESSAGES = 10;
    private static final int MAX_HISTORY_LENGTH = 500;
    private static final int MAX_CONTEXT_ITEMS = 6;

    @Value("${ai.api-url:https://open.bigmodel.cn/api/paas/v4/chat/completions}")
    private String apiUrl;

    @Value("${ai.api-key:}")
    private String apiKey;

    @Value("${ai.model:glm-4-flash}")
    private String model;

    @Autowired
    private FyinfoMapper fyinfoMapper;

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private UserPreferenceMapper preferenceMapper;

    @Autowired
    private UserFavoriteMapper favoriteMapper;

    @Autowired
    private UserBrowseHistoryMapper browseHistoryMapper;

    @Autowired
    private HeritageRecommendationService recommendationService;

    private final RestTemplate restTemplate = buildRestTemplate();

    /** AI 服务是否已配置 API Key */
    public boolean isConfigured() {
        return apiKey != null && !apiKey.trim().isEmpty();
    }

    /**
     * 组装发给大模型的完整消息列表：系统提示词 + 历史对话 + 带用户画像的本轮问题
     */
    public List<Map<String, Object>> buildMessages(Long userId, String message, List<AiMessage> history) {
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("请输入内容");
        }
        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(message("system", SYSTEM_PROMPT + "\n\n【当前用户画像】\n" + buildUserContext(userId)
                + "\n请在推荐路线、非遗项目或回答旅游问题时优先结合以上用户画像；若画像为空，请主动引导用户登录或询问其偏好城市与兴趣类别。"));
        if (history != null) {
            int start = Math.max(0, history.size() - MAX_HISTORY_MESSAGES);
            for (int i = start; i < history.size(); i++) {
                AiMessage item = history.get(i);
                if (item == null || item.getContent() == null || item.getContent().trim().isEmpty()) {
                    continue;
                }
                String role = "assistant".equals(item.getRole()) ? "assistant" : "user";
                messages.add(message(role, truncate(item.getContent(), MAX_HISTORY_LENGTH)));
            }
        }
        messages.add(message("user", message));
        return messages;
    }

    /**
     * 流式调用智谱大模型，每收到一段增量文本就回调 onDelta
     */
    public void streamChat(List<Map<String, Object>> messages, Consumer<String> onDelta) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        body.put("stream", true);
        byte[] payload = JSON.toJSONBytes(body);

        restTemplate.execute(apiUrl, HttpMethod.POST, request -> {
            request.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            request.getHeaders().setBearerAuth(apiKey.trim());
            request.getBody().write(payload);
        }, response -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.startsWith("data:")) {
                        continue;
                    }
                    String data = line.substring(5).trim();
                    if (data.isEmpty()) {
                        continue;
                    }
                    if ("[DONE]".equals(data)) {
                        break;
                    }
                    String delta = extractDelta(data);
                    if (delta != null && !delta.isEmpty()) {
                        onDelta.accept(delta);
                    }
                }
            }
            return null;
        });
    }

    private String extractDelta(String payload) {
        try {
            JSONObject json = JSON.parseObject(payload);
            JSONArray choices = json.getJSONArray("choices");
            if (choices == null || choices.isEmpty()) {
                return null;
            }
            JSONObject delta = choices.getJSONObject(0).getJSONObject("delta");
            if (delta == null) {
                return null;
            }
            return delta.getString("content");
        } catch (Exception exception) {
            return null;
        }
    }

    private String buildUserContext(Long userId) {
        if (userId == null || userId <= 0) {
            return "暂无（游客未登录）";
        }
        if (userMapper.selectById(userId) == null) {
            return "暂无（用户不存在）";
        }
        StringBuilder builder = new StringBuilder();
        UserPreference preference = preferenceMapper.selectById(userId);
        if (preference != null) {
            if (preference.getCity() != null) {
                builder.append("偏好城市：").append(cityName(preference.getCity())).append("\n");
            }
            if (hasText(preference.getCategory())) {
                builder.append("偏好非遗类别：").append(preference.getCategory()).append("\n");
            }
        }

        List<UserFavorite> favorites = favoriteMapper.selectList(
                new LambdaQueryWrapper<UserFavorite>().eq(UserFavorite::getUserId, userId));
        List<Fyinfo> favoriteItems = loadFyinfo(collectFyIds(favorites));
        if (!favoriteItems.isEmpty()) {
            builder.append("收藏的非遗：").append(describeAll(favoriteItems)).append("\n");
        }

        List<UserBrowseHistory> histories = browseHistoryMapper.selectRecent30DaysByUserId(userId);
        List<Fyinfo> browseItems = loadFyinfo(collectBrowseFyIds(histories));
        if (!browseItems.isEmpty()) {
            builder.append("近期浏览的非遗：").append(describeAll(browseItems)).append("\n");
        }

        List<RecommendationVo> recommendations = recommendationService.recommend(userId);
        if (recommendations != null && !recommendations.isEmpty()) {
            builder.append("系统优先推荐：").append(describeAll(recommendations)).append("\n");
        }

        if (builder.length() == 0) {
            return "暂无（用户暂无行为数据）";
        }
        return builder.toString().trim();
    }

    private List<Long> collectFyIds(List<UserFavorite> favorites) {
        List<Long> ids = new ArrayList<>();
        if (favorites != null) {
            for (UserFavorite favorite : favorites) {
                if (favorite != null && favorite.getFyId() != null) {
                    ids.add(favorite.getFyId());
                }
            }
        }
        return ids;
    }

    private List<Long> collectBrowseFyIds(List<UserBrowseHistory> histories) {
        Set<Long> ids = new LinkedHashSet<>();
        if (histories != null) {
            for (UserBrowseHistory history : histories) {
                if (history != null && history.getFyId() != null) {
                    ids.add(history.getFyId());
                }
            }
        }
        return new ArrayList<>(ids);
    }

    private List<Fyinfo> loadFyinfo(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        List<Fyinfo> items = fyinfoMapper.selectBatchIds(ids);
        return items == null ? Collections.emptyList() : items;
    }

    private String describeAll(List<?> items) {
        if (items == null || items.isEmpty()) {
            return "";
        }
        List<String> parts = new ArrayList<>();
        for (Object item : items) {
            if (item instanceof Fyinfo) {
                Fyinfo fyinfo = (Fyinfo) item;
                parts.add(describe(fyinfo.getName(), fyinfo.getCity(), fyinfo.getType()));
            } else if (item instanceof RecommendationVo) {
                RecommendationVo vo = (RecommendationVo) item;
                parts.add(describe(vo.getName(), vo.getCity(), vo.getType()));
            }
            if (parts.size() >= MAX_CONTEXT_ITEMS) {
                break;
            }
        }
        return String.join("、", parts);
    }

    private String describe(String name, Long city, String type) {
        StringBuilder builder = new StringBuilder(hasText(name) ? name : "未知项目");
        List<String> tags = new ArrayList<>();
        if (city != null) {
            tags.add(cityName(city));
        }
        if (hasText(type)) {
            tags.add(type);
        }
        if (!tags.isEmpty()) {
            builder.append("（").append(String.join("·", tags)).append("）");
        }
        return builder.toString();
    }

    private String cityName(Long city) {
        String name = CITY_NAMES.get(city);
        return name == null ? city + "号城市" : name;
    }

    private Map<String, Object> message(String role, String content) {
        Map<String, Object> message = new HashMap<>();
        message.put("role", role);
        message.put("content", content);
        return message;
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() <= maxLength ? trimmed : trimmed.substring(0, maxLength);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static RestTemplate buildRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(60000);
        factory.setReadTimeout(120000);
        return new RestTemplate(factory);
    }
}
