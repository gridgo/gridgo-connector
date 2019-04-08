package io.gridgo.connector.redis.test;

import org.junit.Test;

import lombok.AccessLevel;
import lombok.Getter;

public class SingleRedisStringCommand extends RedisStringCommandBase {

    @Getter(AccessLevel.PUBLIC)
    private final String endpoint = "redis:single://[localhost:6379]";

    @Test
    @Override
    public void testBitcountCommand() throws InterruptedException {
        super.testBitcountCommand();
    }

    /*@Test
    @Override
    public void testBitopCommand() throws InterruptedException {
        super.testBitopCommand();
    }*/

    /*@Test
    @Override
    public void testBitposCommand() throws InterruptedException {
        super.testBitposCommand();
    }*/

    /*@Test
    @Override
    public void testBitFieldCommand() throws InterruptedException {
        super.testBitFieldCommand();
    }*/

    @Test
    @Override
    public void testSetBitCommand() throws InterruptedException {
        super.testSetBitCommand();
    }

    @Test
    @Override
    public void testGBitCommand() throws InterruptedException {
        super.testGBitCommand();
    }

    @Test
    @Override
    public void testDecrementCommand() throws InterruptedException {
        super.testDecrementCommand();
    }

    @Test
    @Override
    public void testDecrbyCommand() throws InterruptedException {
        super.testDecrbyCommand();
    }

    @Test
    @Override
    public void testMsetCommand() throws InterruptedException {
        super.testMsetCommand();
    }

    /*@Test
    @Override
    public void testMGetCommand() throws InterruptedException {
        super.testMGetCommand();
    }*/

    @Test
    @Override
    public void testGetsetCommand() throws InterruptedException {
        super.testGetsetCommand();
    }

    @Test
    @Override
    public void testGetRangeCommand() throws InterruptedException {
        super.testGetRangeCommand();
    }

    @Test
    @Override
    public void testStrLenCommand() throws InterruptedException {
        super.testStrLenCommand();
    }

    /*@Test
    @Override
    public void testIncrCommand() throws InterruptedException {
        super.testIncrCommand();
    }*/

    @Test
    @Override
    public void testSetRangeCommand() throws InterruptedException {
        super.testSetRangeCommand();
    }

    @Test
    @Override
    public void testSetNxCommand() throws InterruptedException {
        super.testSetNxCommand();
    }

    @Test
    @Override
    public void testSetExCommand() throws InterruptedException {
        super.testSetExCommand();
    }


    /*@Test
    @Override
    public void testIncrByCommand() throws InterruptedException {
        super.testIncrByCommand();
    }

    @Test
    @Override
    public void testMsetNxCommand() throws InterruptedException {
        super.testMsetNxCommand();
    }

    @Test
    @Override
    public void testPSetxECommand() throws InterruptedException {
        super.testPSetxECommand();
    }*/

    @Test
    @Override
    public void testIncrByFloatCommand() throws InterruptedException {
        super.testIncrByFloatCommand();
    }

    @Test
    @Override
    public void testEcho() throws InterruptedException {
        super.testEcho();
    }

    @Test
    @Override
    public void testDelete() throws InterruptedException {
        super.testDelete();
    }

    @Test
    @Override
    public void testSadd() throws InterruptedException {
        super.testSadd();
    }

    @Test
    @Override
    public void testScard() throws InterruptedException {
        super.testScard();
    }

    @Test
    @Override
    public void testSdiff() throws InterruptedException {
        super.testSdiff();
    }

    @Test
    @Override
    public void testSdiffStore() throws InterruptedException {
        super.testSdiffStore();
    }

    @Test
    @Override
    public void testSinter() throws InterruptedException {
        super.testSinter();
    }

    @Test
    @Override
    public void testSinterStore() throws InterruptedException {
        super.testSinterStore();
    }

    @Test
    @Override
    public void testSismember() throws InterruptedException {
        super.testSismember();
    }

    @Test
    @Override
    public void testSmembers() throws InterruptedException {
        super.testSmembers();
    }

    @Test
    @Override
    public void testSmove() throws InterruptedException {
        super.testSmove();
    }

    @Test
    @Override
    public void testSpop() throws InterruptedException {
        super.testSpop();
    }

    @Test
    @Override
    public void testSrandMember() throws InterruptedException {
        super.testSrandMember();
    }

    @Test
    @Override
    public void testSrem() throws InterruptedException {
        super.testSrem();
    }

    @Test
    @Override
    public void testSunion() throws InterruptedException {
        super.testSunion();
    }

    @Test
    @Override
    public void testSunionStore() throws InterruptedException {
        super.testSunionStore();
    }

    @Test
    @Override
    public void testScan() throws InterruptedException {
        super.testScan();
    }

    @Test
    @Override
    public void testGeoAdd() throws InterruptedException {
        super.testGeoAdd();
    }

    @Test
    @Override
    public void testDump() throws InterruptedException {
        super.testDump();
    }

    @Test
    @Override
    public void testDel() throws InterruptedException {
        super.testDel();
    }

    @Test
    @Override
    public void testExists() throws InterruptedException {
        super.testExists();
    }

    @Test
    @Override
    public void testExpire() throws InterruptedException {
        super.testExpire();
    }

    @Test
    @Override
    public void testExpireat() throws InterruptedException {
        super.testExpireat();
    }

    @Test
    @Override
    public void testKeys() throws InterruptedException {
        super.testKeys();
    }

    @Test
    @Override
    public void testPersits() throws InterruptedException {
        super.testPersits();
    }


    @Test
    @Override
    public void testPExpire() throws InterruptedException {
        super.testPExpire();
    }

    @Test
    @Override
    public void testPExpireat() throws InterruptedException {
        super.testPExpireat();
    }

    @Test
    @Override
    public void testPTTL() throws InterruptedException {
        super.testPTTL();
    }

    @Test
    @Override
    public void testRandomkey() throws InterruptedException {
        super.testRandomkey();
    }

    @Test
    @Override
    public void testRename() throws InterruptedException {
        super.testRename();
    }

    @Test
    @Override
    public void testRenamenx() throws InterruptedException {
        super.testRenamenx();
    }

    @Test
    @Override
    public void testTouch() throws InterruptedException {
        super.testTouch();
    }

    @Test
    @Override
    public void testType() throws InterruptedException {
        super.testType();
    }

    @Test
    @Override
    public void testUnlink() throws InterruptedException {
        super.testUnlink();
    }

    @Test
    @Override
    public void testHash() throws InterruptedException {
        super.testHash();
    }

    @Test
    @Override
    public void testHashExist() throws InterruptedException {
        super.testHashExist();
    }

    @Test
    @Override
    public void testHashGet() throws InterruptedException {
        super.testHashGet();
    }

    @Test
    @Override
    public void testHashGetAll() throws InterruptedException {
        super.testHashGetAll();
    }

    @Test
    @Override
    public void testHINCRBY() throws InterruptedException {
        super.testHINCRBY();
    }

    @Test
    @Override
    public void testHINCRBYFLOAT() throws InterruptedException {
        super.testHINCRBYFLOAT();
    }

    @Test
    @Override
    public void testHKEYS() throws InterruptedException {
        super.testHKEYS();
    }

    @Test
    @Override
    public void testHLEN() throws InterruptedException {
        super.testHLEN();
    }

    @Test
    @Override
    public void testHMGET() throws InterruptedException {
        super.testHMGET();
    }

    @Test
    @Override
    public void testHMSET() throws InterruptedException {
        super.testHMSET();
    }

    @Test
    @Override
    public void testHSET() throws InterruptedException {
        super.testHSET();
    }

    @Test
    @Override
    public void testHSETNX() throws InterruptedException {
        super.testHSETNX();
    }

    @Test
    @Override
    public void testHSTRLEN() throws InterruptedException {
        super.testHSTRLEN();
    }

    @Test
    @Override
    public void testHVALS() throws InterruptedException {
        super.testHVALS();
    }
}
