package fun.learnlife.cputracer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setName("CountThread1");
                int x = 1;
                while (true) {
                    x++;
                    SystemClock.sleep(5000);
                }
            }
        }).start();
        CpuTracer.getInstance().init(new CpuCallBack() {
            @Override
            public void handleInfo(FileStat totalStat, FileStat processStat, ArrayList<FileStat> threadStats) {
                long appTime = totalStat.totalCPUTimeCost;
                long processTime = processStat.costStime + processStat.costUtime;
                Log.d("lcyy", "--------print cpu Info-----------");
                Log.d("lcyy", "本进程占手机cpu ratio = " + 1.0 * processTime / appTime + "=" + processStat.ratio);
                for (FileStat s : threadStats) {
                    long threadTime = s.costStime + s.costUtime;
                    Log.d("lcyy", s.getName() + ",threadTime = " + threadTime + ",占用系统ratio的：" + 1.0 * threadTime / appTime + "=" + s.ratio);
                }
            }

            @Override
            public void handleInfo(ArrayList<ShellStat> processStat, ArrayList<ShellStat> threadStats) {
                for (ShellStat s : processStat) {
                    Log.d("lcyy", s.threadName = "shell 本进程占手机cpu ratio = " + s.ratio);
                }
                for (ShellStat s : threadStats) {
                    Log.d("lcyy", s.threadName + ", shell 线程占用系统ratio的：" + s.ratio);
                }
            }
        }, this, "com.taobao.taobao").start();
    }
}
