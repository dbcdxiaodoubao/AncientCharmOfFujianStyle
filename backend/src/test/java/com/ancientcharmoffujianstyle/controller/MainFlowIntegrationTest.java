package com.ancientcharmoffujianstyle.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.sql.Timestamp;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Real Spring Security, MyBatis and an initially empty demo database; no mocked business services. */
@SpringBootTest(properties = {"upload.path=target/test-uploads",
        "spring.datasource.url=jdbc:h2:mem:main_flow;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1"})
@AutoConfigureMockMvc
@ActiveProfiles("demo")
class MainFlowIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate jdbc;

    private JsonNode registerAndLogin(String name) throws Exception {
        String body = "{\"userName\":\"" + name + "\",\"password\":\"testpass12\"}";
        mvc.perform(post("/sysuser/register").contentType("application/json").content(body))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200));
        MvcResult login = mvc.perform(post("/sysuser/login").contentType("application/json").content(body))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.token").isString()).andReturn();
        return json.readTree(login.getResponse().getContentAsString()).get("data");
    }

    @Test
    void anonymousReadsAndInvalidRoutesAreHandled() throws Exception {
        mvc.perform(get("/map")).andExpect(status().isOk()).andExpect(jsonPath("$.data.length()").value(10));
        mvc.perform(get("/recommendation")).andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200));
        mvc.perform(get("/favorite/1")).andExpect(status().isUnauthorized());
        for (String body : new String[]{"{}", "{\"start\":0,\"end\":2}", "{\"start\":1,\"end\":10}",
                "{\"start\":4294967297,\"end\":2}"}) {
            mvc.perform(post("/plan").contentType("application/json").content(body))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value(400));
        }
        mvc.perform(post("/plan").contentType("application/json").content("{\"start\":3,\"end\":3}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.msg").value("已生成同城非遗路线"))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void authenticatedMainFlowAndCrossUserProtection() throws Exception {
        JsonNode alice = registerAndLogin("flowAlice");
        JsonNode bob = registerAndLogin("flowBob");
        long userId = alice.get("userId").asLong();
        long otherId = bob.get("userId").asLong();
        String authorization = "Bearer " + alice.get("token").asText();
        String otherAuthorization = "Bearer " + bob.get("token").asText();
        mvc.perform(get("/sysuser/" + userId).header("Authorization", authorization))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.password").doesNotExist());
        mvc.perform(get("/sysuser/list").header("Authorization", authorization))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].password").doesNotExist());
        mvc.perform(get("/sysuser/" + otherId).header("Authorization", authorization)).andExpect(status().isForbidden());
        mvc.perform(post("/favorite").header("Authorization", authorization).param("userId", "" + otherId).param("fyId", "3"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/favorite").header("Authorization", authorization).param("userId", "" + userId).param("fyId", "3"))
                .andExpect(jsonPath("$.code").value(200));
        mvc.perform(get("/favorite/" + userId).header("Authorization", authorization))
                .andExpect(jsonPath("$.data[0].fyId").value(3));
        mvc.perform(post("/preference").header("Authorization", authorization).contentType("application/json")
                        .content("{\"userId\":" + otherId + ",\"city\":3,\"category\":\"传统技艺\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/preference").header("Authorization", authorization).contentType("application/json")
                        .content("{\"userId\":" + userId + ",\"city\":3,\"category\":\"传统技艺\"}"))
                .andExpect(jsonPath("$.code").value(200));
        mvc.perform(post("/recommendation/browse").header("Authorization", authorization)
                        .param("userId", "" + userId).param("fyId", "3"))
                .andExpect(jsonPath("$.code").value(200));
        mvc.perform(get("/recommendation").header("Authorization", authorization))
                .andExpect(jsonPath("$.data[0].id").value(3));
        mvc.perform(get("/recommendation").header("Authorization", authorization).param("userId", "" + otherId))
                .andExpect(status().isForbidden());
        mvc.perform(post("/ai/chat").header("Authorization", authorization).contentType("application/json")
                        .content("{\"userId\":" + otherId + ",\"message\":\"推荐路线\"}"))
                .andExpect(status().isForbidden());

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "png", bytes);
        MockMultipartFile image = new MockMultipartFile("image", "sample.png", "image/png", bytes.toByteArray());
        mvc.perform(multipart("/check-in/upload").file(image).header("Authorization", authorization)
                        .param("userId", "" + userId).param("fyId", "3").param("txt", "模拟打卡"))
                .andExpect(jsonPath("$.code").value(200));
        long recordId = jdbc.queryForObject("SELECT id FROM check_in WHERE user_id = ?", Long.class, userId);
        String pictureUrl = jdbc.queryForObject("SELECT picture_url FROM check_in WHERE id = ?", String.class, recordId);
        mvc.perform(get("/" + pictureUrl)).andExpect(status().isOk()).andExpect(content().bytes(bytes.toByteArray()));
        mvc.perform(get("/check-in/byuser").header("Authorization", authorization).param("pageNum", "1").param("pageSize", "10"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.total").value(1));
        mvc.perform(get("/check-in/byuser").header("Authorization", otherAuthorization).param("userId", "" + userId)
                        .param("pageNum", "1").param("pageSize", "10"))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/check-in").header("Authorization", otherAuthorization).param("id", "" + recordId))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/check-in").header("Authorization", authorization).param("id", "" + recordId))
                .andExpect(jsonPath("$.code").value(200));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM check_in WHERE id = ?", Integer.class, recordId));
        mvc.perform(post("/sysuser/logout").header("Authorization", authorization)).andExpect(jsonPath("$.code").value(200));
        mvc.perform(get("/favorite/" + userId).header("Authorization", authorization)).andExpect(status().isUnauthorized());
    }

    @Test
    void invalidImagesNeverCreateRecordsAndExpiredSessionsAreRejected() throws Exception {
        JsonNode identity = registerAndLogin("imageUser");
        long id = identity.get("userId").asLong();
        String token = identity.get("token").asText();
        mvc.perform(multipart("/check-in/upload").file(new MockMultipartFile("image", "fake.png", "image/png", "not an image".getBytes()))
                        .header("Authorization", "Bearer " + token).param("userId", "" + id).param("fyId", "3"))
                .andExpect(jsonPath("$.code").value(500));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM check_in WHERE user_id = ?", Integer.class, id));
        String storedHash = jdbc.queryForObject("SELECT token_hash FROM auth_session WHERE user_id = ?", String.class, id);
        assertNotEquals(token, storedHash);
        jdbc.update("UPDATE auth_session SET expires_at = ? WHERE user_id = ?", Timestamp.from(Instant.now().minusSeconds(10)), id);
        mvc.perform(get("/sysuser/" + id).header("Authorization", "Bearer " + token)).andExpect(status().isUnauthorized());
    }
}
