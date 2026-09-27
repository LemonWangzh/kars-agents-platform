package com.kars.common.pdf;

import java.io.IOException;
import java.util.Objects;

/** 内容扩展点：业务模板、图表等可以实现此接口，也可用 Lambda 组合。 */
@FunctionalInterface
public interface PdfComponent {
    void render(PdfRenderContext context) throws IOException;

    default PdfComponent andThen(PdfComponent next) {
        Objects.requireNonNull(next, "next component");
        return context -> { render(context); next.render(context); };
    }
}
