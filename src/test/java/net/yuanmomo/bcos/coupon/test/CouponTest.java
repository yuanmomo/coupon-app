package net.yuanmomo.bcos.coupon.test;

import java.math.BigInteger;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.web3j.tuples.generated.Tuple4;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import net.yuanmomo.bcos.coupon.client.CouponClient;
import net.yuanmomo.bcos.coupon.contract.MMCoupon;

/**
 *
 */

public class CouponTest {

    private static CouponClient client = null;

    private static String contractAddress = "0xdbace0aac06019efc3960fd95a482ef9c8501ae7";

    private static final BigInteger _0 = BigInteger.valueOf(0);
    private static final BigInteger _N_1 = BigInteger.valueOf(-1);
    private static final BigInteger _N_2 = BigInteger.valueOf(-2);
    private static final BigInteger _N_3 = BigInteger.valueOf(-3);

    @BeforeClass
    public static void before() {
        try {
            System.out.println("Starting test.....");
            client = new CouponClient();
            client.initialize();
            if (StringUtils.isBlank(contractAddress)) {
                // 部署合约，返回合约地址
                client.deployAssetAndRecordAddr();
                System.out.println(String.format("Contract deployed: [%s]", client.getContractAddress()));
            } else {
                client.setContractAddress(contractAddress);
            }
            if (StringUtils.isBlank(client.getContractAddress())) {
                Assert.fail();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @AfterClass
    public static void after() {
        try {
            client.delete(MOMO);
            client.delete(XIFU);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    public static final String MOMO = "momo";
    public static final String XIFU = "xifu";

    @Test
    public void test() {
        try {
            // 1. 用户没有优惠券 
            System.out.println(String.format("Select before getting a coupon.... "));
            checkUserCoupon(MOMO, "", _N_1);
            System.out.println(String.format("Select before getting a coupon.... PASS!!!\n\n "));

            // 2. momo 赠送 xifu 优惠券，返回 -1 ，没有优惠券可以赠送
            System.out.println(String.format("Give coupon when do not have one .... "));
            List<MMCoupon.GiveEventEventResponse> giveResult = client.give(MOMO, XIFU,
                    RandomStringUtils.randomAlphanumeric(15));
            Assert.assertTrue(CollectionUtils.isNotEmpty(giveResult));
            Assert.assertTrue(giveResult.get(0).result.compareTo(_N_1) == 0);
            System.out.println(String.format("Give coupon when do not have one .... PASS!!!\n\n"));

            // 3. momo 领取优惠券，判断结果
            System.out.println(String.format("Get a coupon.... "));
            String momoCode = getAndCheck(MOMO);
            System.out.println(String.format("Get a coupon.... PASS!!!\n\n"));

            // 4. 用户 momo 再次零用返回 -1，还有未使用的优惠券
            System.out.println(String.format("Get a coupon AGAIN.... "));
            List<MMCoupon.GetEventEventResponse> getResult = client.get(MOMO);
            Assert.assertTrue(CollectionUtils.isNotEmpty(getResult));
            Assert.assertTrue(getResult.get(0).result.compareTo(_N_1) == 0);
            System.out.println(String.format("Get a coupon AGAIN.... PASS!!!\n\n"));

            // 5. xifu 领取优惠券，判断结果
            String xifuCode = getAndCheck(XIFU);

            // 6. momo 再次赠送 xifu 优惠券，返回 -3 ，接受人 XIFU 还有未使用优惠券
            System.out.println(String.format("Give coupon when receiver already has one .... "));
            giveResult = client.give(MOMO, XIFU, momoCode);
            Assert.assertTrue(CollectionUtils.isNotEmpty(giveResult));
            Assert.assertTrue(giveResult.get(0).result.compareTo(_N_3) == 0);
            System.out.println(String.format("Give coupon when receiver already has one .... PASS!!!\n\n"));

            // 7. momo 使用优惠券
            System.out.println(String.format("First time use a coupon.... "));
            List<MMCoupon.UseEventEventResponse> useResult = client.use(MOMO, momoCode);
            Assert.assertTrue(CollectionUtils.isNotEmpty(useResult));
            Assert.assertTrue(useResult.get(0).result.compareTo(_0) == 0);
            System.out.println(String.format("First time use a coupon.... PASS!!!\n\n"));

            // 8. momo 再次使用优惠券，返回 -1，没有未使用的优惠券
            System.out.println(String.format("Use a coupon AGAIN.... "));
            useResult = client.use(MOMO, momoCode);
            Assert.assertTrue(CollectionUtils.isNotEmpty(useResult));
            Assert.assertTrue(useResult.get(0).result.compareTo(_N_1) == 0);
            System.out.println(String.format("Use a coupon AGAIN.... PASS!!!\n\n"));

            // 9. xifu 赠送 momo 优惠券，返回 0
            System.out.println(String.format("Give coupon.... "));
            giveResult = client.give(XIFU, MOMO, xifuCode);
            Assert.assertTrue(CollectionUtils.isNotEmpty(giveResult));
            Assert.assertTrue(giveResult.get(0).result.compareTo(_0) == 0);
            System.out.println(String.format("Give coupon.... PASS!!!\n\n"));

            // 10. xifu 没有优惠券
            System.out.println(String.format("Select coupon after giving.... "));
            checkUserCoupon(XIFU, "", _N_1);
            System.out.println(String.format("Select coupon after giving.... PASS!!!\n\n "));

            // 11. momo 有优惠券
            System.out.println(String.format("Select after receiving.... "));
            checkUserCoupon(MOMO, "", _0);
            System.out.println(String.format("Select after receiving.... PASS!!!\n\n "));


            // 12. momo 赠送 xifu 优惠券，返回 -2, 获赠的优惠券
            System.out.println(String.format("Give after receiving...."));
            giveResult = client.give( MOMO, XIFU, xifuCode);
            Assert.assertTrue(CollectionUtils.isNotEmpty(giveResult));
            Assert.assertTrue(giveResult.get(0).result.compareTo(_N_2) == 0);
            System.out.println(String.format("Give after receiving.... PASS!!!\n\n"));


            // 13. momo 使用获赠优惠券
            System.out.println(String.format("Use a coupon after receiving.... "));
            useResult = client.use(MOMO, xifuCode);
            Assert.assertTrue(CollectionUtils.isNotEmpty(useResult));
            Assert.assertTrue(useResult.get(0).result.compareTo(_0) == 0);
            System.out.println(String.format("Use a coupon after receiving.... PASS!!!"));

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    private String getAndCheck(String account) throws Exception {
        //  领取
        List<MMCoupon.GetEventEventResponse> getResult = client.get(account);
        Assert.assertTrue(CollectionUtils.isNotEmpty(getResult));
        Assert.assertTrue(getResult.get(0).result.compareTo(_0) == 0);

        // 判断 用户 领取优惠券的结果
        String momoCode = checkUserCoupon(account, "", _0);
        Assert.assertTrue(StringUtils.isNotBlank(momoCode));
        checkUserCoupon(account, momoCode, _0);

        return momoCode;
    }

    private String checkUserCoupon(String account, String code, BigInteger resultCode) throws Exception {
        Tuple4<BigInteger, String, BigInteger, BigInteger> selectResult = client.select(account, code);
        Assert.assertTrue(selectResult.getValue1().compareTo(resultCode) == 0);

        if (resultCode.compareTo(_0) == 0) { // 有优惠券
            System.out.println(String.format("Account: [%s] has coupon:[%s,%s]",
                    account, selectResult.getValue2(), selectResult.getValue3()));
            return selectResult.getValue2();
        }
        return "";
    }
}