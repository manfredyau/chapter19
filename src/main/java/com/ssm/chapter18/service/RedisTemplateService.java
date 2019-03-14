package com.ssm.chapter18.service;

public interface RedisTemplateService {
    /**
     * 執行多個命令
     */
    void execMultiCommand();

    /**
     * 執行 redis 事務
     */
    void execTransaction();

    /**
     * 執行 redis 流水線
     */
    void execPipeline();
}
