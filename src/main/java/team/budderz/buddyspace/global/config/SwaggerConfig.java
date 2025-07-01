package team.budderz.buddyspace.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        String jwtSchemeName = "bearerAuth";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);

        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));

        return new OpenAPI()
                .info(new Info().title("BUDDY SPACE API")
                        .description("벗터 API 문서")
                        .version("v1.0.0"))
                .addSecurityItem(securityRequirement)
                .components(components);
    }

    @Bean
    public OpenApiCustomizer globalResponsesCustomizer() {
        return openApi -> openApi.getPaths().values().forEach(pathItem ->
                pathItem.readOperations().forEach(operation -> {
                    operation.getResponses().addApiResponse("400", createErrorResponse("잘못된 요청"));
                    operation.getResponses().addApiResponse("401", createErrorResponse("인증되지 않은 사용자"));
                    operation.getResponses().addApiResponse("403", createErrorResponse("접근 권한 없음"));
                    operation.getResponses().addApiResponse("404", createErrorResponse("요청한 자원이 존재하지 않음"));
                    operation.getResponses().addApiResponse("409", createErrorResponse("이미 존재하거나 충돌 상태"));
                    operation.getResponses().addApiResponse("500", createErrorResponse("서버 내부 오류"));
                })
        );
    }

    private ApiResponse createErrorResponse(String description) {
        Schema<?> errorSchema = new Schema<>()
                .type("object")
                .addProperty("status", new Schema<>().type("integer").example(0))
                .addProperty("code", new Schema<>().type("string").example("string"))
                .addProperty("message", new Schema<>().type("string").example("string"))
                .example(new LinkedHashMap<>() {{
                    put("status", 0);
                    put("code", "string");
                    put("message", "string");
                }});

        return new ApiResponse()
                .description(description)
                .content(new Content().addMediaType(
                        "application/json",
                        new io.swagger.v3.oas.models.media.MediaType().schema(errorSchema)
                ));
    }
}
