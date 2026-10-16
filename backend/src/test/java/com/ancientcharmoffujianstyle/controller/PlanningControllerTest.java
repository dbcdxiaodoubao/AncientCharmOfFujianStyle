package com.ancientcharmoffujianstyle.controller;

import com.ancientcharmoffujianstyle.service.IFyinfoService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.Collections;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PlanningControllerTest {
    @Test
    void emptyDataProducesAnExplicitError() throws Exception {
        PlanningController controller = new PlanningController();
        IFyinfoService service = mock(IFyinfoService.class);
        when(service.listByCity(anyLong())).thenReturn(Collections.emptyList());
        ReflectionTestUtils.setField(controller, "fyinfoService", service);
        MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(new ApiExceptionHandler()).build()
                .perform(post("/plan").contentType("application/json").content("{\"start\":1,\"end\":2}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.msg").value("未找到该路线上的非遗项目"));
    }
}
