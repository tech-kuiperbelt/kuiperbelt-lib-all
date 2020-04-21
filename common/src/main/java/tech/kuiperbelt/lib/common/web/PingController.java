package tech.kuiperbelt.lib.common.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * provide endpoint of "ping"
 */
@RestController
public class PingController {

    @GetMapping("/ping")
    public boolean ping() {
        return true;
    }
}
