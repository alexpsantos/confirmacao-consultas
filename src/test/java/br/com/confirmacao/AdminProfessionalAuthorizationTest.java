package br.com.confirmacao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.profiles.active=test",
        "spring.datasource.url=jdbc:h2:mem:authorization;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.security.jwt.secret=Y29uZmlybWFjYW8tY29uc3VsdGFzLXRlc3Qta2V5LTIwMjYxMjM0NTY=",
        "app.security.jwt.issuer=test"
})
@AutoConfigureMockMvc
class AdminProfessionalAuthorizationTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void adminCanAccessAdministrativeProfessionalAndUserEndpoints() throws Exception {
        mvc.perform(get("/api/v1/admin/professionals").with(admin()))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/admin/users").with(admin()))
                .andExpect(status().isOk());
    }

    @Test
    void professionalCannotAccessAdministrativeEndpoints() throws Exception {
        mvc.perform(get("/api/v1/admin/professionals").with(professional()))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/admin/users").with(professional()))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCannotUseTheProfessionalSelfServiceEndpoint() throws Exception {
        mvc.perform(get("/api/v1/professionals/me").with(admin()))
                .andExpect(status().isForbidden());
    }

    private static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor admin() {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    private static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor professional() {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_PROFESSIONAL"));
    }
}
