package org.arsw.maze_rush.common;

import org.arsw.maze_rush.common.exceptions.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private WebRequest mockRequest(String path) {
        WebRequest request = mock(WebRequest.class);
        when(request.getDescription(false)).thenReturn(path);
        return request;
    }

    //  MethodArgumentNotValidException
    @Test
    void testHandleValidationException() {

        // mock FieldError
        FieldError fe = new FieldError("obj", "fieldName", "must not be null");

        // mock BindingResult
        BindingResult br = mock(BindingResult.class);
        when(br.getFieldErrors()).thenReturn(List.of(fe));

        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(null, br);

        WebRequest req = mockRequest("/validate");

        ResponseEntity<ApiError> response = handler.handleValidationException(ex, req);

        ApiError body = response.getBody();
        assertNotNull(body);
        assertEquals(400, body.getStatus());
        assertEquals("Bad Request", body.getError());
        assertEquals("Solicitud inválida", body.getMessage());
        assertEquals("/validate", body.getPath());
        assertEquals(List.of("fieldName: must not be null"), body.getDetails());
    }

    // NotFound
    @Test
    void testHandleNotFound() {
        WebRequest req = mockRequest("/path");
        NotFoundException ex = new NotFoundException("not found");

        ResponseEntity<ApiError> res = handler.handleNotFound(ex, req);
        ApiError body = res.getBody();

        assertNotNull(body);
        assertEquals(404, body.getStatus());
        assertEquals("not found", body.getMessage());
    }

    // ConflictException
    @Test
    void testHandleConflict() {
        WebRequest req = mockRequest("/path");
        ConflictException ex = new ConflictException("conflict");

        ResponseEntity<ApiError> res = handler.handleConflict(ex, req);
        assertEquals(409, res.getBody().getStatus());
    }

    // UnauthorizedException
    @Test
    void testHandleUnauthorized() {
        WebRequest req = mockRequest("/path");
        UnauthorizedException ex = new UnauthorizedException("unauth");

        ResponseEntity<ApiError> res = handler.handleUnauthorized(ex, req);
        assertEquals(401, res.getBody().getStatus());
    }

    // BadRequestException
    @Test
    void testHandleBadRequest() {
        WebRequest req = mockRequest("/path");
        BadRequestException ex = new BadRequestException("bad");

        ResponseEntity<ApiError> res = handler.handleBadRequest(ex, req);
        assertEquals(400, res.getBody().getStatus());
    }

    // LobbyInUseException
    @Test
    void testHandleLobbyInUse() {
        WebRequest req = mockRequest("/path");
        LobbyInUseException ex = new LobbyInUseException("lobby error");

        ResponseEntity<ApiError> res = handler.handleLobbyInUse(ex, req);
        assertEquals(409, res.getBody().getStatus());
    }

    // IllegalStateException
    @Test
    void testHandleIllegalState() {
        WebRequest req = mockRequest("/path");
        IllegalStateException ex = new IllegalStateException("illegal");

        ResponseEntity<ApiError> res = handler.handleIllegalState(ex, req);
        assertEquals(409, res.getBody().getStatus());
    }

    // Generic Exception handler
    @Test
    void testHandleOther() {
        WebRequest req = mockRequest("/path");
        Exception ex = new Exception("boom");

        ResponseEntity<ApiError> res = handler.handleOther(ex, req);
        assertEquals(500, res.getBody().getStatus());
    }
}
