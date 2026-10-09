package com.ancientcharmoffujianstyle.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SchemaInitializationScriptTest {

    @Test
    void standardSchemaScriptCreatesActivityTablesIdempotently() throws IOException {
        java.io.InputStream input = getClass().getClassLoader().getResourceAsStream("schema.sql");
        assertNotNull(input, "schema.sql must be available for Spring Boot initialization");
        String sql = new String(readAll(input), StandardCharsets.UTF_8);

        assertTrue(sql.contains("CREATE TABLE IF NOT EXISTS user_favorite"));
        assertTrue(sql.contains("CREATE TABLE IF NOT EXISTS user_preference"));
        assertTrue(sql.contains("CREATE TABLE IF NOT EXISTS user_browse_history"));
    }

    private byte[] readAll(java.io.InputStream input) throws IOException {
        java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = input.read(buffer)) != -1) {
            output.write(buffer, 0, length);
        }
        return output.toByteArray();
    }
}
