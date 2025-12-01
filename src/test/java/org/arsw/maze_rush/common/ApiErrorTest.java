package org.arsw.maze_rush.common;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class ApiErrorTest {

    private static final int TEST_STATUS = 404;
    private static final String TEST_ERROR = "Not Found";
    private static final String TEST_MESSAGE = "Resource not available.";
    private static final String TEST_PATH = "/api/resource/1";
    private static final List<String> TEST_DETAILS = Arrays.asList("Field X missing", "Field Y invalid");
    private static final Instant FIXED_TIMESTAMP = Instant.now().minusSeconds(3600);

    // Cobertura de Métodos Generados por Lombok (@Data)-
    @Test
    void testLombokGeneratedMethods() {
        //  Preparar instancias
        ApiError error1 = createFullApiError(TEST_STATUS, TEST_MESSAGE);
        ApiError error2 = createFullApiError(TEST_STATUS, TEST_MESSAGE); 
        ApiError error3 = createFullApiError(500, "Server Error"); 

        // Test toString()
        String stringResult = error1.toString();
        assertNotNull(stringResult);
        assertTrue(stringResult.contains("ApiError"), "toString debe contener el nombre de la clase");
        assertTrue(stringResult.contains("status=" + TEST_STATUS), "toString debe contener los campos");
        assertTrue(stringResult.contains("error=" + TEST_ERROR));

        // Test equals() 
        assertEquals(error1, error2, "Objetos con mismos valores deben ser iguales");
        boolean isSame = error1.equals(error1);
        assertTrue(isSame);  
        assertNotEquals(error1, error3, "Objetos diferentes no deben ser iguales");
        assertNotEquals(null, error1, "No debe ser igual a null");
        assertNotEquals(error1, new Object(), "No debe ser igual a otro tipo");

        // Test hashCode()
        assertEquals(error1.hashCode(), error2.hashCode(), "HashCodes deben coincidir");
        assertNotEquals(error1.hashCode(), error3.hashCode(), "HashCodes deben diferir para objetos distintos");
        
        // Test canEqual
        assertTrue(error1.canEqual(error2));
        assertFalse(error1.canEqual(new Object()));
    }

    @Test
    void testNoArgumentConstructor_ShouldInitializeTimestampAndDefaults() {
        Instant beforeCreation = Instant.now();
        
        ApiError error = new ApiError();
        
        assertNotNull(error.getTimestamp(), "El timestamp no debe ser nulo.");
        assertFalse(error.getTimestamp().isBefore(beforeCreation), "El timestamp debe ser posterior al inicio del test");
        
        assertEquals(0, error.getStatus());
        assertNull(error.getError());
        assertNull(error.getMessage());
        assertNull(error.getPath());
        assertNull(error.getDetails());
    }
    
    @Test
    void testAllArgumentConstructor_ShouldSetAllFieldsCorrectly() {
        ApiError error = new ApiError(
            FIXED_TIMESTAMP, 
            TEST_STATUS, 
            TEST_ERROR, 
            TEST_MESSAGE, 
            TEST_PATH, 
            TEST_DETAILS
        );

        assertEquals(FIXED_TIMESTAMP, error.getTimestamp());
        assertEquals(TEST_STATUS, error.getStatus());
        assertEquals(TEST_ERROR, error.getError());
        assertEquals(TEST_MESSAGE, error.getMessage());
        assertEquals(TEST_PATH, error.getPath());
        assertEquals(TEST_DETAILS, error.getDetails());
    }

    @Test
    void testSettersAndGetters_ShouldWorkCorrectly() {
        ApiError error = new ApiError();
        List<String> newDetails = Collections.singletonList("New detail");
        Instant newTimestamp = Instant.now().plusSeconds(100);

        error.setTimestamp(newTimestamp);
        error.setStatus(200);
        error.setError("OK");
        error.setMessage("Success");
        error.setPath("/api/new");
        error.setDetails(newDetails);

        assertEquals(newTimestamp, error.getTimestamp());
        assertEquals(200, error.getStatus());
        assertEquals("OK", error.getError());
        assertEquals("Success", error.getMessage());
        assertEquals("/api/new", error.getPath());
        assertEquals(newDetails, error.getDetails());
    }

    private ApiError createFullApiError(int status, String message) {
        return new ApiError(FIXED_TIMESTAMP, status, TEST_ERROR, message, TEST_PATH, TEST_DETAILS);
    }
}