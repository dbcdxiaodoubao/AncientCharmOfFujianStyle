package com.ancientcharmoffujianstyle.controller;

import com.ancientcharmoffujianstyle.domain.vo.LoginVo;
import com.ancientcharmoffujianstyle.service.ISysUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SysUserControllerTest {

    private ISysUserService sysUserService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        sysUserService = mock(ISysUserService.class);
        SysUserController controller = new SysUserController();
        controller.iSysUserService = sysUserService;
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void registerReturnsPersistedUserIdentity() throws Exception {
        LoginVo persistedUser = new LoginVo();
        persistedUser.setUserId(42L);
        persistedUser.setUserName("fujianUser");
        when(sysUserService.haveOner("fujianUser")).thenReturn(0);
        when(sysUserService.getLoginVo("fujianUser")).thenReturn(persistedUser);

        mockMvc.perform(post("/sysuser/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userName\":\"fujianUser\",\"password\":\"secret12\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.userId").value(42))
                .andExpect(jsonPath("$.data.userName").value("fujianUser"));

        verify(sysUserService).register(org.mockito.ArgumentMatchers.any());
        verify(sysUserService).getLoginVo("fujianUser");
    }

    @Test
    void registerReturnsBusinessErrorWhenUsernameFormatIsInvalid() throws Exception {
        when(sysUserService.haveOner("invalid_user")).thenReturn(0);
        doThrow(new IllegalArgumentException("用户名需为4-20位字母、数字或中文"))
                .when(sysUserService).register(org.mockito.ArgumentMatchers.any());

        mockMvc.perform(post("/sysuser/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userName\":\"invalid_user\",\"password\":\"secret12\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("用户名需为4-20位字母、数字或中文"));
    }
}
