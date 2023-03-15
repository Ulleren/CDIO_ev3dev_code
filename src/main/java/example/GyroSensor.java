package example;

import ev3dev.actuators.Sound;
import ev3dev.sensors.ev3.EV3GyroSensor;
import lejos.hardware.port.SensorPort;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;
import ev3dev.actuators.lego.motors.EV3MediumRegulatedMotor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class GyroSensor {
    static EV3GyroSensor gyroSensor = new EV3GyroSensor(SensorPort.S2);

    public static int currAngle;
    GyroSensor(){
    }
    public static void initGyro(){
        gyroSensor.reset();
        System.out.println("Reset done from initGyro \n");

    }
    public static int currentGyroAngle(){

        final SampleProvider sp = gyroSensor.getAngleMode();

        float [] sample = new float[sp.sampleSize()]; //save sample in array
        sp.fetchSample(sample, 0); //get sample
        currAngle=(int)sample[0];

        System.out.println("Gyro angle is"+currAngle+"\n");
        Delay.msDelay(1000);

        if(currAngle > 90){
            System.out.println("Rotated 90 degrees from start position"+"\n");
            Delay.msDelay(1000);
            //call to stop movement of latch
        }
        else if(currAngle <= 0){
            System.out.println("In start position"+"\n");
            Delay.msDelay(1000);
            //call to stop movement of latch
        }
        return currAngle;
    }

    public static void main(String[] args) {
        int cnt=0;
        initGyro();
        while(cnt<10){
            currentGyroAngle();
            Delay.msDelay(2000);
            cnt++;
        }

    }

}
