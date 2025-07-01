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
                    operation.getResponses().addApiResponse("400", createErrorResponse(
                            "잘못된 요청",
                            400, "C001", "요청 형식이 잘못되었거나 필요한 값이 누락되었습니다."
                    ));
                    operation.getResponses().addApiResponse("401", createErrorResponse(
                            "인증되지 않은 사용자",
                            401, "C002", "로그인이 필요한 요청입니다."
                    ));
                    operation.getResponses().addApiResponse("403", createErrorResponse(
                            "접근 권한 없음",
                            403, "C003", "요청에 대한 권한이 없습니다."
                    ));
                    operation.getResponses().addApiResponse("404", createErrorResponse(
                            "요청한 자원이 존재하지 않음",
                            404, "C004", "요청한 리소스를 찾을 수 없습니다."
                    ));
                    operation.getResponses().addApiResponse("409", createErrorResponse(
                            "이미 존재하거나 충돌 상태",
                            409, "C005", "이미 존재하는 데이터입니다."
                    ));
                    operation.getResponses().addApiResponse("500", createErrorResponse(
                            "서버 내부 오류",
                            500, "C006", "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
                    ));
                })
        );
    }

    private ApiResponse createErrorResponse(String description, int status, String code, String message) {
        Schema<?> errorSchema = new Schema<>()
                .$ref("#/components/schemas/BaseErrorResponse")
                .example(new java.util.LinkedHashMap<>() {{
                    put("status", status);
                    put("code", code);
                    put("message", message);
                }});

        return new ApiResponse()
                .description(description)
                .content(new Content().addMediaType(
                        "application/json",
                        new io.swagger.v3.oas.models.media.MediaType().schema(errorSchema)
                ));
    }
}
