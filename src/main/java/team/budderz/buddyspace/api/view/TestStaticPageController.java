package team.budderz.buddyspace.api.view;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestStaticPageController {

    @GetMapping("/test/**")
    public ResponseEntity<Resource> serveHtmlWithoutExtension(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String pathWithoutPrefix = uri.replaceFirst("/test", "");
        String fullPath = "static/test" + pathWithoutPrefix + ".html";

        Resource resource = new ClassPathResource(fullPath);

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(resource);
    }
}
