package me.yourbay.vividpath;

import android.graphics.Path;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import me.yourbay.basic.vividpath.VividDrawable;
import me.yourbay.basic.vividpath.VividPath;
import me.yourbay.basic.vividpath.VividView;

public class MainActivity extends AppCompatActivity {

    private final static String S_BATTERY_STR = "M0,19 C0,16.7828376 1.20753986,14.8416411 3.00211353,13.8029948 C3.10690617,8.92340284 7.0952729,5 12,5 C15.2334975,5 18.0687212,6.7052115 19.6554697,9.26543304 C20.2402063,9.09270499 20.8592769,9 21.5,9 C24.2845531,9 26.6601501,10.7509485 27.5858427,13.2118973 C30.129704,13.9074543 32,16.2353674 32,19 C32,22.3069658 29.3136299,25 25.9998243,25 L6.00017566,25 C2.68697795,25 0,22.3137085 0,19";
    private List<VividDrawable> mList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Path path = PathParser.parsePath(S_BATTERY_STR);
        //
        VividDrawable vividDrawable1 = new VividDrawable(path);
        ((ImageView) findViewById(R.id.iv_vividPath)).setImageDrawable(vividDrawable1);
        mList.add(vividDrawable1);
        //
        VividDrawable vividDrawable2 = new VividDrawable(path);
        vividDrawable2.getVividPath().setMode(VividPath.Mode.Increase);
        ((ImageView) findViewById(R.id.iv_vividPath2)).setImageDrawable(vividDrawable2);
        mList.add(vividDrawable2);
        //
        ((VividView) findViewById(R.id.iv_vividView)).setPath(path)
                .setColors(new int[]{0x11ff0000, 0xffffffff});
        mList.add(((VividView) findViewById(R.id.iv_vividView)).getVividDrawable());
        //
//        ((VividView) findViewById(R.id.iv_vividView2)).setPath(path).setColors(new int[]{0x00ff0000, 0x55ffffff}).setMode(VividPath.Mode.Increase);

        ViewGroup parent = (ViewGroup) findViewById(R.id.ll_parent);
        for (int i = 0; i < parent.getChildCount(); i++) {
            parent.getChildAt(i).setBackgroundColor(i % 2 == 0 ? 0x20000000 : 0x10000000);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (VividDrawable drawable : mList) {
            if (drawable == null) {
                continue;
            }
            drawable.destroy();
        }
    }
}
