package net.unfinishedhacks.hullstiffnessmonitor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.provider.ContactsContract;
import android.util.AttributeSet;
import android.view.View;

import java.util.Vector;

public class DataView extends View {
    Paint paint = new Paint();
    int width;
    int height;
    private Vector dataStorage;
    private double maxHeelAngle;
    private double maxDevAngle;
    double heelResolution;
    private int devResolution;
    Vector posHeel;
    Vector negHeel;
    int bottomMargin;

    private void init() {
        paint.setColor(Color.BLACK);
        width = this.getMeasuredWidth();
        height = this.getMeasuredHeight();
        maxHeelAngle = 10.0;
        maxDevAngle = 1.0;
        heelResolution = 1.0;
        devResolution = 2;
        posHeel =  new Vector();
        negHeel =  new Vector();
        bottomMargin = height-30;
    }

    public DataView(Context context) {
        super(context);
        init();
    }

    public DataView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DataView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @Override
    public void onDraw(Canvas canvas) {
        height = this.getHeight();
        bottomMargin = height-30;
        width = this.getWidth();
        canvas.drawLine(0, 0, 20, 20, paint);
        canvas.drawLine(20, 0, 0, 20, paint);

        drawAxes(canvas);
        drawData(canvas);
    }

    private double getMaxHeel() {
        //double maxHeel = 0.0;
        return maxHeelAngle;

    }

    private void drawAxes(Canvas canvas) {

        System.out.println("drawAxes("+width+","+height+")");
        canvas.drawLine(0, bottomMargin, width, bottomMargin, paint);
        canvas.drawLine(width/2, 0, width/2, bottomMargin, paint);
        //paint.setFontFeatureSettings();
        canvas.drawText("0",width/2,height,paint);
        // draw heel scale
        double maxHeel = 10.0;
        if (getMaxHeel()>maxHeel) maxHeel = getMaxHeel();

        int increment = 1;
        if (maxHeel > 20)
            increment = 5;

        int scalePix = width/2;
        int tickWidth = (int) (scalePix/(maxHeel/increment));
        for (int i = 0; i < (maxHeel/increment);i++)
        {
            canvas.drawLine(scalePix-tickWidth*i, bottomMargin, scalePix-tickWidth*i, height, paint);
            canvas.drawLine(scalePix+tickWidth*i, bottomMargin, scalePix+tickWidth*i, height, paint);
        }
    }

    private void drawData(Canvas canvas) {
        double maxDev = 0.0;
        double minDev = 1000.0;
        int scalePix = width/2;
        double maxHeel = 10.0;
        int increment = 1;

        if (getMaxHeel()>maxHeel) maxHeel = getMaxHeel();
        int tickWidth = (int) (scalePix/(maxHeel/increment));
        paint.setColor(Color.RED);
        double tempDev;
        double temp = 0.0;
        Vector tempVec;

        for (int i = 0; i < maxHeelAngle/heelResolution;i++) {
            if(posHeel.size() <= i)
                break;
            tempVec = (Vector) posHeel.get(i);
            if (tempVec != null && tempVec.size() > 0) {

                for (int j = 0; j < tempVec.size(); j++) {
                    tempDev = ((DataElement) tempVec.get(j)).getDevAngleDeg10x();
                    canvas.drawLine(scalePix + tickWidth * i - 5, (int) (bottomMargin * (tempDev / maxDevAngle)), scalePix + tickWidth * i + 5, (int) (bottomMargin * (tempDev / maxDevAngle)), paint);

                    if (tempDev < minDev)
                        minDev = tempDev;
                    if(tempDev > maxDev)
                        maxDev = tempDev;

                }

                canvas.drawLine(scalePix + tickWidth * i, (int) (bottomMargin * (maxDev / maxDevAngle)), scalePix + tickWidth * i, (int) (bottomMargin * (minDev / maxDevAngle)), paint);
                System.out.println("DataView.drawData maxDev: " + maxDev + " minDev: " + minDev);
                minDev = 1000.0;
                maxDev = 0.0;
            }
        }

        for (int i = 0; i < maxHeelAngle/heelResolution;i++) {
            if(negHeel.size() <= i)
                break;
            tempVec = (Vector) negHeel.get(i);
            if (tempVec != null && tempVec.size() > 0) {
                for (int j = 0; j < tempVec.size(); j++) {
                    tempDev = ((DataElement) tempVec.get(j)).getDevAngleDeg10x();
                    canvas.drawLine(scalePix - tickWidth * i - 5, (int) (bottomMargin * (tempDev / maxDevAngle)), scalePix - tickWidth * i + 5, (int) (bottomMargin * (tempDev / maxDevAngle)), paint);
                    if (tempDev < minDev)
                        minDev = tempDev;
                    if(tempDev > maxDev)
                        maxDev = tempDev;
                }
                canvas.drawLine(scalePix - tickWidth * i, (int) (bottomMargin * (maxDev / maxDevAngle)), scalePix - tickWidth * i, (int) (bottomMargin * (minDev / maxDevAngle)), paint);
                System.out.println("DataView.drawData maxDev: " + maxDev + " minDev: " + minDev);
                minDev = 1000.0;
                maxDev = 0.0;
            }
        }
        paint.setColor(Color.BLACK);
    }

    public void addData(DataElement elem) {
        boolean addtoEnd = true;
        double tempDouble = 0.0;

        System.out.println("DataView.addData("+elem.getHeelAngleDeg()+","+elem.getDevAngleDeg10x()+")");
        int index = (int) (elem.getHeelAngleDeg()/heelResolution);
        if (index< 0) {
            index = Math.abs(index);
            if(negHeel.size() <= index) {
                    for(int i = negHeel.size(); i < index+1; i++) {
                        negHeel.add(new Vector());
                    }
            }
            if(((Vector)negHeel.get(index)).size() != 0)
            {
                DataElement temp;
                for (int i = 0; i < ((Vector)negHeel.get(index)).size();i++)
                {
                    temp = (DataElement) ((Vector)negHeel.get(index)).get(i);
                    if(elem.getDevAngleDeg10x() > temp.getDevAngleDeg10x()-devResolution/2 || elem.getDevAngleDeg10x() > temp.getDevAngleDeg10x()+devResolution/2) {
                        addtoEnd = false;
                        temp.incrementCount();
                        System.out.println("DataView.addData increment existing("+elem.getDevAngleDeg10x()+" vs. "+temp.getDevAngleDeg10x()+") dev resolution "+devResolution/2+" count: "+ temp.getCount());
                        break;
                    }
                }
            }
            if(addtoEnd) {
                ((Vector)negHeel.get(index)).add(elem);
            }
        }
        else
        {
            index = Math.abs(index);
            if(posHeel.size() <= index) {
                for(int i = posHeel.size(); i < index+1; i++) {
                    posHeel.add(new Vector());
                }
            }
            if(((Vector)posHeel.get(index)).size() != 0)
            {
                DataElement temp;
                for (int i = 0; i < ((Vector)posHeel.get(index)).size();i++)
                {
                    temp = (DataElement) ((Vector)posHeel.get(index)).get(i);
                    if(elem.getDevAngleDeg10x() > temp.getDevAngleDeg10x()-devResolution/2 || elem.getDevAngleDeg10x() > temp.getDevAngleDeg10x()+devResolution/2) {
                        addtoEnd = false;
                        temp.incrementCount();
                        break;
                    }
                }
            }
            if(addtoEnd) {
                ((Vector)posHeel.get(index)).add(elem);
            }
        }

        //updateMaxValues();
        tempDouble = Math.abs(elem.getHeelAngleDeg());
        if ( tempDouble > maxHeelAngle)
            maxHeelAngle = tempDouble;

        tempDouble = Math.abs(elem.getDevAngleDeg10x());
        if ( tempDouble > maxDevAngle)
            maxDevAngle = tempDouble;
    }

    private void updateMaxValues() {
        double temp;
        for (int i = 0; i < dataStorage.size(); i++) {
            temp = Math.abs(((DataElement) dataStorage.get(i)).getHeelAngleDeg());
            if ( temp > maxHeelAngle)
                maxHeelAngle = temp;

            temp = Math.abs(((DataElement) dataStorage.get(i)).getDevAngleDeg10x());
            if ( temp > maxDevAngle)
                maxDevAngle = temp;
        }
    }
}
