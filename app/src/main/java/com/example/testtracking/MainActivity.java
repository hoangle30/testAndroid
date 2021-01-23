package com.example.testtracking;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.PublicKey;

public class MainActivity extends AppCompatActivity {
    //khai báo các biến, mảng,...
    Button xem;
    RelativeLayout backgr,im_move_zoom_rotate;
    TextView ten_user;
    static EditText name;
    public float tendai,tenrong,daibackgr,rongbackgr,diemgiuax,diemgiuay,phong303X,phong303y,phong304X,phong304y,chieudaiphong,chieurongphong,diemdaux,diemdauy;
    public float toadox,toadoy;
    public static String var="hoang@gmail,com";
    public static String var1="hoang@gmail,com";
    public static String var2="hoang",kiemtra;
    int mang_phong_x []={1,0,1,0,1,0,1,0,1};
    int mang_phong_y []={0,0,0,1,1,2,2,3,3,0};
    float scalediff;
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;
    private float oldDist = 1f;
    private float d = 0f;
    private float newRot = 0f;

    //FirebaseAuth fAuth;
    DatabaseReference data_user1 = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // ánh xạ các thuộc tính từ file xml qua
        backgr = (RelativeLayout) findViewById(R.id.background);
        im_move_zoom_rotate= (RelativeLayout) findViewById(R.id.im_move_zoom_rotate);
        name = (EditText) findViewById(R.id.editTextTextPersonName);
        xem = (Button) findViewById(R.id.button);
        ten_user = (TextView) findViewById(R.id.textView_ten_user);
        name.setText(var2);
     //   fAuth=FirebaseAuth.getInstance();
        // nút nhấn để xem thông tin vị trí người dùng...
        xem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                var1 = name.getText().toString();
                kiemtra = var1.replace('.',',');
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(250,350);
                layoutParams.leftMargin = 50;
                layoutParams.topMargin = 50;
                layoutParams.bottomMargin = -250;
                layoutParams.rightMargin = -250;
                im_move_zoom_rotate.setLayoutParams(layoutParams);// đặt lại map
                kiemtratk();// kiểm tra thông tin người dùng có tồn tại không
            }
        });
        zoom_and_rotate();// zoom rotate
        getdataUser();// nhận data khi có thay đổi
    }
    public void tinhtoadiemdau()// tính tọa độ phòng đầu tiên trên map
    {
        // lấy chiều dài, chiều rộng của map (theo pixel)
        daibackgr = im_move_zoom_rotate.getHeight();
        rongbackgr = im_move_zoom_rotate.getWidth();
        // tọa độ điểm chính giữa
        diemgiuax=rongbackgr/2;
        diemgiuay=daibackgr/2;
        // tính chiều dài, chiều rộng của phòng (dựa theo map)
        chieudaiphong= (float) (daibackgr/5.7);
        chieurongphong= (float) (rongbackgr/3.1);
        // tính tọa độ điểm đầu tiên (theo pixel)
        diemdaux= diemgiuax- chieurongphong;
        diemdauy= diemgiuay- 2*chieudaiphong;
    }
    public void zoom_and_rotate()// zoom and rotate
    {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(250,350);
        layoutParams.leftMargin = 50;
        layoutParams.topMargin = 50;
        layoutParams.bottomMargin = -250;
        layoutParams.rightMargin = -250;
        im_move_zoom_rotate.setLayoutParams(layoutParams);
        ten_user.setTextSize(5);
        im_move_zoom_rotate.setOnTouchListener(new View.OnTouchListener() {
            RelativeLayout.LayoutParams parms;
            int startwidth;
            int startheight;
            float dx = 0, dy = 0, x = 0, y = 0;
            float angle = 0;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final RelativeLayout view = (RelativeLayout) v;
                ten_user.setTextSize(5);
//                ((BitmapDrawable) view.setBackgroundDrawable().setAntiAlias(true);
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:

                        parms = (RelativeLayout.LayoutParams) view.getLayoutParams();
                        startwidth = parms.width;
                        startheight = parms.height;
                        dx = event.getRawX() - parms.leftMargin;
                        dy = event.getRawY() - parms.topMargin;
                        mode = DRAG;
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        oldDist = spacing(event);
                        if (oldDist > 10f) {
                            mode = ZOOM;
                        }
                        d = rotation(event);
                        break;
                    case MotionEvent.ACTION_UP:

                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        mode = NONE;

                        break;

                    case MotionEvent.ACTION_MOVE:
                        if (mode == DRAG) {
                            x = event.getRawX();
                            y = event.getRawY();
                            parms.leftMargin = (int) (x - dx);
                            parms.topMargin = (int) (y - dy);
                            parms.rightMargin = 0;
                            parms.bottomMargin = 0;
                            parms.rightMargin = parms.leftMargin + (5 * parms.width);
                            parms.bottomMargin = parms.topMargin + (10 * parms.height);

                            view.setLayoutParams(parms);
                        } else if (mode == ZOOM) {
                            if (event.getPointerCount() == 2) {
                                newRot = rotation(event);
                                float r = newRot - d;
                                angle = r;

                                x = event.getRawX();
                                y = event.getRawY();

                                float newDist = spacing(event);
                                if (newDist > 10f) {
                                    float scale = newDist / oldDist * view.getScaleX();
                                    if (scale > 0.6) {
                                        scalediff = scale;
                                        view.setScaleX(scale);
                                        view.setScaleY(scale);
                                    }
                                }
                                view.animate().rotationBy(angle).setDuration(0).setInterpolator(new LinearInterpolator()).start();
                                x = event.getRawX();
                                y = event.getRawY();

                                parms.leftMargin = (int) ((x - dx) + scalediff);
                                parms.topMargin = (int) ((y - dy) + scalediff);

                                parms.rightMargin = 0;
                                parms.bottomMargin = 0;
                                parms.rightMargin = parms.leftMargin + (5 * parms.width);
                                parms.bottomMargin = parms.topMargin + (10 * parms.height);

                                view.setLayoutParams(parms);
                            }
                        }
                        break;
                }
                return true;
            }


        });
    }
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private float rotation(MotionEvent event) {
        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }

    private  void kiemtratk()// kiểm tra xem thông tin người dùng có tồn tại không
    {
        DatabaseReference data_kiemtra = FirebaseDatabase.getInstance().getReference();
        data_kiemtra.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.child(kiemtra).exists()) {
                    var = kiemtra;
                    String[] names = var1.split("@");
                    var2 = names[0]; // 004
                    getdataUser();
                }
                else
                {
                    name.setError("No data");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    private void getdataUser()// llấy dữ liệu vị trí từ firebase
    {
        data_user1.child(var).addValueEventListener(new ValueEventListener() // lắng nghe dữ liệu từ firebase
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot data)// nếu có thay đổi thì gọi hàm
            {
                test1();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void test1()// hàm được gọi khi có thay đổi dữ liệu
    {
        data_user1.child(var).addListenerForSingleValueEvent(new ValueEventListener()// lấy dữ liệu của người dùng (người đang muốn xem vị trí)
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot data) {
                String yy = data.getValue().toString();// nhận dữ liệu
                String[] parts = yy.split(",");// xử lý chuỗi
                String part1 = parts[0]; // tọa độ x
                String part2 = parts[1];//toạ độ y
                String part3 = parts[2]; // số phòng, số tầng
                char z = part3.charAt(0);// tách số tầng
                char sophong = part3.charAt(2); // tách số phòng

                toadox = Float.parseFloat(part1);// tọa độ lấy từ firebase (float)
                toadoy = Float.parseFloat(part2);// toạ độ lấy từ firebase (float)
                int sophongint=Character.getNumericValue(sophong);// chuyển số phòng sang dạng int
                // hiển thị map tầng 3 hoặc tầng 4
                if (z=='3'){
                    im_move_zoom_rotate.setBackgroundResource(R.drawable.map_c3);
                }
                else if (z=='4'){
                    im_move_zoom_rotate.setBackgroundResource(R.drawable.map_c4);
                }
                else {
                    im_move_zoom_rotate.setBackgroundColor(Color.rgb(123,23,200));
                }
                tendai = ten_user.getHeight();// xác định chiều dài của tên hiển thị
                tenrong = ten_user.getWidth();// xác định chiều rộng của tên hiển thị

                tinhtoadiemdau();// tính tọa độ đầu
                // vị trí của người dùng trong phòng
                toadox=diemdaux + chieurongphong*mang_phong_x[sophongint] + (chieurongphong * toadox) / 10;
                toadoy=diemdauy + chieudaiphong*mang_phong_y[sophongint] + (chieudaiphong * toadoy) / 10;
                // không để vị trí người dùng vượt quá phòng
                if (toadox >= diemdaux + chieurongphong*mang_phong_x[sophongint] + chieurongphong )
                {
                    toadox = toadox - tenrong;
                }
                if (toadoy >= diemdauy + chieudaiphong*mang_phong_y[sophongint] + chieudaiphong) {
                    toadoy = toadoy - tendai;
                }
                ten_user.animate().x(toadox).y(toadoy);// set tọa độ theo vị trí người dùng, sử dụng hiệu ứng animation
                ten_user.setText(var2);//animation();// hiển thị tên người dùng
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}