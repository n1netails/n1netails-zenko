package com.n1netails.n1netails.zenko.constant;

import java.util.regex.Pattern;

public class TraceConstant {

    // Java
    public static final Pattern JAVA_STACK_TRACE = Pattern.compile("^\\s*at\\s.+|^Caused by:.*");
    // Python
    public static final Pattern PYTHON_STACK_TRACE = Pattern.compile("^\\s*File \".+\", line \\d+, in .+");
    // C#
    public static final Pattern CSHARP_STACK_TRACE = Pattern.compile("^\\s*at\\s.+");
    // PHP
    public static final Pattern PHP_STACK_TRACE = Pattern.compile("^#\\d+\\s+.+\\(.+\\):\\s?.*");
    // JavaScript/TypeScript
    public static final Pattern JAVASCRIPT_STACK_TRACE = Pattern.compile("^\\s*at\\s(?:.+\\s\\()?[^()]+\\.js:\\d+:\\d+\\)?$");
    // Ruby
    public static final Pattern RUBY_STACK_TRACE = Pattern.compile("^\\s*from\\s.+\\.rb:\\d+:in\\s`.*'$");
    // Go
    public static final Pattern GO_STACK_TRACE = Pattern.compile("^\\s*.+\\.go:\\d+\\s\\+0x[0-9a-fA-F]+$");
}
