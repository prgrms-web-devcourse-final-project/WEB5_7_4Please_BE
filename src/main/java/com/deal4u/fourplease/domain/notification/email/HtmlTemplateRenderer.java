package com.deal4u.fourplease.domain.notification.email;

import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;

@Component
@RequiredArgsConstructor
public class HtmlTemplateRenderer {

    private final TemplateEngine templateEngine;

    public String rend(String htmlName, Map<String, Object> data) {
        IContext content = toContent(data);
        return templateEngine.process(htmlName, content);
    }

    private IContext toContent(Map<String, Object> data) {
        return new Context(Locale.KOREA, data);
    }
}
