package com.js;

/**
 * @Description: twitter的snowflake算法 -- java实现
 * @Author: 渡劫 dujie
 * @Date: 2021/1/1 9:37 PM
 **/
public class SnowFlake {

    /**
     * 起始的时间戳
     */
    private final static long START_STMP = 1480166465631L;

    /**
     * 序列号占用的位数
     **/
    private final static long SEQUENCE_BIT = 12;
    /**
     * 机器标识占用的位数
     **/
    private final static long MACHINE_BIT = 5;
    /**
     * 数据中心占用的位数
     **/
    private final static long DATACENTER_BIT = 5;

    /**
     * 每一部分的最大值
     */
    private final static long MAX_DATACENTER_NUM = -1L ^ (-1L << DATACENTER_BIT);
    private final static long MAX_MACHINE_NUM = -1L ^ (-1L << MACHINE_BIT);
    private final static long MAX_SEQUENCE = -1L ^ (-1L << SEQUENCE_BIT);

    /**
     * 每一部分向左的位移
     */
    private final static long MACHINE_LEFT = SEQUENCE_BIT;
    private final static long DATACENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT;
    private final static long TIMESTMP_LEFT = DATACENTER_LEFT + DATACENTER_BIT;

    /**
     * 数据中心
     **/
    private final long datacenterId;
    /**
     * 机器标识
     **/
    private final long machineId;
    /**
     * 序列号
     **/
    private long sequence = 0L;
    /**
     * 上一次时间戳
     **/
    private long lastStmp = -1L;

    public SnowFlake(long datacenterId, long machineId) {
        if (datacenterId > MAX_DATACENTER_NUM || datacenterId < 0) {
            throw new IllegalArgumentException("datacenterId can't be greater than MAX_DATACENTER_NUM or less than 0");
        }
        if (machineId > MAX_MACHINE_NUM || machineId < 0) {
            throw new IllegalArgumentException("machineId can't be greater than MAX_MACHINE_NUM or less than 0");
        }
        this.datacenterId = datacenterId;
        this.machineId = machineId;
    }

    /**
     * 产生下一个ID
     *
     * @return
     */
    private synchronized long nextId() {
        long currStmp = System.currentTimeMillis();
        if (currStmp < lastStmp) {
            throw new RuntimeException("Clock moved backwards.  Refusing to generate id");
        }

        if (currStmp == lastStmp) {
            //相同毫秒内，序列号自增
            sequence = (sequence + 1) & MAX_SEQUENCE;
            //同一毫秒的序列数已经达到最大
            if (sequence == 0L) {
                currStmp = getNextMill();
            }
        } else {
            //不同毫秒内，序列号置为0
            sequence = 0L;
        }

        lastStmp = currStmp;

        return (currStmp - START_STMP) << TIMESTMP_LEFT
                //时间戳部分
                | datacenterId << DATACENTER_LEFT
                //数据中心部分
                | machineId << MACHINE_LEFT
                //机器标识部分
                | sequence;
        //序列号部分
    }

    private long getNextMill() {
        long mill = System.currentTimeMillis();
        while (mill <= lastStmp) {
            mill = System.currentTimeMillis();
        }
        return mill;
    }

    public String getUuid() {
        SnowFlake snowFlake = new SnowFlake(2, 3);
        return String.valueOf(snowFlake.nextId());
    }
}