package com.ancientcharmoffujianstyle.controller;

import com.ancientcharmoffujianstyle.domain.entity.Fyinfo;
import com.ancientcharmoffujianstyle.service.ICheckInService;
import com.ancientcharmoffujianstyle.service.IFyinfoService;
import com.ancientcharmoffujianstyle.utils.UploadUtil;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.util.Collections;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CheckInUploadFailureTest {
    @Test
    void storageFailureReturnsErrorWithoutSavingTheCheckIn() throws Exception {
        CheckInController controller = new CheckInController();
        controller.uploadUtil = mock(UploadUtil.class);
        controller.checkInService = mock(ICheckInService.class);
        controller.fyinfoService = mock(IFyinfoService.class);
        when(controller.fyinfoService.getById(3L)).thenReturn(new Fyinfo());
        when(controller.uploadUtil.uploadImage(any(), any())).thenThrow(new IOException("simulated unavailable disk"));
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(1L, null, Collections.emptyList()));
        try {
            MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(new ApiExceptionHandler()).build()
                    .perform(multipart("/check-in/upload").file(new MockMultipartFile("image", "test.png", "image/png", new byte[]{1}))
                            .param("userId", "1").param("fyId", "3"))
                    .andExpect(status().isInternalServerError()).andExpect(jsonPath("$.msg").value("图片保存失败，请重试"));
            verify(controller.checkInService, never()).save(any());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
