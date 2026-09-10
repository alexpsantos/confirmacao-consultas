package br.com.confirmacao.auth.api;

import br.com.confirmacao.auth.application.AuthService;
import br.com.confirmacao.audit.application.AuditService;
import br.com.confirmacao.auth.application.AuthenticationResult;
import br.com.confirmacao.auth.application.InvalidCredentialsException;
import br.com.confirmacao.auth.application.PasswordResetService;
import br.com.confirmacao.auth.application.PasswordResetRequestResult;
import br.com.confirmacao.auth.application.InvalidPasswordResetTokenException;
import br.com.confirmacao.shared.api.GlobalExceptionHandler;
import br.com.confirmacao.tenant.domain.Tenant;
import br.com.confirmacao.user.domain.User;
import br.com.confirmacao.user.domain.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private AuditService auditService;

    @MockitoBean
    private PasswordResetService passwordResetService;

    private Tenant tenant;
    private User user;

    @BeforeEach
    void setUp() {
        tenant = new Tenant(
                "Clínica Horizonte",
                "America/Sao_Paulo"
        );

        user = new User(
                tenant,
                null,
                "Alex Santos",
                "alex@exemplo.com",
                "password-hash",
                UserRole.OWNER
        );
    }

    @Test
    void shouldLogin() throws Exception {
        when(authService.authenticate(
                tenant.getId(),
                "alex@exemplo.com",
                "correct-password"
        )).thenReturn(
                new AuthenticationResult(
                        "jwt-token",
                        28_800L,
                        user
                )
        );

        mockMvc.perform(
                        post(loginUrl())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validBody())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken")
                        .value("jwt-token"))
                .andExpect(jsonPath("$.tokenType")
                        .value("Bearer"))
                .andExpect(jsonPath("$.expiresIn")
                        .value(28_800))
                .andExpect(jsonPath("$.userId")
                        .value(user.getId().toString()))
                .andExpect(jsonPath("$.tenantId")
                        .value(tenant.getId().toString()))
                .andExpect(jsonPath("$.name")
                        .value("Alex Santos"))
                .andExpect(jsonPath("$.role")
                        .value("OWNER"));
    }

    @Test
    void shouldReturnUnauthorizedForInvalidCredentials()
            throws Exception {
        when(authService.authenticate(
                tenant.getId(),
                "alex@exemplo.com",
                "incorrect-password"
        )).thenThrow(new InvalidCredentialsException());

        mockMvc.perform(
                        post(loginUrl())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validBody()
                                        .replace(
                                                "correct-password",
                                                "incorrect-password"
                                        ))
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error")
                        .value("Unauthorized"))
                .andExpect(jsonPath("$.message")
                        .value("E-mail ou senha inválidos"))
                .andExpect(jsonPath("$.path")
                        .value(loginUrl()))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    void shouldReturnBadRequestForInvalidLoginData()
            throws Exception {
        mockMvc.perform(
                        post(loginUrl())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "email": "invalid-email",
                                          "password": ""
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Existem campos inválidos"))
                .andExpect(jsonPath("$.fieldErrors.tenantId")
                        .value("O tenant é obrigatório"))
                .andExpect(jsonPath("$.fieldErrors.email")
                        .value("O e-mail deve ser válido"))
                .andExpect(jsonPath("$.fieldErrors.password")
                        .value("A senha é obrigatória"));

        verifyNoInteractions(authService);
    }

    @Test
    void shouldRequestPasswordResetWithoutAuthentication() throws Exception {
        when(passwordResetService.request(tenant.getId(), "alex@exemplo.com"))
                .thenReturn(new PasswordResetRequestResult("reset-token", user));

        mockMvc.perform(post("/api/v1/auth/password-reset/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tenantId":"%s","email":"alex@exemplo.com"}
                                """.formatted(tenant.getId())))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.resetToken").value("reset-token"));
    }

    @Test
    void shouldNotExposeWhetherResetAccountExists() throws Exception {
        when(passwordResetService.request(tenant.getId(), "unknown@exemplo.com"))
                .thenReturn(new PasswordResetRequestResult(null, null));

        mockMvc.perform(post("/api/v1/auth/password-reset/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tenantId":"%s","email":"unknown@exemplo.com"}
                                """.formatted(tenant.getId())))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.resetToken").doesNotExist());
    }

    @Test
    void shouldResetPassword() throws Exception {
        when(passwordResetService.reset("reset-token", "NovaSenha1"))
                .thenReturn(user);

        mockMvc.perform(post("/api/v1/auth/password-reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"reset-token","newPassword":"NovaSenha1"}
                                """))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldRejectInvalidResetToken() throws Exception {
        when(passwordResetService.reset("invalid-token", "NovaSenha1"))
                .thenThrow(new InvalidPasswordResetTokenException());

        mockMvc.perform(post("/api/v1/auth/password-reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"invalid-token","newPassword":"NovaSenha1"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Token de recuperação inválido ou expirado"));
    }

    private String loginUrl() {
        return "/api/v1/auth/login";
    }

    private String validBody() {
        return """
                {
                  "tenantId": "%s",
                  "email": "alex@exemplo.com",
                  "password": "correct-password"
                }
                """.formatted(tenant.getId());
    }
}
