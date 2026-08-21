package start.metaHandler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;

import common.constant.FillHandleConstant;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;
import start.security.SecurityContextParam;

import java.time.LocalDateTime;
import java.util.Map;

import static common.constant.FillHandleConstant.CREATE_TIME_HANDLER;

@Component
public class AutoMetaObjectHandler implements MetaObjectHandler {
    private Long getUserId(){
        return SecurityContextParam.getCurrentUserId();
    }

    @Override
    public void insertFill(MetaObject metaObject) {
        this.setFieldValByName(FillHandleConstant.CREATE_TIME_HANDLER, LocalDateTime.now(), metaObject);
        this.setFieldValByName(FillHandleConstant.UPDATE_TIME_HANDLER, LocalDateTime.now(), metaObject);
        this.setFieldValByName(FillHandleConstant.CREATE_USER_HANDLER, getUserId(), metaObject);
        this.setFieldValByName(FillHandleConstant.UPDATE_USER_HANDLER, getUserId(), metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.setFieldValByName(FillHandleConstant.UPDATE_TIME_HANDLER, LocalDateTime.now(), metaObject);
        this.setFieldValByName(FillHandleConstant.UPDATE_USER_HANDLER, getUserId(), metaObject);
    }
}