package com.ancientcharmoffujianstyle.controller;

import com.ancientcharmoffujianstyle.domain.entity.UserPreference;
import com.ancientcharmoffujianstyle.domain.entity.UserFavorite;
import com.ancientcharmoffujianstyle.service.IUserFavoriteService;
import com.ancientcharmoffujianstyle.service.IUserPreferenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserActivityControllerTest {

    private IUserFavoriteService favoriteService;
    private IUserPreferenceService preferenceService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(1L, null,
                        Collections.emptyList()));
        favoriteService = mock(IUserFavoriteService.class);
        preferenceService = mock(IUserPreferenceService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(
                new FavoriteController(favoriteService),
                new PreferenceController(preferenceService))
                .build();
    }

    @org.junit.jupiter.api.AfterEach
    void clearAuthentication() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    void addFavoriteReturnsSuccessWhenServiceCreatesFavorite() throws Exception {
        when(favoriteService.addFavorite(1L, 2L)).thenReturn(true);

        mockMvc.perform(post("/favorite").param("userId", "1").param("fyId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void addFavoriteReturnsBusinessErrorForDuplicate() throws Exception {
        when(favoriteService.addFavorite(1L, 2L)).thenReturn(false);

        mockMvc.perform(post("/favorite").param("userId", "1").param("fyId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("已收藏该非遗项目"));
    }

    @Test
    void addFavoriteRejectsMissingRequiredParameter() throws Exception {
        mockMvc.perform(post("/favorite").param("userId", "1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void removeFavoriteBindsRequestParameters() throws Exception {
        mockMvc.perform(delete("/favorite").param("userId", "1").param("fyId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(favoriteService).removeFavorite(1L, 2L);
    }

    @Test
    void listFavoritesBindsPathVariableAndReturnsData() throws Exception {
        UserFavorite favorite = new UserFavorite();
        favorite.setId(10L);
        favorite.setUserId(1L);
        favorite.setFyId(2L);
        when(favoriteService.listByUserId(1L)).thenReturn(Collections.singletonList(favorite));

        mockMvc.perform(get("/favorite/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].id").value(10))
                .andExpect(jsonPath("$.data[0].userId").value(1))
                .andExpect(jsonPath("$.data[0].fyId").value(2));

        verify(favoriteService).listByUserId(1L);
    }

    @Test
    void getPreferenceBindsPathVariable() throws Exception {
        UserPreference preference = new UserPreference();
        preference.setUserId(1L);
        preference.setCity(3L);
        preference.setCategory("传统技艺");
        when(preferenceService.getPreference(1L)).thenReturn(preference);

        mockMvc.perform(get("/preference/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.city").value(3))
                .andExpect(jsonPath("$.data.category").value("传统技艺"));

        verify(preferenceService).getPreference(1L);
    }

    @Test
    void savePreferenceBindsRequestBody() throws Exception {
        mockMvc.perform(post("/preference")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":1,\"city\":3,\"category\":\"传统技艺\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        ArgumentCaptor<UserPreference> captor = ArgumentCaptor.forClass(UserPreference.class);
        verify(preferenceService).savePreference(captor.capture());
        UserPreference preference = captor.getValue();
        org.junit.jupiter.api.Assertions.assertEquals(1L, preference.getUserId());
        org.junit.jupiter.api.Assertions.assertEquals(3L, preference.getCity());
        org.junit.jupiter.api.Assertions.assertEquals("传统技艺", preference.getCategory());
    }
}
