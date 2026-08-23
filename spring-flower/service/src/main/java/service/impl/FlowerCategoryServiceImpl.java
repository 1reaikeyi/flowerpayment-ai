package service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import common.constant.ErrorConstant;
import common.constant.RedisPrefixConstant;
import common.exception.FlowerCategoryFailedException;
import mapper.FlowerCategoryMapper;
import model.dto.CategoryPageDTO;
import model.dto.FlowerCategoryDTO;
import model.entity.FlowerCategory;
import model.vo.FlowerCategoryVO;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import service.FlowerCategoryService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@CacheConfig(cacheNames = RedisPrefixConstant.CATEGORY_TYPE_PREFIX)
@Transactional(rollbackFor = Exception.class)
public class FlowerCategoryServiceImpl extends ServiceImpl<FlowerCategoryMapper, FlowerCategory> implements FlowerCategoryService {

    @CacheEvict(allEntries = true)
    @Override
    public FlowerCategoryDTO create(FlowerCategoryDTO flowerCategoryDTO) {
        FlowerCategory flowerCategory = BeanUtil.copyProperties(flowerCategoryDTO, FlowerCategory.class);
        super.save(flowerCategory);
        // 把带 id、createTime 的 entity 转回 DTO 返回
        FlowerCategoryDTO dto = BeanUtil.copyProperties(flowerCategory, FlowerCategoryDTO.class);
        return dto;
    }
    @Cacheable(key = "#type")
    @Override
    public List<FlowerCategoryVO> readByType(Long type) {
        List<FlowerCategory> flowerCategories = this.lambdaQuery()
                .eq(FlowerCategory::getType, type)
                .list();
        if (CollectionUtil.isEmpty(flowerCategories)) {
            throw new FlowerCategoryFailedException(ErrorConstant.CATEGORY_NOT_EXIST);
        }
        List<FlowerCategoryVO> flowerCategoryVOList = flowerCategories.stream()
                .map(flowerCategory -> BeanUtil.toBean(flowerCategory, FlowerCategoryVO.class))
                .toList();
        return flowerCategoryVOList;
    }
    @CacheEvict(allEntries = true)
    @Override
    public List<FlowerCategoryVO> readPage(CategoryPageDTO categoryPageDTO) {
        LambdaQueryWrapper<FlowerCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(categoryPageDTO.getType() != null, FlowerCategory::getType, categoryPageDTO.getType());
        IPage page = new Page(categoryPageDTO.getPage(),categoryPageDTO.getPageSize());
        IPage<FlowerCategory> flowerCategoryIPage = super.page(page,queryWrapper);
        List<FlowerCategoryVO> voList = flowerCategoryIPage.getRecords().stream()
                .map(flowerCategory -> BeanUtil.copyProperties(flowerCategory, FlowerCategoryVO.class))
                .collect(Collectors.toList());
        return voList;
    }
    @CacheEvict(allEntries = true)
    @Override
    public void updateByObject(FlowerCategoryDTO categoryDTO) {
         // 1. 校验 ID
        if (categoryDTO.getId() == null) {
            throw new FlowerCategoryFailedException(ErrorConstant.OPERATION_ERROR);
        }
        // 2. 构建 wrapper，只 set 有值的字段
        LambdaUpdateWrapper<FlowerCategory> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(FlowerCategory::getId, categoryDTO.getId());
        if(categoryDTO.getName() != null){
            updateWrapper.set(FlowerCategory::getName,categoryDTO.getName());
        }
        if (categoryDTO.getType() != null) {
            updateWrapper.set(FlowerCategory::getType, categoryDTO.getType());
        }
        if (categoryDTO.getSort() != null) {
            updateWrapper.set(FlowerCategory::getSort, categoryDTO.getSort());
        }
        if (categoryDTO.getStatus() != null) {
            updateWrapper.set(FlowerCategory::getStatus, categoryDTO.getStatus());
        }
        
        super.update(updateWrapper);
    }
    @CacheEvict(allEntries = true)
    @Override
    public void deleteById(List<Long> ids) {
        if(CollectionUtil.isEmpty(ids)){
            throw new FlowerCategoryFailedException(ErrorConstant.CATEGORY_NOT_EXIST);
        }
        super.removeByIds(ids);
    }


}
