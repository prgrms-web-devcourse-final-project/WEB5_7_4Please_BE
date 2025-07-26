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

            PathItem loginPagePath = new PathItem().get(
                    new Operation()
                            .tags(List.of("Member"))
                            .summary("소셜 로그인 페이지 요청")
                            .description("소셜 로그인을 시도하는 API")
                            .parameters(List.of(
                                    new PathParameter()
                                            .name("type")
                                            .required(true)
                                            .schema(new StringSchema())
                                            .description("소셜 로그인 플랫폼 종류")
                            ))
                            .responses(new ApiResponses()
                                    .addApiResponse("302", new ApiResponse()
                                            .description("Success")
                                            .headers(Map.of(
                                                    "location", new Header()
                                                            .description(
                                                                    "타겟 플랫폼의 로그인 페이지 리다이렉트 url")
                                                            .schema(new StringSchema().example(
                                                                    "http://login_url.com"))
                                            ))
                                    )
                            )
            );
            paths.addPathItem("/api/v1/login/page/{type}", loginPagePath);

            Content content = new Content();
            content.putAll(Map.of(
                    "application/json", new MediaType()
                            .schema(new ObjectSchema()
                                    .addProperties("redirect", new StringSchema()
                                            .format("uri")
                                            .example("/api/v1/signup/{token}")
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
                                            .schema(new StringSchema())
                            ))
                            .responses(new ApiResponses()
                                    .addApiResponse("200", new ApiResponse().description("로그인 성공"))
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
