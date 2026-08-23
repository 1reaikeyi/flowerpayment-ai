package service;

import com.baomidou.mybatisplus.extension.service.IService;
import model.dto.CategoryPageDTO;
import model.dto.FlowerCategoryDTO;
import model.entity.FlowerCategory;
import model.vo.FestivalVO;
import model.vo.FlowerCategoryVO;
import model.vo.FlowerVO;

import java.util.List;

/**
 * 花店分类 Service（对应 flower_category 表）
 */

public interface FlowerCategoryService extends IService<FlowerCategory> {

    FlowerCategoryDTO create(FlowerCategoryDTO flowerCategoryDTO);

    List<FlowerCategoryVO> readByType(Long type);

    List<FlowerCategoryVO> readPage(CategoryPageDTO categoryPageDTO);

    void updateByObject(FlowerCategoryDTO categoryDTO);

    void deleteById(List<Long> ids);

    List<FlowerVO> readFlower(Long categoryId);

    List<FestivalVO> readFestival(Long categoryId);
}
