package com.ancientcharmoffujianstyle.mapper;

import org.apache.ibatis.annotations.Param;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SysUserMapperParameterBindingTest {

    @Test
    void usernameQueriesBindTheirStringArgumentAsUsername() throws NoSuchMethodException {
        assertUsernameBinding("login");
        assertUsernameBinding("getLoginVo");
        assertUsernameBinding("haveOner");
    }

    private void assertUsernameBinding(String methodName) throws NoSuchMethodException {
        Method method = SysUserMapper.class.getMethod(methodName, String.class);
        Param parameterBinding = method.getParameters()[0].getAnnotation(Param.class);

        assertNotNull(parameterBinding, methodName + " must declare a MyBatis parameter name");
        assertEquals("username", parameterBinding.value());
    }
}
