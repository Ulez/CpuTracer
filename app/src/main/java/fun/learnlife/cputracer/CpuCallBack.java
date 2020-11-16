package fun.learnlife.cputracer;

import java.util.ArrayList;

public abstract class CpuCallBack {
    void handleInfo(FileStat totalStat, FileStat processStat, ArrayList<FileStat> threadStat){};

    void handleInfo(ArrayList<ShellStat> processStats, ArrayList<ShellStat> threadStat){};
}
