package example;

import ev3dev.actuators.Sound;
import ev3dev.sensors.Battery;
import ev3dev.sensors.ev3.EV3GyroSensor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;
import ev3dev.actuators.lego.motors.EV3MediumRegulatedMotor;

public class GyroSensor {
    static EV3GyroSensor gyroSensor = new EV3GyroSensor(SensorPort.S3);
    static EV3MediumRegulatedMotor latch = new EV3MediumRegulatedMotor(MotorPort.C);

    public static int currAngle;
    public static int currLatchPos;


    GyroSensor(){
    }
    public static void initGyro(){
        gyroSensor.reset();
        System.out.println("Reset done from initGyro \n");

    }
    public static void initMotorLatchSpeed(){
        int latchSpeed=180;
        //System.out.println("Defining motor speed to "+latchSpeed+" degrees/s\n");
        latch.setSpeed(latchSpeed);
    }
    public static void moveLatch(int dir){
        double angToTurn=90;
        double relation=5.432; //relation between degrees to turn from angle in degrees and motor position
        int angg= (int) (angToTurn*relation);
        if(dir==0){
            latch.rotate(angg);
        }else{
            latch.rotate((-angg));
        }

    }
    public static int currentGyroAngle(){

        final SampleProvider sp = gyroSensor.getAngleMode();

        float [] sample = new float[sp.sampleSize()]; //save sample in array
        sp.fetchSample(sample, 0); //get sample
        currAngle=(int)sample[0];

        System.out.println("Gyro angle is"+(-currAngle)+"\n");

        //Delay.msDelay(2000);

        /*if(currAngle > 90){
            latch.stop();
            System.out.println("Rotated 90 degrees from start position"+"\n");
            Delay.msDelay(1000);

        }
        else if(currAngle <= 0){
            latch.stop();
            System.out.println("In start position"+"\n");
            Delay.msDelay(1000);

        }*/
        return currAngle;
    }


    public static void main(String[] args) {
        //int cnt=0;
        int dir=0;
        initGyro();
        initMotorLatchSpeed();
        currentGyroAngle();
        currLatchPos= (int) latch.getPosition();
        System.out.println("1st Latch pos is"+currLatchPos+"\n");
        moveLatch(dir);
        currentGyroAngle();
        currLatchPos= (int) latch.getPosition();
        System.out.println("2nd Latch pos is"+currLatchPos+"\n");
        dir=1;
        moveLatch(dir);
        Delay.msDelay(10000);

       /* while(cnt<10){
            currentGyroAngle();
            Delay.msDelay(500);
            cnt++;
        }*/

    }

}
