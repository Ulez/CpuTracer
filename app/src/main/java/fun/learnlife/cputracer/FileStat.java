package fun.learnlife.cputracer;

import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;

public class FileStat extends IStat {

    public long totalCPUTimeCost;
    public long costUtime;
    public long costStime;

    private boolean isThread;
    private Long baseUtime;
    private Long baseStime;
    //进程号
    private static final int index_pid = 0;
    //名字
    private static final int index_comm = 1;
    //状态：
    private static final int index_state = 2;
    //父进程id
    private static final int index_ppid = 3;
    //线程组号
    private static final int index_pgrp = 4;

    private static final int index_session = 5;
    //    设备号
    private static final int index_tty_nr = 6;
    private static final int index_tpgid = 7;
    private static final int index_flags = 8;
    private static final int index_minflt = 9;
    private static final int index_cminflt = 10;
    private static final int index_majflt = 11;
    private static final int index_cmajflt = 12;
    //    用户态运行时间
    private static final int index_utime = 13;
    //    核心态运行时间
    private static final int index_stime = 14;
    //    累计的该任务的所有的 waited-for 进程曾经在用户态运行的时间
    private static final int index_cutime = 15;
    //    累计的该任务的所有的 waited-for 进程曾经在核心态运行的时间，单位为 jiffies
    private static final int index_cstime = 16;
    //    任务的动态优先级
    private static final int index_priority = 17;
    //    任务的静态优先级。
    private static final int index_nice = 18;
    //    该任务所在的线程组里线程的个数
    private static final int index_num_threads = 19;

    private static final int index_user = 2;
    private static final int index_nice_time = 3;
    private static final int index_system = 4;
    private static final int index_idle = 5;
    private static final int index_iowait = 6;
    private static final int index_irq = 7;
    private static final int index_softirq = 8;
    private static final int index_stealstolen = 9;
    private static final int index_guest = 10;

    private long user;
    private long nice;
    private long system;
    private long idle;
    private long iowait;
    private long irq;
    private long softirq;
    private long stealstolen;
    private long guest;
    //    private long totalCPUTime;
    private String[] procInFo;

    private static final int index_itrealvalue = 20;
    private static final int index_starttime = 21;
    private String pathPro;

    public FileStat(String pathPro, boolean isThread) {
        this.isThread = isThread;
        this.pathPro = pathPro;
        getCpuCurrentTime();
        if (isThread) {//进程或者线程的cpu时间
            baseUtime = getBaseUtime();
            baseStime = getBaseStime();
        } else {
            user = Long.parseLong(procInFo[index_user]);
            nice = Long.parseLong(procInFo[index_nice_time]);
            system = Long.parseLong(procInFo[index_system]);
            idle = Long.parseLong(procInFo[index_idle]);
            iowait = Long.parseLong(procInFo[index_iowait]);
            irq = Long.parseLong(procInFo[index_irq]);
            softirq = Long.parseLong(procInFo[index_softirq]);
            stealstolen = Long.parseLong(procInFo[index_stealstolen]);
            guest = Long.parseLong(procInFo[index_guest]);
        }
    }

    private void getCpuCurrentTime() {
        RandomAccessFile procFile = null;
        String procFileContents;
        try {
            procFile = new RandomAccessFile(pathPro, "r");
            procFileContents = procFile.readLine();
//            Log.e("lcyy", isThread + ",读取的字符为：" + procFileContents);
            procInFo = procFileContents.split(" ");
//            Log.e("lcyy","处理后字符组为："+procInFo.toString());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                procFile.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public long getBaseUtime() {
        if (procInFo != null && procInFo.length > 14)
            return Long.parseLong(procInFo[index_utime]);
        return 0;
    }

    public long getBaseStime() {
        if (procInFo != null && procInFo.length > 14)
            return Long.parseLong(procInFo[index_stime]);
        return 0;
    }

    public String getName() {
        return "pid = " + procInFo[index_pid] + ",name = " + procInFo[index_comm];
    }

    @Override
    public void updateCpuInfo() {
        getCpuCurrentTime();
        if (isThread) {
            costUtime = getBaseUtime() - baseUtime;
            costStime = getBaseStime() - baseStime;
            baseUtime = getBaseUtime();
            baseStime = getBaseStime();
            long threadTime = costUtime + costStime;
            if (threadTime > 0) {
                if (pathPro.contains("task"))
                    Log.i("lcyy", getName() + ",线程耗时 = " + (costUtime + costStime));
                else
                    Log.i("lcyy", getName() + ",进程耗时 = " + (costUtime + costStime));
            }
        } else {
            totalCPUTimeCost = Long.parseLong(procInFo[index_user]) - user +
                    Long.parseLong(procInFo[index_nice_time]) - nice +
                    Long.parseLong(procInFo[index_system]) - system +
                    Long.parseLong(procInFo[index_idle]) - idle +
                    Long.parseLong(procInFo[index_iowait]) - iowait +
                    Long.parseLong(procInFo[index_irq]) - irq +
                    Long.parseLong(procInFo[index_softirq]) - softirq +
                    Long.parseLong(procInFo[index_stealstolen]) - stealstolen +
                    Long.parseLong(procInFo[index_guest]) - guest;

            user = Long.parseLong(procInFo[index_user]);
            nice = Long.parseLong(procInFo[index_nice_time]);
            system = Long.parseLong(procInFo[index_system]);
            idle = Long.parseLong(procInFo[index_idle]);
            iowait = Long.parseLong(procInFo[index_iowait]);
            irq = Long.parseLong(procInFo[index_irq]);
            softirq = Long.parseLong(procInFo[index_softirq]);
            stealstolen = Long.parseLong(procInFo[index_stealstolen]);
            guest = Long.parseLong(procInFo[index_guest]);
            Log.i("lcyy", "总cpu耗时 = " + totalCPUTimeCost);
        }
    }
}
