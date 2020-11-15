package fun.learnlife.cputracer;

import java.util.ArrayList;

public interface CpuCallBack {
    void handleInfo(FileStat totalStat, FileStat processStat, ArrayList<FileStat> threadStat);

    void handleInfo(ArrayList<ShellStat> processStats, ArrayList<ShellStat> threadStat);
}
