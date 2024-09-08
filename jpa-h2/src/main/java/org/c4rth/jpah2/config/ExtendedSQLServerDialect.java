package org.c4rth.jpah2.config;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.dialect.SQLServerDialect;
import org.hibernate.dialect.function.CommonFunctionFactory;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.type.StandardBasicTypes;

public class ExtendedSQLServerDialect extends SQLServerDialect {

    public static final String FUNCTION_STRING_AGG = "string_agg";
    
    public ExtendedSQLServerDialect() {
       super();
       //registerFunction(FUNCTION_STRING_AGG, new StandardSQLFunction(FUNCTION_STRING_AGG));
       //registerFunction("group_concat", new StandardSQLFunction("group_concat", StandardBasicTypes.STRING));
       //registerFunction("ARRAY_AGG", new StandardSQLFunction("ARRAY_AGG", StandardBasicTypes.STRING));
    }

    public void initializeFunctionRegistry(FunctionContributions functionContributions) {
       super.initializeFunctionRegistry(functionContributions);

       CommonFunctionFactory functionFactory = new CommonFunctionFactory(functionContributions);
       functionContributions.getFunctionRegistry().register(FUNCTION_STRING_AGG, new StandardSQLFunction(FUNCTION_STRING_AGG));
       functionContributions.getFunctionRegistry().register("group_concat", new StandardSQLFunction("group_concat", StandardBasicTypes.STRING));
       functionContributions.getFunctionRegistry().register("ARRAY_AGG", new StandardSQLFunction("ARRAY_AGG", StandardBasicTypes.STRING));
    }
}