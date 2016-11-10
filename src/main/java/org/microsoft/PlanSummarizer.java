package org.microsoft;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;

public class PlanSummarizer {
    enum header {vertex, dependents_on, input_table, num_rows, data_size, basic_stats, column_stats}

    public static void main(String[] args) throws IOException {

        if(args.length<1){
            System.err.println("Usage: plan <planInputFileName.json>");
            System.exit(1);
        }
        String plan = new String(Files.readAllBytes(Paths.get(args[0])));
        JsonParser parser = new JsonParser();
        Multimap<String,String> dependencies = ArrayListMultimap.create();

        // get Tez plan
        for (Map.Entry<String, JsonElement> entry : parser.parse(plan).getAsJsonObject().get("STAGE PLANS").getAsJsonObject().entrySet()) {
            if(entry.getValue().getAsJsonObject().has("Tez")) {
                for (Map.Entry<String, JsonElement> edge : entry.getValue().getAsJsonObject().get("Tez").getAsJsonObject().get("Edges:").getAsJsonObject().entrySet()) {
                    if(edge.getValue().isJsonArray()) {
                        for (JsonElement e : edge.getValue().getAsJsonArray()) {
                            dependencies.put(edge.getKey(),e.getAsJsonObject().get("parent").getAsString());
                        }
                    }else{
                        dependencies.put(edge.getKey(),edge.getValue().getAsJsonObject().get("parent").getAsString());
                    }
                }
            }
        }

        //print the header
        System.out.println(Arrays.toString(header.values()).replace("[","").replace("]","").replaceAll(" ",""));

        // get all tables being read
        for (Map.Entry<String, JsonElement> entry : parser.parse(plan).getAsJsonObject().get("STAGE PLANS").getAsJsonObject().entrySet()) {
            if (entry.getValue().getAsJsonObject().has("Tez")) {
                for (Map.Entry<String, JsonElement> v : entry.getValue().getAsJsonObject().get("Tez").getAsJsonObject().get("Vertices:").getAsJsonObject().entrySet()) {
                    if(v.getKey().startsWith("Map")) {
                        for (JsonElement s : v.getValue().getAsJsonObject().get("Map Operator Tree:").getAsJsonArray()) {
                            JsonObject table = s.getAsJsonObject().get("TableScan").getAsJsonObject();
                            System.out.println(v.getKey() + "," + dependencies.get(v.getKey()) + "," + table.get("alias:").getAsString() + "," + getStats(table));
                        }
                    }else{
                        System.out.println(v.getKey()+"," + dependencies.get(v.getKey()) + "," + "," +getStats(v.getValue().getAsJsonObject().get("Reduce Operator Tree:").getAsJsonObject().entrySet().iterator().next().getValue().getAsJsonObject()));
                    }
                }
            }
        }
    }

    static String getStats(JsonObject table){
        String[] stats = table.get("Statistics:").getAsString().split(" ");
        return stats[2]+","+stats[5]+","+stats[8]+","+stats[11];
    }
}
