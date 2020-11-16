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
                sb.append("proc:本进程占cpu:").append(100 * processStat.ratio).append("%").append('\n');
                for (FileStat s : threadStats) {
                    long threadTime = s.costStime + s.costUtime;
                    sb.append(s.getName())
                            .append(",threadCostTime = ")
                            .append(threadTime)
                            .append(",本线程占用cpu:")
                            .append(100 * s.ratio)
                            .append("%")
                            .append('\n');
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
                final StringBuffer sb = new StringBuffer();
                for (ShellStat s : processStat) {
                    sb.append("pid=").append(s.pidStr).append(",shell:本进程占cpu:").append(s.ratioStr).append('\n');
                }
                for (ShellStat s : threadStats) {
                    sb.append("pid=").append(s.pidStr).append(",").append(s.threadName).append(",本线程占用cpu:").append(s.ratioStr).append('\n');
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvInfo.setText(sb.toString());
                    }
                });
            }
        }, this).start();
    }
}
