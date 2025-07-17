package com.deal4u.fourplease.domain.notification.email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.thymeleaf.exceptions.TemplateInputException;

@SpringBootTest
class HtmlTemplateRendererTest {

    @Autowired
    private HtmlTemplateRenderer htmlTemplateRenderer;

    @Test
    @DisplayName("랜더링 성공")
    void successfulRendering() {
        String rend = htmlTemplateRenderer.render("test", Map.of("date", "test"));

        String html = """
                <!DOCTYPE html>
                <html>
                
                <body>
                <div style="margin:100px;">
                  <h1>Today's Overview on NESS</h1>
                  <br>
                  <p>test의 할일 목록입니다.</p>
                </div>
                </body>
                </html>
                """;
        assertThat(rend.stripIndent().trim()).isEqualTo(html.stripIndent().trim());
    }

    @Test
    @DisplayName("랜더링 실패")
    void failedRendering() {
        ThrowableAssert.ThrowingCallable callable = () -> htmlTemplateRenderer.render(
                "discontainFile", Map.of("date", "test"));

        assertThatThrownBy(callable)
                .isInstanceOf(TemplateInputException.class);
    }
}