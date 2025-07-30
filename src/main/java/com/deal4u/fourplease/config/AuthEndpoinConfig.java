package com.deal4u.fourplease.config;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.PathParameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import java.util.List;
import java.util.Map;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthEndpoinConfig {

    @Bean
    public OpenApiCustomizer springSecurityLoginEndpointCustomizer() {
        return openApi -> {
            Paths paths = openApi.getPaths();

            Content content = new Content();
            content.putAll(Map.of(
                    "application/json", new MediaType()
                            .schema(new ObjectSchema()
                                    .addProperties("token", new StringSchema()
                                            .example(
                                                    "136157784067-ho9n4t7vqf8hijq3mc97l3r2vjuaaoo7")
                                    )
                            )
            ));

            Content successContent = new Content();
            successContent.putAll(Map.of(
                    "application/json", new MediaType()
                            .schema(new ObjectSchema()
                                    .addProperties("nickname", new StringSchema()
                                            .example("홍길동")
                                    )
                            )
            ));

            PathItem loginCallbackPath = new PathItem().get(
                    new Operation()
                            .tags(List.of("Member"))
                            .summary("소셜 로그인 요청")
                            .parameters(List.of(
                                    new PathParameter()
                                            .name("type")
                                            .required(true)
                                            .schema(new StringSchema()),
                                    new QueryParameter()
                                            .name("code")
                                            .required(true)
                                            .schema(new StringSchema()),
                                    new QueryParameter()
                                            .name("state")
                                            .required(true)
                                            .schema(new StringSchema())
                            ))
                            .responses(new ApiResponses()
                                    .addApiResponse("200",
                                            new ApiResponse().description("로그인 성공").headers(Map.of(
                                                    "Authorization", new Header()
                                                            .description(
                                                                    "JWT Access Token. 응답 헤더에 포함됨.")
                                                            .schema(new StringSchema()
                                                                    .example(
                                                                            "Bearer 136157784067-"
                                                                          + "ho9n4t7vqf8hijq3"
                                                                          + "mc97l3r2vjuaaoo7"
                                                                    )
                                                            ),
                                                    "Set-Cookie", new Header()
                                                            .description(
                                                                    "Refresh Token. "
                                                                  + "HttpOnly 쿠키로 전달됨."
                                                            )
                                                            .schema(new StringSchema()
                                                                    .example(
                                                                            "refreshToken"
                                                                          + "=abc.def.ghi; "
                                                                          + "Path=/; HttpOnly; "
                                                                          + "Max-Age=3600; "
                                                                          + "SameSite=Lax"
                                                                    )
                                                            )
                                            )))
                                    .addApiResponse("401", new ApiResponse()
                                            .description("로그인 실패 - 회원가입 필요")
                                            .content(content)
                                    )
                            )
            );
            paths.addPathItem("/api/v1/login/{type}", loginCallbackPath);
        };
    }
}
