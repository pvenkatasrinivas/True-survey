package com.truesurvey.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Title and description shown at the top of Swagger UI (http://localhost:8081/swagger-ui.html).
 * springdoc reads the controllers and builds the rest of the documentation automatically.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI trueSurveyOpenApi() {
        return new OpenAPI().info(new Info()
                .title("True Survey API")
                .version("1.0.0")
                .description("Create surveys, collect responses and view a summary of the results. "
                        + "Question types: SHORT_TEXT, SINGLE_CHOICE, MULTIPLE_CHOICE."));
    }
}
