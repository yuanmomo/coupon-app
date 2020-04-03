package net.yuanmomo.bcos.coupon.constant;

import java.math.BigInteger;

/**
 *
 */

public enum CouponTypeEnum {
    GET(BigInteger.valueOf(0),"手动领取"),
    GIVE(BigInteger.valueOf(1),"赠送"),

    ;

    private BigInteger id;
    private String description;

    /**
     * @param id
     * @param description
     */
    CouponTypeEnum(BigInteger id, String description) {
        this.id = id;
        this.description = description;
    }

    /**
     * @param id
     * @return
     */
    public static CouponTypeEnum getById(BigInteger id) {
        for (CouponTypeEnum value : CouponTypeEnum.values()) {
            if (value.id.equals(id)) {
                return value;
            }
        }
        return null;
    }


    public BigInteger getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }
}
