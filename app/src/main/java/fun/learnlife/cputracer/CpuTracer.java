package fun.learnlife.cputracer;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CpuTracer {
    private String pkgName = "fun.learnlife.cputracer";
    private static final String TAG = "CpuTracer";
    private int mPid = android.os.Process.myPid();
    static ArrayList<ShellStat> threadShellStats = new ArrayList<>();
    private ArrayList<ShellStat> processShellStat = new ArrayList<>();

    static ArrayList<FileStat> threadStats = new ArrayList<>();
    private static FileStat totalStat;
    private static FileStat processStat;
    private boolean canReadProcFile;

    private final static Comparator<FileStat> sLoadComparator = new Comparator<FileStat>() {
        public final int
        compare(FileStat sta, FileStat stb) {
            int ta = (int) (sta.costUtime + sta.costStime);
            int tb = (int) (stb.costUtime + stb.costStime);
            if (ta != tb) {
                return ta > tb ? -1 : 1;
            }
            return 0;
        }
    };
    private CpuCallBack cpuCallBack;
    private static CpuTracer mInstance;

    private CpuTracer() {

    }

    public static CpuTracer getInstance() {
        if (mInstance == null) {
            synchronized (CpuTracer.class) {
                if (mInstance == null) {
                    mInstance = new CpuTracer();
                }
            }
        }
        return mInstance;
    }


    public CpuTracer init(CpuCallBack cpuCallBack, Context context) {
        this.cpuCallBack = cpuCallBack;
        canReadProcFile = Build.VERSION.SDK_INT < Build.VERSION_CODES.O || checkIsSystemApp(context);
        Log.d("lcyy", "can = " + canReadProcFile);
//        canReadProcFile = false;
        if (canReadProcFile) {
            File[] threadsProcFiles = new File("/proc/" + mPid + "/task").listFiles();
            if (threadsProcFiles == null || threadsProcFiles.length < 1) {
                Log.e(TAG, "error get proc,Pid =" + mPid);
                return this;
            }
            for (File threadFile : threadsProcFiles) {//进程下面各个线程的CPU使用情况
                int threadID = Integer.parseInt(threadFile.getName());
//            Log.d("lcyy", "线程号:" + threadID);
                FileStat s = new FileStat("/proc/" + mPid + "/task/" + threadID + "/stat", true);
                threadStats.add(s);
            }
            processStat = new FileStat("/proc/" + mPid + "/stat", true);//进程CPU使用情况
            totalStat = new FileStat("/proc/stat", false);//总的cpu时间
        }
        return this;
    }

    /**
     * 适配Android O以上的版本。
     * 进程占用：top -n 1 | grep speechserver
     * 线程占用：top -t -n 1 | grep speechserver
     */
    private void getByShell(String[] cmd, ArrayList<ShellStat> shellStats) {
        Log.e(TAG, "getByShell ===");
        shellStats.clear();
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(cmd);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            Log.e("oooo", "start--------");
//            Log.e("lcyy111","line===="+reader.readLine());
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                Log.e("oooo", "line=-----" + line);
                if (TextUtils.isEmpty(line)) {
                    continue;
                }
                ShellStat shellStat = new ShellStat();
                String[] content = line.split(" ");
                ArrayList<String> infos = new ArrayList<>();
                for (String s : content) {
                    if (!TextUtils.isEmpty(line)) {
                        infos.add(s);
                    }
                }
                if (infos == null || infos.size() < 9) return;
                if ("0%".equals(infos.get(3))) {
                    break;
                }
                shellStat.ratioStr = infos.get(3);
                shellStat.threadName = infos.get(9);
                shellStats.add(shellStat);
                Log.e("oooo", line);
            }
            Log.e("oooo", "end--------");
        } catch (IOException e) {
            Log.e("oooo", "get line11 error =" + e.getMessage());
            e.printStackTrace();
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    private volatile boolean getting = true;

    public void start() {
        getting = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setName("GetCpuInfoThread");
                while (getting) {
                    SystemClock.sleep(5000);
                    updateAndPrint();
                }
            }
        }).start();
    }

    public void updateAndPrint() {
//        Log.e("lcyy", "updateAndPrint info--------");
//        Log.e("lcyy", "appTime = " + appTime + ",processTime = " + processTime);
        if (!getting) return;
        for (FileStat s : threadStats) {
            s.updateCpuInfo();
        }
        if (canReadProcFile) {
            if (processStat == null) return;
            processStat.updateCpuInfo();
            totalStat.updateCpuInfo();
            Collections.sort(threadStats, sLoadComparator);
            long appTime = totalStat.totalCPUTimeCost;
            long processTime = processStat.costStime + processStat.costUtime;
            processStat.ratio = 1.0 * processTime / appTime;
            for (FileStat s : threadStats) {
                long threadTime = s.costStime + s.costUtime;
                s.ratio = 1.0 * threadTime / appTime;
            }
            if (cpuCallBack != null)
                cpuCallBack.handleInfo(totalStat, processStat, threadStats);
        } else {
            String[] cmd = {"sh", "-c",
                    "top -t -n 1 | grep " + pkgName
            };
            getByShell(cmd, threadShellStats);
            String[] cmd2 = {"sh", "-c",
                    "top -n 1 | grep " + pkgName
            };
            getByShell(cmd2, processShellStat);
            if (cpuCallBack != null)
                cpuCallBack.handleInfo(processShellStat, threadShellStats);
        }
    }

    public void stop() {
        getting = false;
        Log.e(TAG, "stop ---- getting = " + getting);
    }

    private boolean checkIsSystemApp(Context context) {
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> list = pm.getInstalledPackages(0);
        for (PackageInfo pi : list) {
            ApplicationInfo ai = null;
            try {
                Log.i("isSystem", ">>>>>pi.packageName<<<<<" + pi.packageName);
                if (pi.packageName.equals(pkgName)) {
                    ai = pm.getApplicationInfo(pi.packageName, 0);
                    Log.e("isSystem", ">>>>>>packages is<<<<<<<<" + ai.publicSourceDir);
                    if ((ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                        Log.d(TAG, ">>>>>>packages is system package:" + pi.packageName);
                        return true;
                    } else {
                        Log.d(TAG, ">>>>>>packages is not system package:" + pi.packageName);
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
