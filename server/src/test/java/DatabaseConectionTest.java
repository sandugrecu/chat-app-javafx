//package com.sandugrecu;

import com.sandugrecu.database.DatabaseConnection;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseConnectionTest {

    @BeforeAll
    static void setup() {
        var stream = DatabaseConnection.class.getClassLoader().getResourceAsStream("config.properties");
        if (stream == null) {
            fail("config.properties not found in test classpath");
        }
    }


    @Test
    void testConfigLoads() {
        assertNotNull(DatabaseConnection.class, "DatabaseConnection class should load");
        assertNotNull(getPrivateField("URL"), "DB URL should not be null");
        assertNotNull(getPrivateField("USERNAME"), "DB username should not be null");
        assertNotNull(getPrivateField("PASSWORD"), "DB password should not be null");
    }

    @Test
    void testConnection() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            assertNotNull(conn, "Connection should not be null");
            assertFalse(conn.isClosed(), "Connection should be open");
        } catch (Exception e) {
            fail("Exception occurred while connecting to DB: " + e.getMessage());
        }
    }

    @Test
    void testValidateUser() {
        // Replace with a test user known to exist
        assertTrue(DatabaseConnection.validateUser("test", "test"), "User should be valid");
    }

    // Helper to read private static fields (optional)
    private Object getPrivateField(String fieldName) {
        try {
            var field = DatabaseConnection.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(null);
        } catch (Exception e) {
            return null;
        }
    }
}
