package model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.io.Serializable;

/**
 * 收货地址簿实体类（对应 user_address 表）
 * 用户多收货地址存储（用于鲜花配送）
 */

@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_address")
@Getter
@Setter
@ToString
public class UserAddress implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 收花人姓名
     */
    @TableField("consignee")
    private String consignee;

    /**
     * 手机号
     */
    @TableField("phone")
    private String phone;

    /**
     * 收花人性别
     */
    @TableField("sex")
    private String sex;

    /**
     * 省级区划编号
     */
    @TableField("province_code")
    private String provinceCode;

    /**
     * 省级名称
     */
    @TableField("province_name")
    private String provinceName;

    /**
     * 市级区划编号
     */
    @TableField("city_code")
    private String cityCode;

    /**
     * 市级名称
     */
    @TableField("city_name")
    private String cityName;

    /**
     * 区级区划编号
     */
    @TableField("district_code")
    private String districtCode;

    /**
     * 区级名称
     */
    @TableField("district_name")
    private String districtName;

    /**
     * 详细地址
     */
    @TableField("detail")
    private String detail;

    /**
     * 标签
     */
    @TableField("label")
    private String label;

    /**
     * 是否默认：0否 1是
     */
    @TableField("is_default")
    private Long isDefault;
}
