package io.gridgo.redis.lettuce.delegate;

import io.lettuce.core.ScanArgs;

public interface LettuceScannable {

    default ScanArgs buildScanArgs(Long count, String match) {
        if (count != null || match != null) {
            ScanArgs scanArgs = new ScanArgs();
            if (count != null) {
                scanArgs.limit(count);
            }
            if (match != null) {
                scanArgs.match(match);
            }
            return scanArgs;
        }
        return null;
    }

}
