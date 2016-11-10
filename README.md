# PlanSummarizer

Creates a summary of a hive plan. This can be used among other things, to detect regression in the plan.

## How to compile

``mvn package assembly:single`` will create a jar with all dependency in target folder.

## How to run

``Usage: PlanSummary  <planInputFileName.json>``

Here is an example

1. Obtain a plan that can be uses to generate summary.

    ``hive -e "explain formmated SELECT l_returnflag  ,l_linestatus  ,sum(l_quantity) AS sum_qty  ,sum(l_extendedprice) AS sum_base_price  ,sum(l_extendedprice * (1 - l_discount)) AS sum_disc_price  ,sum(l_extendedprice * (1 - l_discount) * (1 + l_tax)) AS sum_charge  ,avg(l_quantity) AS avg_qty  ,avg(l_extendedprice) AS avg_price  ,avg(l_discount) AS avg_disc  ,count(*) AS count_order FROM lineitem WHERE l_shipdate <= '1998-09-16' GROUP BY l_returnflag  ,l_linestatus ORDER BY l_returnflag  ,l_linestatus"`` > plan.json 
    
    This will generate ``plan.json`` file that looks like 
    
    ```javascript
    {"STAGE DEPENDENCIES":{"Stage-1":{"ROOT STAGE":"TRUE"},"Stage-0":{"DEPENDENT STAGES":"Stage-1"}},"STAGE PLANS":{"Stage-1":{"Tez":{"Vertices:":{"Reducer 3":{"Reduce Operator Tree:":{"Select Operator":{"Statistics:":"Num rows: 450812808 Data size: 104588713421 Basic stats: COMPLETE Column stats: NONE","children":{"File Output Operator":{"compressed:":"false","Statistics:":"Num rows: 450812808 Data size: 104588713421 Basic stats: COMPLETE Column stats: NONE","table:":{"input format:":"org.apache.hadoop.mapred.TextInputFormat","output format:":"org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat","serde:":"org.apache.hadoop.hive.serde2.lazy.LazySimpleSerDe"}}},"outputColumnNames:":["_col0","_col1","_col2","_col3","_col4","_col5","_col6","_col7","_col8","_col9"],"expressions:":"KEY.reducesinkkey0 (type: string), KEY.reducesinkkey1 (type: string), VALUE._col0 (type: double), VALUE._col1 (type: double), VALUE._col2 (type: double), VALUE._col3 (type: double), VALUE._col4 (type: double), VALUE._col5 (type: double), VALUE._col6 (type: double), VALUE._col7 (type: bigint)"}}},"Reducer 2":{"Reduce Operator Tree:":{"Group By Operator":{"aggregations:":["sum(VALUE._col0)","sum(VALUE._col1)","sum(VALUE._col2)","sum(VALUE._col3)","avg(VALUE._col4)","avg(VALUE._col5)","avg(VALUE._col6)","count(VALUE._col7)"],"mode:":"mergepartial","Statistics:":"Num rows: 450812808 Data size: 104588713421 Basic stats: COMPLETE Column stats: NONE","keys:":"KEY._col0 (type: string), KEY._col1 (type: string)","children":{"Reduce Output Operator":{"value expressions:":"_col2 (type: double), _col3 (type: double), _col4 (type: double), _col5 (type: double), _col6 (type: double), _col7 (type: double), _col8 (type: double), _col9 (type: bigint)","sort order:":"++","Statistics:":"Num rows: 450812808 Data size: 104588713421 Basic stats: COMPLETE Column stats: NONE","key expressions:":"_col0 (type: string), _col1 (type: string)"}},"outputColumnNames:":["_col0","_col1","_col2","_col3","_col4","_col5","_col6","_col7","_col8","_col9"]}}},"Map 1":{"Execution mode:":"vectorized","Map Operator Tree:":[{"TableScan":{"alias:":"lineitem","Statistics:":"Num rows: 901625616 Data size: 209177426842 Basic stats: COMPLETE Column stats: NONE","filterExpr:":"(l_shipdate <= '1998-09-16') (type: boolean)","children":{"Select Operator":{"Statistics:":"Num rows: 901625616 Data size: 209177426842 Basic stats: COMPLETE Column stats: NONE","children":{"Group By Operator":{"aggregations:":["sum(l_quantity)","sum(l_extendedprice)","sum((l_extendedprice * (1 - l_discount)))","sum(((l_extendedprice * (1 - l_discount)) * (1 + l_tax)))","avg(l_quantity)","avg(l_extendedprice)","avg(l_discount)","count()"],"mode:":"hash","Statistics:":"Num rows: 901625616 Data size: 209177426842 Basic stats: COMPLETE Column stats: NONE","keys:":"l_returnflag (type: string), l_linestatus (type: string)","children":{"Reduce Output Operator":{"value expressions:":"_col2 (type: double), _col3 (type: double), _col4 (type: double), _col5 (type: double), _col6 (type: struct<count:bigint,sum:double,input:double>), _col7 (type: struct<count:bigint,sum:double,input:double>), _col8 (type: struct<count:bigint,sum:double,input:double>), _col9 (type: bigint)","sort order:":"++","Statistics:":"Num rows: 901625616 Data size: 209177426842 Basic stats: COMPLETE Column stats: NONE","Map-reduce partition columns:":"_col0 (type: string), _col1 (type: string)","key expressions:":"_col0 (type: string), _col1 (type: string)"}},"outputColumnNames:":["_col0","_col1","_col2","_col3","_col4","_col5","_col6","_col7","_col8","_col9"]}},"outputColumnNames:":["l_returnflag","l_linestatus","l_quantity","l_extendedprice","l_discount","l_tax"],"expressions:":"l_returnflag (type: string), l_linestatus (type: string), l_quantity (type: double), l_extendedprice (type: double), l_discount (type: double), l_tax (type: double)"}}}}]}},"DagId:":"root_20161107202436_b97f2221-00ee-4335-a495-847454b3ec60:1","Edges:":{"Reducer 3":{"parent":"Reducer 2","type":"SIMPLE_EDGE"},"Reducer 2":{"parent":"Map 1","type":"SIMPLE_EDGE"}},"DagName:":""}},"Stage-0":{"Fetch Operator":{"Processor Tree:":{"ListSink":{}},"limit:":"-1"}}}}
    ```

2. We can now run the planSummarizer to get summary

    ``java -jar plan.json > plansummary.csv``
    
    This will generate a plansummary.csv file that looks like 
    
    | vertex    | dependents_on      | input_table | num_rows  | data_size    | basic_stats | column_stats | 
    |-----------|--------------------|-------------|-----------|--------------|-------------|--------------| 
    | Reducer 3 | [Reducer 2]        |             | 450812808 | 104588713421 | COMPLETE    | NONE         | 
    | Reducer 2 | [Map 1]            |             | 450812808 | 104588713421 | COMPLETE    | NONE         | 
    | Map 1     | []                 | lineitem    | 901625616 | 209177426842 | COMPLETE    | NONE         | 
