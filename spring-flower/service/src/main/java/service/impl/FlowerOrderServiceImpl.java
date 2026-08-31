package service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import mapper.FlowerOrderMapper;
import model.dto.FlowerOrderPageDTO;
import model.entity.FlowerCategory;
import model.entity.FlowerOrder;
import model.entity.FlowerOrderDetail;
import model.entity.FlowerOrderPay;
import model.enums.OrderStatusEnum;
import model.enums.PayStatusEnum;
import model.vo.FlowerCategoryVO;
import model.vo.FlowerOrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import service.FlowerOrderDetailService;
import service.FlowerOrderPayService;
import service.FlowerOrderService;
import service.zhifubao.DTO.PayDTO;
import service.zhifubao.DTO.RefundDTO;
import service.zhifubao.service.ZhifubaoService;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FlowerOrderServiceImpl extends ServiceImpl<FlowerOrderMapper, FlowerOrder> implements service.FlowerOrderService {

    @Autowired
    private ZhifubaoService zhifubaoService;
    @Autowired
    private FlowerOrderPayService flowerOrderPayService;
    @Autowired
    private FlowerOrderDetailService flowerOrderDetailService;

    @Override
    public FlowerOrderVO readById(Long id) {
        FlowerOrder flowerOrder = super.getById(id);
        FlowerOrderVO flowerOrderVO = BeanUtil.toBean(flowerOrder, FlowerOrderVO.class);
        return flowerOrderVO;
    }

    @Override
    public List<FlowerOrderVO> readPage(FlowerOrderPageDTO flowerOrderPageDTO) {
        LambdaQueryWrapper<FlowerOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FlowerOrder::getStatus, flowerOrderPageDTO.getStatus());
        IPage page = new Page(flowerOrderPageDTO.getPage(),flowerOrderPageDTO.getPageSize());
        IPage<FlowerOrder> flowerOrderIPage= super.page(page,queryWrapper);
        List<FlowerOrderVO> voList = flowerOrderIPage.getRecords().stream()
                .map(flowerOrder -> BeanUtil.copyProperties(flowerOrder, FlowerOrderVO.class))
                .collect(Collectors.toList());
        return voList;
    }

    @Override
    public void update3(Long id) {
        super.lambdaUpdate()
                .eq(FlowerOrder::getId, id)
                .set(FlowerOrder::getStatus, OrderStatusEnum.COOKING);
    }

    @Override
    public void update4(Long id) {
        super.lambdaUpdate()
                .eq(FlowerOrder::getId, id)
                .set(FlowerOrder::getStatus, OrderStatusEnum.GO);
    }

    @Override
    public void update5(Long id) {
        super.lambdaUpdate()
                .eq(FlowerOrder::getId, id)
                .set(FlowerOrder::getStatus, OrderStatusEnum.DELIVERING);
    }

    @Override
    public void update6(Long id) {
        super.lambdaUpdate()
                .eq(FlowerOrder::getId, id)
                .set(FlowerOrder::getStatus, OrderStatusEnum.ARRIVED);
    }

    @Override
    public void update7(Long id) {
        super.lambdaUpdate()
                .eq(FlowerOrder::getId, id)
                .set(FlowerOrder::getStatus, OrderStatusEnum.COMPLETED);
    }

    @Override
    public void update8(Long id) {
        FlowerOrder flowerOrder = super.getById(id);
        FlowerOrderDetail flowerOrderDetail = flowerOrderDetailService.lambdaQuery()
                .eq(FlowerOrderDetail::getOrderId,flowerOrder.getId())
                .one();
        RefundDTO refundDTO = new RefundDTO();
        refundDTO.setOutTradeNo(flowerOrder.getId().toString());
        refundDTO.setOutRefundNo(flowerOrder.getId().toString());
        refundDTO.setRefundReason("因为XXXXXXXXXX,已退款");
        refundDTO.setRefundAmount(flowerOrderDetail.getAmount());
        try {
            refund(refundDTO);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        flowerOrderPayService.lambdaUpdate()
                .eq(FlowerOrderPay::getOrderId, flowerOrder.getId())
                .set(FlowerOrderPay::getPayStatus, PayStatusEnum.REFUNDED);
        super.lambdaUpdate()
                .eq(FlowerOrder::getId, id)
                .set(FlowerOrder::getStatus, OrderStatusEnum.CANCELLED);
    }

    @Override
    public void update1(Long id) {
        super.lambdaUpdate()
                .eq(FlowerOrder::getId, id)
                .set(FlowerOrder::getStatus, OrderStatusEnum.COOKING);
    }

    @Override
    public void update2(Long id) {
        super.lambdaUpdate()
                .eq(FlowerOrder::getId, id)
                .set(FlowerOrder::getStatus, OrderStatusEnum.COOKING);
    }

    public void order(PayDTO payDTO, HttpServletResponse response) throws Exception {
        String form = zhifubaoService.createPagePayForm(payDTO);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(form);
        response.getWriter().flush();
    }
    public AlipayTradeRefundResponse refund(RefundDTO refundDTO) throws Exception {
        return zhifubaoService.refund(refundDTO);
    }
}
