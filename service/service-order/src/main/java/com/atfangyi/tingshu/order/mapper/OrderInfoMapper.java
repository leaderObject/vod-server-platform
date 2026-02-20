package com.atfangyi.tingshu.order.mapper;

import com.atfangyi.tingshu.dto.OrderDto;
import com.atfangyi.tingshu.model.order.OrderInfo;
import com.atfangyi.tingshu.vo.order.OrderInfoManagerVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.lettuce.core.dynamic.annotation.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;


@SuppressWarnings({"all"})
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {


    IPage<OrderInfoManagerVo> queryOrderInfo(Page<OrderInfoManagerVo> page, @Param("orderDto") OrderDto orderDto);


    @Select("select orInfo.id  id,\n" +
            "order_title orderTile, user_id userId,\n" +
            "order_derate.derate_amount  derateAmount,\n" +
            "orInfo.order_no     orderNo,\n" +
            "orInfo.order_status orderStatus,\n" +
            "orInfo.pay_way      payWay,\n" +
            "orDe.item_name      itemName,\n" +
            "orDe.item_url       itemUrl,\n" +
            "orDe.item_price     itemPrice,\n" +
            "order_amount  orderAmount\n" +
            "from order_info orInfo\n" +
            "join order_detail orDe\n" +
            "on orInfo.id = orDe.order_id  and  order_id=#{id}\n" +
            "left join  order_derate on orInfo.id = order_derate.order_id\n")
    List<OrderInfoManagerVo> queryOrderInfoById(@Param("id") Long id);


    @Select("select order_detail.item_id ItemId\n" +
            "from order_info\n" +
            "join order_detail\n" +
            "on order_info.id = order_detail.order_id " +
            "and order_info.order_status = #{orderStatus}\n" +
            "and order_info.item_type = #{itemType};\n")
    List<Long> queryOrderInfoByItemType(@Param("itemType") String itemType, @Param("orderStatus") String orderStatus);

}
