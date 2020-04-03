pragma solidity ^0.4.25;

import "./Table.sol";

/**
 *  优惠券的 领取，查询，使用和赠送 的智能合约。
 *
 *  假定场景:
 *  1. 每个人名下只能有一张未使用的优惠券。
 *  2. 优惠券号码生成不会重复。
 *  3. 优惠券只能被赠送一次。
 */
contract MMCoupon {

    // 定义常量
    enum USED {NO, YES} // 是否使用
    enum TYPE {GET, GIVE} // 类型，GET: 自己领取； GIVE: 赠送获得

    // 事件
    event GetEvent(int8 result, string account, string code);
    event UseEvent(int8 result, string account, string code, uint256 value);
    event GiveEvent(int8 result, string send, string receive, string code);
    event RemoveEvent(int count);

    // 构造函数
    constructor() public {
        createTable();
    }

    // 创建表
    function createTable() private {
        TableFactory tf = TableFactory(0x1001);
        // 优惠券管理表，key : account, field: code, type, value, used
        // |  column   |  type       |  desc                  |
        // |-----------|-------------|------------------------|
        // |  account  |  string     |  用户账号（主键）         |
        // |-----------|-------------|------------------------|
        // |  code     |  string     |  优惠券码               |
        // |-----------|-------------|------------------------|
        // |  type     |  GIFT(uint)|  GET:领取; GIVE:赠送获得 |
        // |-----------|-------------|------------------------|
        // |  value    |  uint256    |  优惠券面值，单位: 分     |
        // |-----------|-------------|------------------------|
        // |  used     |  USED(uint)|  YES:已使用; NO:未使用   |
        // |-----------|-------------|------------------------|
        // 创建表
        tf.createTable("t_coupon", "account", "code,type,value,used");
    }

    // 获取表操作对象
    function openTable() private returns (Table) {
        TableFactory tf = TableFactory(0x1001);
        Table table = tf.openTable("t_coupon");
        return table;
    }

    /*
     描述 : 根据账户查询优惠券
     参数 :
             account : 账户
             code    : 是否根据优惠券号码查询
     返回值：
             参数1: 有未使用的优惠券返回 0, 否则返回 -1
             参数2: 第一个参数为0时有效，优惠券号码
             参数3: 第一个参数为0时有效，面值
             参数4: 第一个参数为0时有效，是否使用
     */
    function select(string account, string code) public constant returns (int8, string, uint256, uint8){
        // 打开表
        Table table = openTable();

        // 查询
        Condition condition = table.newCondition();
        condition.EQ("used", int(USED.NO));
        bytes memory codeBytes = bytes(code);
        // Uses memory
        if (codeBytes.length > 0) {
            condition.EQ("code", code);
        }

        Entries entries = table.select(account, condition);
        if (0 == uint256(entries.size())) {
            return (- 1, "", 0, uint8(TYPE.GET));
        } else {
            Entry entry = entries.get(0);
            return (0, entry.getString("code"), uint256(entry.getInt("value")), uint8(entry.getInt("type")));
        }
    }

    /*
     描述 : 领取优惠券
     参数 :
             account   : 账户
             code      : 优惠券号码
             value     : 面值
             type      : 类型
     返回值：
             0 : 领取成功
             -1: 还有未使用的优惠券
             -2: 数据插入失败
     */
    function get(string account, string code, uint256 value, uint8 codeType) public returns (int8){
        // 判断用户是否有还未使用的优惠券
        (int8 ret,,,) = select(account, "");

        int8 result = 0;
        if (ret == 0) {// 表示有未使用的优惠券
            result = - 1;
            emit GetEvent(result, account, code);
            return result;
        }

        Table table = openTable();

        // 没有优惠券，可以领取
        Entry entry = table.newEntry();
        entry.set("code", code);
        entry.set("type", int(codeType));
        entry.set("value", int(value));
        entry.set("used", int(USED.NO));

        result = (table.insert(account, entry) == 1 ? int8(0) : int8(- 2));
        emit GetEvent(result, account, code);
        return result;
    }


    /*
     描述 : 使用优惠券
     参数 :
             account : 账户
             code    : 优惠券号码

     返回值：
            0  : 使用成功
            -1 : 没有未使用的该优惠券
            -2 : 更新数据失败
     */
    function use(string account, string code) public returns (int8){
        // 判断用户未使用这张券
        (int8 ret,,uint256 value,) = select(account, code);

        // 判断是否已经使用过了
        int8 result = 0;
        if (ret == - 1) {
            result = - 1;
            emit UseEvent(result, account, code, 0);
            return result;
        }

        // 打开表
        Table table = openTable();

        Condition condition = table.newCondition();
        condition.EQ("code", code);

        Entry newEntry = table.newEntry();
        newEntry.set("used", int(USED.YES));
        result = table.update(account, newEntry, condition) == 1 ? int8(0) : int8(- 2);
        emit UseEvent(result, account, code, value);
        return result;
    }

    /*
     描述 : 赠送优惠券
     参数 :
             send    : 赠送者
             receive : 接收人
             code    : 优惠券号码

     返回值：
            1  : 赠送成功
            0  : 赠送失败
            -1 : 发送者没有可用优惠券
            -2 : 发送者的优惠券是别人赠送，不能再转送
            -3 : 接受人还有未使用优惠券
     */
    function give(string send, string receive, string code) public returns (int256){
        // 判断赠送用户未使用这张券
        (int8 ret1, ,uint256 value, uint8 codeType) = select(send, code);

        // 判断是否已经使用过了
        int8 result = 0;
        if (ret1 == -1) { // send 没有优惠券
            result = - 1;
            emit GiveEvent(result, send, receive, code);
            return result;
        }

        // 是否是赠送的优惠券
        if (TYPE(codeType) == TYPE.GIVE) {
            result = - 2;
            emit GiveEvent(result, send, receive, code);
            return result;
        }

        // 判断获赠用户是否有还未使用的优惠券
        (int8 ret2,,,) = select(receive, "");
        if (ret2 == 0) {
            result = - 3;
            emit GiveEvent(result, send, receive, code);
            return result;
        }

        // 发送者使用
        use(send, code);
        // 接受者获赠
        result = get(receive, code, value, uint8(TYPE.GIVE));

        emit GiveEvent(result, send, receive, code);
        return result;
    }

    /**
     * 重置数据，测试时使用。
     */
    function deleteDate(string account) public returns (int) {
        // 打开表
        Table table = openTable();
        int count = table.remove(account, table.newCondition());
        emit RemoveEvent(count);
        return count;
    }
}