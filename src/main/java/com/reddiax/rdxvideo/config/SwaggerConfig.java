package com.reddiax.rdxvideo.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@OpenAPIDefinition(info = @Info(
        title = "RdX Video API", 
        version = "1.0", 
        description = "RdX Video API - Video content management service. " +
                "All endpoints require OAuth2 Bearer token authentication."
))
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer",
        description = "JWT Bearer token from rdx-auth OAuth2 server"
)
public class SwaggerConfig implements WebMvcConfigurer {

    private static final String ROOT_URL = "/";
    private static final String SWAGGER_UI_URL = "/swagger-ui.html";

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController(ROOT_URL, SWAGGER_UI_URL);
    }
}
