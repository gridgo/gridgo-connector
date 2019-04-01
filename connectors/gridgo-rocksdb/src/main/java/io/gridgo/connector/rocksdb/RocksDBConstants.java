package io.gridgo.connector.rocksdb;

import io.gridgo.connector.keyvalue.KeyValueConstants;

public class RocksDBConstants {

    public static final String OPERATION = KeyValueConstants.OPERATION;

    public static final String OPERATION_SET = KeyValueConstants.OPERATION_SET;

    public static final String OPERATION_GET = KeyValueConstants.OPERATION_GET;
    
    public static final String OPERATION_GET_ALL = KeyValueConstants.OPERATION_GET_ALL;
    
    public static final String OPERATION_DELETE = KeyValueConstants.OPERATION_DELETE;
    
    public static final String PARAM_CREATE_IF_MISSING = "createIfMissing";
    
    public static final String PARAM_WRITE_BUFFER_SIZE = "writeBufferSize";
    
    public static final String PARAM_MAX_WRITE_BUFFER_NUMBER = "maxWriteBufferNumber";
    
    public static final String PARAM_MIN_WRITE_BUFFER_TO_MERGE = "minWriteBufferNumberToMerge";
    
    public static final String PARAM_ALLOW_2_PHASE_COMMIT = "2pc";
    
    public static final String PARAM_ALLOW_MMAP_READS = "mmapReads";
    
    public static final String PARAM_ALLOW_MMAP_WRITES = "mmapWrites";
}
