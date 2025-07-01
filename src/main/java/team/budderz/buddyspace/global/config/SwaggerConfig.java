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

    /**
     * 모든 OpenAPI 엔드포인트에 대해 표준 오류 응답(400, 401, 403, 404, 409, 500)을 자동으로 추가하는 커스터마이저 빈을 생성합니다.
     *
     * 각 상태 코드에 대해 일관된 오류 응답 스키마와 설명이 문서에 포함됩니다.
     *
     * @return OpenAPI 문서에 글로벌 오류 응답을 추가하는 OpenApiCustomizer 빈
     */
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

    /**
     * 지정된 설명과 함께 표준화된 에러 응답 스키마를 생성합니다.
     *
     * @param description 에러 응답에 대한 설명
     * @return status, code, message 필드를 포함하는 JSON 에러 응답 객체
     */
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
