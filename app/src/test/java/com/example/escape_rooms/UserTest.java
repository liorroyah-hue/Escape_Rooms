package com.example.escape_rooms;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class UserTest {
    private User user;

    @Before
    public void setUp() {
        user = new User(1, "testUser", "password123");
    }

    @Test
    public void testUserConstructor() {
        assertEquals(1, user.getId());
        assertEquals("testUser", user.getUsername());
        assertEquals("password123", user.getPassword());
    }

    @Test
    public void testSettersAndGetters() {
        user.setId(5);
        user.setUsername("newAdmin");
        user.setPassword("secret456");

        assertEquals(5, user.getId());
        assertEquals("newAdmin", user.getUsername());
        assertEquals("secret456", user.getPassword());
    }
}
