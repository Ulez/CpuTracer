package fun.learnlife.cputracer;

import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ShellStat extends IStat {
    private static final int index_ratio = 3;
    private static final int index_name = 9;
    private static ArrayList<IStat> threadStats = new ArrayList<>();
    private static IStat processStat;
    public String ratioStr;
    public String threadName;

    public static ArrayList<IStat> getThreadStats() {
        return threadStats;
    }

    public static IStat getProcessStat() {
        return processStat;
    }

    @Override
    void updateCpuInfo() {

    }
}
