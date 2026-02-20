package com.atfangyi.tingshu.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author fang yi
 * @Description // TODO
 * @Date 2026/1/8 16:05
 * @Version 1.0
 */
@SuppressWarnings({"all"})
@Getter
@AllArgsConstructor
public enum OperationType {
      CREATE("1"), UPDATE("2"), DELETE("3"), QUERY("4");
      private  String    type;
}
