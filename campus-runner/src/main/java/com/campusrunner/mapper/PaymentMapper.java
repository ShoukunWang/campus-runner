package com.campusrunner.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campusrunner.entity.Payment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PaymentMapper extends BaseMapper<Payment> {

    @Select("SELECT * FROM payment WHERE payment_no = #{paymentNo} AND is_deleted = 0")
    Payment selectByPaymentNo(@Param("paymentNo") String paymentNo);
}
