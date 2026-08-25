package service;


import org.springframework.ai.content.Media;
import reactor.core.publisher.Flux;

public interface VisualService {
    Flux<String> chat(Media media);
}
