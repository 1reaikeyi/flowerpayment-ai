package service;

import com.baomidou.mybatisplus.extension.service.IService;
import model.entity.FlowerCategory;
import org.springframework.stereotype.Service;

/**
 * 花店分类 Service（对应 flower_category 表）
 */
@Service
public interface FlowerCategoryService extends IService<FlowerCategory> {
}
