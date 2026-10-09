package com.ancientcharmoffujianstyle.controller;

import com.ancientcharmoffujianstyle.domain.vo.RecommendationVo;
import com.ancientcharmoffujianstyle.service.Impl.HeritageRecommendationService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RecommendationControllerTest {

    @Test
    void recommendationEndpointSupportsMissingUserIdForColdStart() throws Exception {
        HeritageRecommendationService service = mock(HeritageRecommendationService.class);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new RecommendationController(service)).build();
        when(service.recommend(null)).thenReturn(Collections.<RecommendationVo>emptyList());

        mockMvc.perform(get("/recommendation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(service).recommend(isNull());
    }
}
