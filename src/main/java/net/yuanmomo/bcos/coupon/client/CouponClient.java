package net.yuanmomo.bcos.coupon.client;

import java.math.BigInteger;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.Keys;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.channel.ChannelEthereumService;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.tuples.generated.Tuple4;
import org.fisco.bcos.web3j.tx.gas.StaticGasProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import net.yuanmomo.bcos.coupon.constant.CouponTypeEnum;
import net.yuanmomo.bcos.coupon.contract.MMCoupon;

public class CouponClient {

    static Logger logger = LoggerFactory.getLogger(CouponClient.class);

    private Web3j web3j;

    private Credentials credentials;

    private String contractAddress = null;

    public void setWeb3j(Web3j web3j) {
        this.web3j = web3j;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public CouponClient setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
        return this;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    public void initialize() throws Exception {

        // init the Service
        @SuppressWarnings("resource")
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
        Service service = context.getBean(Service.class);
        service.run();

        ChannelEthereumService channelEthereumService = new ChannelEthereumService();
        channelEthereumService.setChannelService(service);
        Web3j web3j = Web3j.build(channelEthereumService, 1);

        // init Credentials
        Credentials credentials = Credentials.create(Keys.createEcKeyPair());

        setCredentials(credentials);
        setWeb3j(web3j);

        logger.debug(" web3j is " + web3j + " ,credentials is " + credentials);
    }

    private static BigInteger gasPrice = new BigInteger("30000000");
    private static BigInteger gasLimit = new BigInteger("30000000");

    public String deployAssetAndRecordAddr() {
        try {
            MMCoupon mmCoupon = MMCoupon.deploy(web3j, credentials, new StaticGasProvider(gasPrice, gasLimit)).send();
            System.out.println(" deploy Coupon success, contract address is " + mmCoupon.getContractAddress());

            contractAddress = mmCoupon.getContractAddress();
        } catch (Exception e) {
            System.out.println(" deploy Coupon contract failed, error message is  " + e.getMessage());
        }
        return "";
    }

    public Tuple4<BigInteger, String, BigInteger, BigInteger> select(String account, String code) throws Exception {
        MMCoupon mmCoupon = MMCoupon.load(contractAddress, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
        return mmCoupon.select(account, code).send();
    }

    public List<MMCoupon.GetEventEventResponse> get(String account) throws Exception {
        MMCoupon mmCoupon = MMCoupon.load(contractAddress, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
        String code = RandomStringUtils.randomAlphanumeric(15);
        int value = RandomUtils.nextInt(10, 100) * 100;
        TransactionReceipt receipt = mmCoupon.get(account, code, BigInteger.valueOf(value), CouponTypeEnum.GET.getId()).send();
        return mmCoupon.getGetEventEvents(receipt);
//            List<MMCoupon.GetEventEventResponse> response = mmCoupon.getGetEventEvents(receipt);
    }

    public List<MMCoupon.UseEventEventResponse> use( String account, String code) throws Exception {
        MMCoupon mmCoupon = MMCoupon.load(contractAddress, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
        TransactionReceipt receipt = mmCoupon.use(account, code).send();
        return mmCoupon.getUseEventEvents(receipt);
    }

    public List<MMCoupon.GiveEventEventResponse> give( String send, String receive, String code) throws Exception {
            MMCoupon mmCoupon = MMCoupon.load(contractAddress, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
            TransactionReceipt receipt = mmCoupon.give(send, receive, code).send();
            return mmCoupon.getGiveEventEvents(receipt);
    }

    public List<MMCoupon.RemoveEventEventResponse> delete(String account) throws Exception {
        MMCoupon mmCoupon = MMCoupon.load(contractAddress, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
        TransactionReceipt receipt = mmCoupon.deleteDate(account).send();
        return mmCoupon.getRemoveEventEvents(receipt);
    }

    public static void Usage() {
        System.out.println(" Usage:");
        System.exit(0);
    }

    public static void main(String[] args) throws Exception {

        if (args.length < 1) {
            Usage();
        }

        CouponClient client = new CouponClient();
        client.initialize();

        switch (args[0]) {
            case "deploy":
                client.deployAssetAndRecordAddr();
                break;
            case "select":
                if (args.length < 2) {
                    Usage();
                }
                client.select(args[1], args.length == 3 ? args[2] : "");
                break;
            case "get":
                if (args.length < 2) {
                    Usage();
                }
                client.get(args[1]);
                break;
            case "use":
                if (args.length < 3) {
                    Usage();
                }
                client.use(args[1], args[2]);
                break;
            case "give":
                if (args.length < 4) {
                    Usage();
                }
                client.give(args[1], args[2], args[3]);
                break;
            default: {
                Usage();
            }
        }

        System.exit(0);
    }
}
