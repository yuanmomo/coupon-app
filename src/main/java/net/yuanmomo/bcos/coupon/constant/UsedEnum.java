package net.yuanmomo.bcos.coupon.constant;

import java.math.BigInteger;

/**
 *
 */

public enum UsedEnum {
    NO(BigInteger.valueOf(0),"未使用"),
    YES(BigInteger.valueOf(1),"已经使用"),

    ;

    private BigInteger id;
    private String description;

    /**
     * @param id
     * @param description
     */
    UsedEnum(BigInteger id, String description) {
        this.id = id;
        this.description = description;
    }

    /**
     * @param id
     * @return
     */
    public static UsedEnum getById(BigInteger id) {
        for (UsedEnum value : UsedEnum.values()) {
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
