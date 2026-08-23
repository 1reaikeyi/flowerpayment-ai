package start.controller.admin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.FlowerDetailService;
import service.FlowerService;

@RestController
@RequestMapping("/admin/flower")
@Slf4j
public class AdminFlowerDetailController {
    @Autowired
    private FlowerDetailService flowerDetailService;

}
