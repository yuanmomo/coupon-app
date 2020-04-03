package net.yuanmomo.bcos.coupon.bean;

import java.math.BigInteger;

import net.yuanmomo.bcos.coupon.constant.CouponTypeEnum;
import net.yuanmomo.bcos.coupon.constant.UsedEnum;

/**
 *
 */

public class Coupon {
    private String account;
    private String code;
    private CouponTypeEnum type;
    private BigInteger value;
    private UsedEnum used;

    public String getAccount() {
        return account;
    }

    public Coupon setAccount(String account) {
        this.account = account;
        return this;
    }

    public String getCode() {
        return code;
    }

    public Coupon setCode(String code) {
        this.code = code;
        return this;
    }

    public CouponTypeEnum getType() {
        return type;
    }

    public Coupon setType(CouponTypeEnum type) {
        this.type = type;
        return this;
    }

    public BigInteger getValue() {
        return value;
    }

    public Coupon setValue(BigInteger value) {
        this.value = value;
        return this;
    }

    public UsedEnum getUsed() {
        return used;
    }

    public Coupon setUsed(UsedEnum used) {
        this.used = used;
        return this;
    }
}