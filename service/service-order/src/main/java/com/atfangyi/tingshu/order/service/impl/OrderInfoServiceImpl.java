package com.atfangyi.tingshu.order.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.stream.CollectorUtil;
import cn.hutool.core.util.IdUtil;
import com.atfangyi.tingshu.account.AccountFeignClient;
import com.atfangyi.tingshu.album.AlbumFeignClient;
import com.atfangyi.tingshu.common.constant.RedisConstant;
import com.atfangyi.tingshu.common.constant.SystemConstant;
import com.atfangyi.tingshu.common.execption.GuiguException;
import com.atfangyi.tingshu.common.result.ResultCodeEnum;
import com.atfangyi.tingshu.common.util.AuthContextHolder;
import com.atfangyi.tingshu.dto.OrderDto;
import com.atfangyi.tingshu.model.album.AlbumInfo;
import com.atfangyi.tingshu.model.order.OrderDerate;
import com.atfangyi.tingshu.model.order.OrderDetail;
import com.atfangyi.tingshu.model.order.OrderInfo;
import com.atfangyi.tingshu.model.user.UserInfo;
import com.atfangyi.tingshu.model.user.VipServiceConfig;
import com.atfangyi.tingshu.order.helper.SignHelper;
import com.atfangyi.tingshu.order.mapper.OrderInfoMapper;
import com.atfangyi.tingshu.order.service.OrderDerateService;
import com.atfangyi.tingshu.order.service.OrderDetailService;
import com.atfangyi.tingshu.order.service.OrderInfoService;
import com.atfangyi.tingshu.user.client.UserFeignClient;
import com.atfangyi.tingshu.vo.account.AccountLockVo;
import com.atfangyi.tingshu.vo.order.*;
import com.atfangyi.tingshu.vo.user.UserPaidRecordVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@SuppressWarnings({"all"})
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderInfoService {

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private AlbumFeignClient albumFeignClient;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private OrderDerateService orderDerateService;

    @Autowired
    private AccountFeignClient accountFeignClient;


    /**
     * 封装生成订单之前的订单数据
     *
     * @param tradeVo
     * @return
     */
    @Override
    public OrderInfoVo orderInfoService(TradeVo tradeVo) {
        OrderInfoVo orderInfoVo = new OrderInfoVo();
        //生成交易号 防止重复提交订单
        String TradeNo = IdUtil.fastSimpleUUID();
        orderInfoVo.setTradeNo(TradeNo);
        orderInfoVo.setItemType(tradeVo.getItemType());
        redisTemplate.opsForValue().set(RedisConstant.ORDER_TRADE_NO_PREFIX + AuthContextHolder.getUserId() + ":" + TradeNo, TradeNo, RedisConstant.ORDER_TRADE_EXPIRE, TimeUnit.MINUTES);
        String itemType = tradeVo.getItemType();
        BigDecimal originalAmount = new BigDecimal(0.0);
        BigDecimal derateAmount = new BigDecimal(0.0);
        BigDecimal orderAmount = new BigDecimal(0.0);
        ArrayList<OrderDetailVo> orderDetailVos = new ArrayList<>();
        ArrayList<OrderDerateVo> orderDerateVos = new ArrayList<>();
        //获取购买类型
        if (SystemConstant.ORDER_ITEM_TYPE_VIP.equals(itemType)) {
            // vip会员
            VipServiceConfig vipServiceConfig = userFeignClient.getVipServiceConfig(tradeVo.getItemId()).getData();
            Assert.notNull(vipServiceConfig, "数据出现异常{vipServiceConfig}");
            originalAmount = vipServiceConfig.getPrice();
            orderAmount = vipServiceConfig.getDiscountPrice();
            derateAmount = originalAmount.subtract(orderAmount);
            OrderDetailVo orderDetailVo = new OrderDetailVo();
            orderDetailVo.setItemId(tradeVo.getItemId());
            orderDetailVo.setItemName("购买Vip" + vipServiceConfig.getName());
            orderDetailVo.setItemUrl(vipServiceConfig.getImageUrl());
            orderDetailVo.setItemPrice(orderAmount);
            orderDetailVos.add(orderDetailVo);
            OrderDerateVo orderDerateVo = new OrderDerateVo();
            orderDerateVo.setRemarks("用户购买Vip->购买类型->" + vipServiceConfig.getName() + "原始金额->" + originalAmount + "减免->" + derateAmount + "实际金额->" + orderAmount);
            orderDerateVo.setDerateAmount(derateAmount);
            orderDerateVo.setDerateType(SystemConstant.ORDER_DERATE_VIP_SERVICE_DISCOUNT);
            orderDerateVos.add(orderDerateVo);
        } else if (SystemConstant.ORDER_ITEM_TYPE_TRACK.equals(itemType)) {
            //声音
        } else if (SystemConstant.ORDER_ITEM_TYPE_ALBUM.equals(itemType)) {
            //专辑
            AlbumInfo albumInfo = albumFeignClient.getAlbumInfoById(tradeVo.getItemId()).getData();
            Assert.notNull(albumInfo, "数据出现异常albumInfo");
            UserInfo userInfo = userFeignClient.queryUserInfoByUserId(AuthContextHolder.getUserId());
            Assert.notNull(userInfo, "数据出现异常userInfo");
            if (userInfo.getIsVip() == 1 && DateTime.now().isBefore(userInfo.getVipExpireTime())) {
                //当前购买专辑用户为vip用户
                //获取专辑原价
                originalAmount = albumInfo.getPrice();
                if (albumInfo.getVipDiscount().intValue() != -1) {
                    //判断当前专辑有折扣(Vip折扣)
                    orderAmount = originalAmount.multiply(albumInfo.getDiscount()).setScale(2, RoundingMode.HALF_UP);
                    //优惠价格
                    derateAmount = originalAmount.subtract(orderAmount);
                } else {
                    orderAmount = originalAmount;
                }
            } else {
                //当前购买专辑用户为普通用户
                originalAmount = albumInfo.getPrice();
                if (albumInfo.getDiscount().intValue() != -1) {
                    //判断当前专辑有折扣(Vip折扣)
                    orderAmount = originalAmount.multiply(albumInfo.getDiscount()).setScale(2, RoundingMode.HALF_UP);
                    //优惠价格
                    derateAmount = originalAmount.subtract(orderAmount);
                } else {
                    orderAmount = originalAmount;
                }
            }
            OrderDetailVo orderDetailVo = new OrderDetailVo();
            orderDetailVo.setItemId(tradeVo.getItemId());
            orderDetailVo.setItemName("购买专辑编号->" + albumInfo.getId() + "介绍" + albumInfo.getAlbumTitle() + "原始金额->" + originalAmount + "减免->" + derateAmount + "实际金额->" + orderAmount);
            orderDetailVo.setItemPrice(orderAmount);
            orderDetailVo.setItemUrl(albumInfo.getCoverUrl());
            orderDetailVos.add(orderDetailVo);
            OrderDerateVo orderDerateVo = new OrderDerateVo();
            orderDerateVo.setDerateAmount(derateAmount);
            orderDerateVo.setDerateType(SystemConstant.ORDER_DERATE_ALBUM_DISCOUNT);
            orderDerateVo.setRemarks("用户购买专辑编号->" + albumInfo.getId() + "介绍" + albumInfo.getAlbumTitle());
        }
        orderInfoVo.setOriginalAmount(originalAmount);
        orderInfoVo.setDerateAmount(derateAmount);
        orderInfoVo.setOrderAmount(orderAmount);
        orderInfoVo.setTimestamp(DateUtil.current());
        orderInfoVo.setOrderDetailVoList(orderDetailVos);
        orderInfoVo.setOrderDerateVoList(orderDerateVos);
        Map<String, Object> map = BeanUtil.beanToMap(orderInfoVo, false, true);
        log.info("订单数据{}", map);
        orderInfoVo.setSign(SignHelper.getSign(map));
        return orderInfoVo;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public String submitOrder(OrderInfoVo orderInfoVo) {
        Long userId = AuthContextHolder.getUserId();
        String RedisKey = RedisConstant.ORDER_TRADE_NO_PREFIX + userId + ":" + orderInfoVo.getTradeNo();
        DefaultRedisScript<Boolean> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText("if redis.call(\"get\",KEYS[1]) == ARGV[1]\n" + "then\n" + "    return redis.call(\"del\",KEYS[1])\n" + "else\n" + "    return 0\n" + "end");
        redisScript.setResultType(Boolean.class);
        Boolean flag = (Boolean) redisTemplate.execute(redisScript, Arrays.asList(RedisKey), orderInfoVo.getTradeNo());
        if (!flag) {
            //订单存在或重复提交订单
            throw new GuiguException(501, "订单重复提交");
        }
        //判断数据是否被修改
        Map<String, Object> map = BeanUtil.beanToMap(orderInfoVo, false, false);
        map.remove("payWay");
        SignHelper.checkSign(map);
        OrderInfo orderInfo = BeanUtil.copyProperties(orderInfoVo, OrderInfo.class);
        orderInfo.setUserId(userId);
        if (!CollectionUtils.isEmpty(orderInfoVo.getOrderDetailVoList()))
            orderInfo.setOrderTitle(orderInfoVo.getOrderDetailVoList().get(0).getItemName());
        String OrderNo = DateUtil.today().replaceAll("-", "") + IdUtil.getSnowflakeNextIdStr();
        orderInfo.setOrderNo(OrderNo);
        orderInfo.setOrderStatus(SystemConstant.ORDER_STATUS_UNPAID);
        orderInfoMapper.insert(orderInfo);
        if (!CollectionUtils.isEmpty(orderInfoVo.getOrderDetailVoList())) {
            orderDetailService.saveBatch(orderInfoVo.getOrderDetailVoList().stream().map(orderDetailVo -> {
                OrderDetail orderDetail = BeanUtil.copyProperties(orderDetailVo, OrderDetail.class);
                orderDetail.setOrderId(orderInfo.getId());
                return orderDetail;
            }).collect(Collectors.toList()));
        }
        if (!CollectionUtils.isEmpty(orderInfoVo.getOrderDerateVoList())) {
            orderDerateService.saveBatch(orderInfoVo.getOrderDerateVoList().stream().map(orderDerateVo -> {
                OrderDerate orderDerate = BeanUtil.copyProperties(orderDerateVo, OrderDerate.class);
                orderDerate.setOrderId(orderInfo.getId());
                return orderDerate;
            }).collect(Collectors.toList()));
        }
        OrderDerate orderDerate = new OrderDerate();
        if (orderInfoVo.getPayWay().equals(SystemConstant.ORDER_PAY_ACCOUNT)) {
            //余额支付
            BigDecimal bigDecimal = accountFeignClient.getAvailableAmount().getData();
            //用户账户余额不足
            if (orderInfoVo.getOrderAmount().compareTo(bigDecimal) > 0)
                throw new GuiguException(ResultCodeEnum.ACCOUNT_LESS);
            //对用户账户资金进行扣减 以及生成账户记录详细信息
            AccountLockVo accountLockVo = new AccountLockVo();
            accountLockVo.setUserId(userId);
            accountLockVo.setOrderNo(orderInfo.getOrderNo());
            accountLockVo.setAmount(orderInfoVo.getOrderAmount());
            accountLockVo.setContent(orderInfoVo.getOrderDetailVoList().get(0).getItemName());
            AccountLockVo lockVo = accountFeignClient.checkAndLock(accountLockVo).getData();
            //用户购买的商品类型 以及给予开通权限
            UserPaidRecordVo userPaidRecordVo = new UserPaidRecordVo();
            userPaidRecordVo.setOrderNo(orderInfo.getOrderNo());
            userPaidRecordVo.setUserId(userId);
            userPaidRecordVo.setItemType(orderInfoVo.getItemType());
            if (CollectionUtil.isNotEmpty(orderInfoVo.getOrderDetailVoList()))
                userPaidRecordVo.setItemIdList(orderInfoVo.getOrderDetailVoList().stream().map(orderDetailVo -> orderDetailVo.getItemId()).collect(Collectors.toList()));
            userFeignClient.saveUserPaid(userPaidRecordVo);
            log.info("支付成功");
            LambdaUpdateWrapper<OrderInfo> orderInfoLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            orderInfoLambdaUpdateWrapper.set(OrderInfo::getOrderStatus, SystemConstant.ORDER_STATUS_PAID);
            orderInfoLambdaUpdateWrapper.eq(OrderInfo::getOrderNo, OrderNo);
            if (orderInfoMapper.update(null, orderInfoLambdaUpdateWrapper) < 1) {
                throw new GuiguException(ResultCodeEnum.DATA_ERROR);
            }
        } else {
            //使用WPAY

        }
        return OrderNo;
    }

    @Override
    public Map<String, Object> getOrderInfo(String orderNo) {
        HashMap<String, Object> map = new HashMap<>();
        //order信息
        LambdaQueryWrapper<OrderInfo> orderInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        orderInfoLambdaQueryWrapper.eq(OrderInfo::getOrderNo, orderNo).eq(OrderInfo::getUserId, AuthContextHolder.getUserId());
        OrderInfo orderInfo = orderInfoMapper.selectOne(orderInfoLambdaQueryWrapper);
        Assert.notNull(orderInfo, "订单不存在");
        map.put("id", orderInfo.getId());
        map.put("createTime", orderInfo.getCreateTime());
        map.put("userId", orderInfo.getUserId());
        map.put("orderTitle", orderInfo.getOrderTitle());
        map.put("orderNo", orderInfo.getOrderNo());
        map.put("orderStatus", orderInfo.getOrderStatus());
        map.put("originalAmount", orderInfo.getOriginalAmount());
        map.put("derateAmount", orderInfo.getDerateAmount());
        map.put("orderAmount", orderInfo.getOrderAmount());
        map.put("itemType", orderInfo.getItemType());
        map.put("payWay", orderInfo.getPayWay());
        map.put("orderDetailList", orderDetailService.list(Wrappers.lambdaQuery(OrderDetail.class).eq(OrderDetail::getOrderId, orderInfo.getId())));
        map.put("orderDerateList", orderDerateService.list(Wrappers.lambdaQuery(OrderDerate.class).eq(OrderDerate::getOrderId, orderInfo.getId())));
        map.put("orderStatusName", this.OrderSatus(orderInfo.getOrderStatus()));
        map.put("payWayName", this.PayStatus(orderInfo.getPayWay()));
        return map;
    }

    @Override
    public Page<OrderInfo> findUserPage(Long page, Long size) {
        Page<OrderInfo> orderInfoPage = orderInfoMapper.selectPage(new Page<>(page, size), Wrappers.lambdaQuery(OrderInfo.class).eq(OrderInfo::getUserId, AuthContextHolder.getUserId()));
        if (CollectionUtil.isNotEmpty(orderInfoPage.getRecords())) {
            orderInfoPage.getRecords().forEach(orderInfo -> {
                orderInfo.setOrderStatusName(this.OrderSatus(orderInfo.getOrderStatus()));
                orderInfo.setPayWayName(this.PayStatus(orderInfo.getPayWay()));
                orderInfo.setOrderDerateList(orderDerateService.list(Wrappers.lambdaQuery(OrderDerate.class).eq(OrderDerate::getOrderId, orderInfo.getId())));
                orderInfo.setOrderDetailList(orderDetailService.list(Wrappers.lambdaQuery(OrderDetail.class).eq(OrderDetail::getOrderId, orderInfo.getId())));
            });
        }
        return orderInfoPage;
    }

    @Override
    public IPage<OrderInfoManagerVo> queryOrderInfo(Long current, Long size, OrderDto orderDto) {
        IPage<OrderInfoManagerVo> orderInfoManagerVoIPage = orderInfoMapper.queryOrderInfo(new Page<OrderInfoManagerVo>(current, size), orderDto);
        List<OrderInfoManagerVo> orderInfoManagerVoIPageRecords = orderInfoManagerVoIPage.getRecords();
        if (!CollectionUtils.isEmpty(orderInfoManagerVoIPageRecords)) {
            orderInfoManagerVoIPageRecords.stream().map(orderInfoManagerVo -> {
                orderInfoManagerVo.setPayWay(this.PayStatus(orderInfoManagerVo.getPayWay()));
                orderInfoManagerVo.setOrderStatus(this.OrderSatus(orderInfoManagerVo.getOrderStatus()));
                return orderInfoManagerVo;
            }).collect(Collectors.toList());
        }
        return orderInfoManagerVoIPage;
    }

    @Override
    public List<OrderInfoManagerVo> queryOrderInfoById(Long id) {
        List<OrderInfoManagerVo> orderInfoManagerVos = orderInfoMapper.queryOrderInfoById(id);
        if (!CollectionUtils.isEmpty(orderInfoManagerVos)) {
            return orderInfoManagerVos.stream().map(orderInfoManagerVo -> {
                orderInfoManagerVo.setPayWay(this.PayStatus(orderInfoManagerVo.getPayWay()));
                orderInfoManagerVo.setOrderStatus(this.OrderSatus(orderInfoManagerVo.getOrderStatus()));
                return orderInfoManagerVo;
            }).collect(Collectors.toList());
        }
        return orderInfoManagerVos;
    }

    @Override
    public List<Long> queryOrderInfoByItemType(String itemType, String orderStatus) {
        return orderInfoMapper.queryOrderInfoByItemType(itemType, orderStatus);
    }


    public String PayStatus(String PayStatus) {
        switch (PayStatus) {
            case "1101":
                PayStatus = "微信支付";
                break;
            case "1102":
                PayStatus = "支付宝支付";
                break;
            case "1103":
                PayStatus = "账户余额支付";
                break;
        }
        return PayStatus;
    }

    public String OrderSatus(String OrderSatus) {
        switch (OrderSatus) {
            case "0901":
                OrderSatus = "未支付";
                break;
            case "0902":
                OrderSatus = "已支付";
                break;
            case "0903":
                OrderSatus = "已取消";
                break;
        }
        return OrderSatus;
    }


}
