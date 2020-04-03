#!/bin/bash 

function usage() 
{
    echo " Usage : "
    echo "   bash coupon_run.sh deploy"
    echo "   bash coupon_run.sh get asset_account "
    echo "   bash coupon_run.sh select asset_account "
    echo "   bash coupon_run.sh use asset_account coupon_code "
    echo "   bash coupon_run.sh give send_asset_account receive_asset_account coupon_code "
    echo " "
    echo " "
    echo "examples : "
    echo "   bash coupon_run.sh deploy "
    echo "   bash coupon_run.sh get account1"
    echo "   bash coupon_run.sh select account1 "
    echo "   bash coupon_run.sh use account1  11111 "
    echo "   bash coupon_run.sh give account1 account2 code"
    exit 0
}

    case $1 in
    deploy)
            [ $# -lt 1 ] && { usage; }
            ;;
    get)
            [ $# -lt 2 ] && { usage; }
            ;;
    select)
            [ $# -lt 2 ] && { usage; }
            ;;
    use)
            [ $# -lt 3 ] && { usage; }
            ;;
    give)
            [ $# -lt 4 ] && { usage; }
            ;;
    *)
        usage
            ;;
    esac

    java -Djdk.tls.namedGroups="secp256k1" -cp 'apps/*:conf/:lib/*' net.yuanmomo.bcos.coupon.client.CouponClient $@

