package io.gridgo.connector.rocksdb;

public class RocksDBConstants {

    public static final String OPERATION = "RocksDB_Operation";

    public static final String OPERATION_SET = "RocksDB_OperationSet";

    public static final String OPERATION_GET = "RocksDB_OperationGet";
    
    public static final String OPERATION_GET_ALL = "RocksDB_OperationGetAll";
    
    public static final String PARAM_CREATE_IF_MISSING = "createIfMissing";
    
    public static final String PARAM_WRITE_BUFFER_SIZE = "writeBufferSize";
    
    public static final String PARAM_MAX_WRITE_BUFFER_NUMBER = "maxWriteBufferNumber";
    
    public static final String PARAM_MIN_WRITE_BUFFER_TO_MERGE = "minWriteBufferNumberToMerge";
    
    public static final String PARAM_ALLOW_2_PHASE_COMMIT = "2pc";
    
    public static final String PARAM_ALLOW_MMAP_READS = "mmapReads";
    
    public static final String PARAM_ALLOW_MMAP_WRITES = "mmapWrites";
}
