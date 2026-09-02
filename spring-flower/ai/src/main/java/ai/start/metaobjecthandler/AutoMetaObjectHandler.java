package ai.start.metaobjecthandler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import common.constant.FillHandleConstant;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;


import java.time.LocalDateTime;

@Component
public class AutoMetaObjectHandler implements MetaObjectHandler {


    @Override
    public void insertFill(MetaObject metaObject) {
        this.setFieldValByName(FillHandleConstant.CREATE_TIME_HANDLER, LocalDateTime.now(), metaObject);
        this.setFieldValByName(FillHandleConstant.UPDATE_TIME_HANDLER, LocalDateTime.now(), metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.setFieldValByName(FillHandleConstant.UPDATE_TIME_HANDLER, LocalDateTime.now(), metaObject);

    }
}