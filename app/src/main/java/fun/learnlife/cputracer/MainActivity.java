package fun.learnlife.cputracer;

import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    TextView tvInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvInfo = findViewById(R.id.tv_info);
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
                final StringBuffer sb = new StringBuffer();
                sb.append("本进程占cpu:" + 100 * processStat.ratio + "%");
                sb.append('\n');
                for (FileStat s : threadStats) {
                    long threadTime = s.costStime + s.costUtime;
                    sb.append(s.getName() + ",threadCostTime = " + threadTime + ",本线程占用cpu:" + 100 * s.ratio + "%");
                    sb.append('\n');
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvInfo.setText(sb.toString());
                    }
                });
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
        }, this).start();
    }
}
