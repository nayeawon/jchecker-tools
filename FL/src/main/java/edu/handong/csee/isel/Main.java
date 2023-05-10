package edu.handong.csee.isel;

import com.google.gson.JsonObject;

public class Main {
    public static void main(String[] args) {
        Main main = new Main();
        main.run(args[0], args[1]);
    }

    public JsonObject run(String srcPath, String testPath) {
        GZoltarRunner gzoltar = new GZoltarRunner();
        JsonObject result = gzoltar.run(srcPath, testPath);
        return result;
    }
}
