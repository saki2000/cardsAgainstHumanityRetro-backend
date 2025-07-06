package config;

import com.retro.retro_against_humanity_backend.exceptions.ErrorResponse;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public GlobalOpenApiCustomizer globalResponseCustomizer() {
        return openApi -> openApi.getPaths().values().forEach(pathItem ->
                pathItem.readOperations().forEach(operation -> {
                    operation.getResponses().addApiResponse("400", new ApiResponse()
                            .description("Bad Request")
                            .content(new Content().addMediaType("application/json",
                                    new MediaType().schema(new Schema<ErrorResponse>().$ref("#/components/schemas/ErrorResponse")))));
                    operation.getResponses().addApiResponse("500", new ApiResponse()
                            .description("Internal Server Error")
                            .content(new Content().addMediaType("application/json",
                                    new MediaType().schema(new Schema<ErrorResponse>().$ref("#/components/schemas/ErrorResponse")))));
                })
        );
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components().addSchemas("ErrorResponse", new Schema<ErrorResponse>()));
    }
}