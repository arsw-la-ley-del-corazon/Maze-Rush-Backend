package org.arsw.maze_rush.common.exceptions;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class BadRequestExceptionTest {

    @Test
    void testConstructorWithMessage() {
        BadRequestException ex = new BadRequestException("Invalid request");

        assertEquals("Invalid request", ex.getMessage());
        assertNull(ex.getCause());
        assertTrue(ex instanceof RuntimeException);
    }

    @Test
    void testConstructorWithMessageAndCause() {
        Throwable cause = new IllegalArgumentException("Wrong argument");
        BadRequestException ex = new BadRequestException("Invalid request", cause);

        assertEquals("Invalid request", ex.getMessage());
        assertEquals(cause, ex.getCause());
        assertTrue(ex instanceof RuntimeException);
    }

    @Test
    void testExceptionThrownProperly() {
        Exception exception = assertThrows(
                BadRequestException.class,
                () -> { throw new BadRequestException("Bad input"); }
        );

        assertEquals("Bad input", exception.getMessage());
    }
}
